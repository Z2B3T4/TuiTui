package vip.xiaozhao.intern.baseUtil.service.Impl;

import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vip.xiaozhao.intern.baseUtil.intf.entity.Message;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelBookshelf;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelSubscribeAudit;
import vip.xiaozhao.intern.baseUtil.intf.mapper.BookShelfMapper;
import vip.xiaozhao.intern.baseUtil.intf.mapper.MessageLevelMapper;
import vip.xiaozhao.intern.baseUtil.intf.mapper.MessageMapper;
import vip.xiaozhao.intern.baseUtil.intf.mapper.NovelInfoMapper;
import vip.xiaozhao.intern.baseUtil.intf.service.MessageLevelService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
    本期代码一个是这个，另一个是在BookShelfServiceImpl的readChapter方法中

 */

@Service
public class MessageLevelServiceImpl implements MessageLevelService {
    @Resource
    private MessageLevelMapper messageLevelMapper;

    @Resource
    private BookShelfMapper bookShelfMapper;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private NovelInfoMapper novelInfoMapper;


    // 每天凌晨2点执行
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateMessageLevel() {
        // 拿到书架表中的所有数据，因为每一条数据都有用户id，小说id，根据这个每个数据都去流水表中查询出最近3条记录，然后根据这个最近3条记录中的时间差计算出等级，然后修改小说的发送等级
        // 从而更新消息推送等级
        List<YikeNovelBookshelf> bookSubscribeAll = bookShelfMapper.getBookSubscribeAll();
        int finalLevel = 0;
        for (YikeNovelBookshelf yikeNovelBookshelf : bookSubscribeAll) {
            int userId = yikeNovelBookshelf.getUserId();
            int novelId = yikeNovelBookshelf.getNovelId();
            // 根据用户id和小说id拿到这个人这本书最近三次阅读的流水记录
            List<YikeNovelSubscribeAudit> last3Audit
                    = messageLevelMapper.getYikeNovelSubscribeAuditByUserIdAndNovelId(userId, novelId);
            // 只有阅读流水不为空才进行下面的操作，只有当用户订阅了新小说但是没看的时候为空
            if (!last3Audit.isEmpty()){
                // maxLevel 是最后3条记录中最大的level
                int minLevel = 0;
                for (YikeNovelSubscribeAudit yikeNovelSubscribeAuditOneUser : last3Audit) {
                    int level = BookShelfServiceImpl.getLevel(yikeNovelSubscribeAuditOneUser.getTimeGap());
                    if (level <  minLevel) {
                        minLevel = level;
                    }
                }
                // 获取最后一次阅读的时间即AddTime字段和最后一次发送通知的时间
                YikeNovelSubscribeAudit leastAudit = last3Audit.get(0);
                Date leastReadTime = leastAudit.getAddTime();
                int timeGapToLastReadTime = 0;  // 距离上次阅读的时间间隔（分钟）
                int newLevel = 0;  // 由这个时间间隔算出的对应发送等级，注意这里赋值的默认值是5，代表5等级
                if (leastReadTime != null) {
                    // 获取最后一次发送通知的时间
                    Message leastMessage = messageMapper.getLeastSendTime(userId, novelId);
                    if(leastMessage != null){
                        Date leastSendTime = leastMessage.getSendTime();
                        // 计算 leastReadTime 和 sendTime 之间的时间差（毫秒）
                        long diffInMillis = leastSendTime.getTime() - leastReadTime.getTime();
                        // 转换为分钟
                        timeGapToLastReadTime = (int) TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
                        newLevel = BookShelfServiceImpl.getLevel(timeGapToLastReadTime);
                    }
                }
                // 修改小说的发送等级
                // 拿到当前之前该用户这本小说的发送等级
                YikeNovelBookshelf bookShelfByUserIdAndNovelId = bookShelfMapper.getBookShelfByUserIdAndNovelId(userId, novelId);
                Date updateTime = bookShelfByUserIdAndNovelId.getUpdateTime();

                // 获取当前时间
                Date currentTime = new Date();
                // 获取今天的日期
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 2);
                calendar.set(Calendar.MINUTE, 5);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Date twoFiveAM = calendar.getTime(); // 今天凌晨2点5分

                // 判断 updateTime 是否在今天凌晨2点5分到当前时间之间
                // 2点5分是因为每天凌晨两点的定时任务，
                // 由于数据量大，并且人流量不高，所以确保这个时间段确保是所有数据都已经更新完成
                /*
                * 目的：正常如果是每天定时任务或者没有触发低等级突然变快但是只升一级的条件的话，
                *       不会去改书架表中的推送等级字段
                *       但是如果改了则一定是触发了升一级，那么此时这一天的定时任务重最近三次的一定是有很短时间间隔的记录
                *       即对应1或者2等级的，但是我们不应该按照这个等级，应该按照只升一级的结果
                *
                *       并且如果能进入到if判断中，说明书架表在今天已经更新过
                *           ，那么其实一定newLevel是短的，因为这个小说用户今天就看过
                *       所以此时在和升一级之后的sendLevel取等级较大者，
                *       （即对应推送等级较大的，1是较小的等级，5是较大的等级，1等级对应一小时内响应的用户群体对应的等级）
                * */
                if (updateTime != null && updateTime.after(twoFiveAM) && updateTime.before(currentTime)) {
                    // updateTime 在今天凌晨2点5分到现在之间
                    int sendLevel = bookShelfByUserIdAndNovelId.getSendLevel();
                    finalLevel = Math.max(Math.max(minLevel,newLevel), sendLevel);
                } else {
                    // updateTime 不在这个时间范围内
                    /*
                    * 进入到这里说明今天并没有触发只升一级的条件，说明书架表中的就是昨天定时任务更新的，
                    * 所以只需要看近三次和最近一次和最新通知时间较大的推送等级（越大的推送等级对应的就是发送频率越低的方式）
                    *
                    * 为什么是较大者？ 首先minLevel是前三次中推送等级最小的（也就是发送频率最快的对应的等级）
                    *       比如：如果前三次有1等级的，但是最近一次距离通知时间间隔长，导致这个newLevel是3或者4，
                    *               那我们肯定是要看最近他没读，自然是降级，要这个推送等级大的（即发送频率低的）
                    *       再比如：  如果前三次全是低等级（3、4、5级），那么自然最近一次与通知时间间隔也不会比他们短
                    *               所以取较大值也是合理的
                    * */
                    finalLevel = Math.max(minLevel,newLevel);
                }
            }else{
                // 这里是如果用户对于这本小说没有阅读记录那么就是刚订阅，默认是1级
                // 当然即便用户自从订阅之后没有阅读过也要加入到定时任务重进行，因为有的用户真的是订阅了但是从来不读，我们需要定时任务去降级
                finalLevel = 1;
            }
            messageLevelMapper.updateSendLevelByUserIdAndNovelId(finalLevel, userId, novelId);

        }
    }

    
}
