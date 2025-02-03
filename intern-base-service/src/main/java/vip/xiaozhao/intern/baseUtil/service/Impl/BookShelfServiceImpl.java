package vip.xiaozhao.intern.baseUtil.service.Impl;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vip.xiaozhao.intern.baseUtil.intf.constant.RedisConstant;
import vip.xiaozhao.intern.baseUtil.intf.entity.Message;
import vip.xiaozhao.intern.baseUtil.intf.entity.NovelInfo;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelBookshelf;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelSubscribeAudit;
import vip.xiaozhao.intern.baseUtil.intf.mapper.BookShelfMapper;
import vip.xiaozhao.intern.baseUtil.intf.mapper.MessageLevelMapper;
import vip.xiaozhao.intern.baseUtil.intf.mapper.MessageMapper;
import vip.xiaozhao.intern.baseUtil.intf.mapper.NovelInfoMapper;
import vip.xiaozhao.intern.baseUtil.intf.service.BookShelfService;
import vip.xiaozhao.intern.baseUtil.intf.service.NovelInfoService;
import vip.xiaozhao.intern.baseUtil.intf.utils.JsonUtils;
import vip.xiaozhao.intern.baseUtil.intf.utils.redis.RedisUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookShelfServiceImpl implements BookShelfService {
     /*
        区分主从在mapper层中，加了 @SlaveDataSource 是从库读取，可以指定从库的名称，没加的是默认主库操作
        或者加了名称是master的都是从主库读取
     */


    @Resource
    private BookShelfMapper bookShelfMapper;

    @Resource
    private NovelInfoService novelInfoService;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private MessageLevelMapper messageLevelMapper;

    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private NovelInfoMapper novelInfoMapper;

    @Override
    public List<YikeNovelBookshelf> getBookShelfByUserId(int userId) throws Exception {
        String strUserId = String.valueOf(userId);
        if(RedisUtils.get(RedisConstant.preUserId +  strUserId, List.class) == null){
            List<YikeNovelBookshelf> bookShelfByUserId = bookShelfMapper.getBookShelfByUserId(userId);
            if(bookShelfByUserId == null || bookShelfByUserId.isEmpty()){
                return null;
            }
            bookShelfByUserId.sort(new Comparator<YikeNovelBookshelf>() {
                @Override
                public int compare(YikeNovelBookshelf o1, YikeNovelBookshelf o2) {
                    return o2.getIsTop() - o1.getIsTop();
                }
            });
            String str = JsonUtils.toStr(bookShelfByUserId);
            RedisUtils.set(RedisConstant.preUserId +  strUserId,str,RedisUtils.EXRP_ONE_HOUR);
            return bookShelfByUserId;
        }else{
            String strList = RedisUtils.get(RedisConstant.preUserId +  strUserId);
            ArrayList<YikeNovelBookshelf> yikeNovelBookshelves = parseStringToList(strList);
            System.out.println("yikeNovelBookshelves " + yikeNovelBookshelves);
            return yikeNovelBookshelves;
        }
    }
    private ArrayList<YikeNovelBookshelf> parseStringToList(String jsonString) throws Exception {
        try {
            return objectMapper.readValue(jsonString, new TypeReference<ArrayList<YikeNovelBookshelf>>() {});
        } catch (MismatchedInputException e) {
            //throw new RuntimeException("JSON 格式错误，无法解析为对象列表");
            // 如果失败，尝试将字符串转为数组后再反序列化
            String[] items = jsonString.split(",");
            ArrayList<YikeNovelBookshelf> list = new ArrayList<>();
            for (String item : items) {
                YikeNovelBookshelf bookshelf = objectMapper.readValue(item, YikeNovelBookshelf.class);
                list.add(bookshelf);
            }
            return list;
        }
    }

    @Override
    public void readChapter(int userID,int novelId, int chapterId) throws Exception {
        // 拿到这个人这本书的信息（从书架表中）
        YikeNovelBookshelf bookShelfByUserIdAndNovelId = bookShelfMapper.getBookShelfByUserIdAndNovelId(userID, novelId);
        if(bookShelfByUserIdAndNovelId == null){
            throw new RuntimeException("该用户没有订阅该小说");
        }
        // 拿到小说的最新更新时间
        Date lastUpdateTime = bookShelfByUserIdAndNovelId.getLastUpdateTime();
        //NovelInfo novelInfoByNovelId = novelInfoMapper.getNovelInfoByNovelId(novelId);
        // 拿到最后一次的发送消息的时间
        Message sendTimeMessage = messageMapper.getSendTime(userID, novelId, chapterId);
        // 获取当前时间
        long currentTimeMillis = System.currentTimeMillis();
        int lastUpdateTimeMinutes = Integer.MAX_VALUE;
        int sendTimeMinutes = Integer.MAX_VALUE;
        if (lastUpdateTime != null) {
            // 计算当前时间与 小说最新更新时间 的差值（以分钟为单位）
            long lastUpdateTimeDiffMillis = currentTimeMillis - lastUpdateTime.getTime();
            lastUpdateTimeMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(lastUpdateTimeDiffMillis);
        }
        if (sendTimeMessage != null) {
            Date sendTime = sendTimeMessage.getSendTime();
            if (sendTime != null) {
                // 计算当前时间与 最新消息发送时间 的差值（以分钟为单位）
                long sendTimeDiffMillis = currentTimeMillis - sendTime.getTime();
                sendTimeMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(sendTimeDiffMillis);
            }
        }
        // 取当前时间与两个时间中的最小差值
        int timeGapMinutes = Math.min(lastUpdateTimeMinutes, sendTimeMinutes);
        // 获取当前时间间隔对应的等级
        int level = getLevel(timeGapMinutes);
        // 只有当前时间间隔对应等级比当前等级小（1是小，5是大，1对应发送频率最高的那一档，5是发送频率最低的那一档）
        // 并且原先还处于345等级的用户，采取只升一级
        if(bookShelfByUserIdAndNovelId.getSendLevel() > level && bookShelfByUserIdAndNovelId.getSendLevel() >= 3){
            messageLevelMapper.updateSendLevelByUserIdAndNovelId(bookShelfByUserIdAndNovelId.getSendLevel() - 1,userID,novelId);
        }
        // 向流水表中插入数据
        bookShelfMapper.readChapter(userID,novelId,chapterId,timeGapMinutes);
        // 这个更新用户书架订阅缓存
        RedisUtils.remove(RedisConstant.preUserId +  String.valueOf(userID));
        getBookShelfByUserId(userID);
    }

    public static int getLevel(int timeGapMinutes) {
        // 根据分钟差来判断等级
        int level;
        if (timeGapMinutes < 60) {
            level = 1; // A - 有通知就打开【1小时内】
        } else if (timeGapMinutes < 1440) { // 60 * 24 = 1440
            level = 2; // B - 当天非实时，每天发一次
        } else if (timeGapMinutes >= 1440 && timeGapMinutes <= 4320) { // 1440 * 3 = 4320
            level = 3; // C - 【1-3天】 ，2天发一次
        } else if (timeGapMinutes > 4320 && timeGapMinutes <= 10080) { // 10080 = 7 * 1440
            level = 4; // D - 【3-7天】 ，1周通知一次
        } else {
            level = 5; // E - 【>7天】(取关) -》不通知
        }
        return level;
    }


    @Override
    public void updateTopBook(int userID, int novelId) throws Exception {
        List<YikeNovelBookshelf> bookShelfByUserId = getBookShelfByUserId(userID);
        Boolean isexistNovel = false;
        int isTopID = -1;
        for (YikeNovelBookshelf yikeNovelBookshelf : bookShelfByUserId) {
            if(yikeNovelBookshelf.getNovelId() == novelId){
                isexistNovel = true;
            }
            if(yikeNovelBookshelf.getIsTop() == 1){
                isTopID = yikeNovelBookshelf.getNovelId();
                break;
            }
        }
        if(!isexistNovel){
            throw new RuntimeException("该小说不存在");
        }
        if(isTopID != -1 && bookShelfByUserId.size() > 1){
            bookShelfMapper.updateTopBook(userID,isTopID);
        }
        bookShelfMapper.updateTopBook(userID,novelId);
        RedisUtils.remove(RedisConstant.preUserId +  String.valueOf(userID));
        getBookShelfByUserId(userID);
    }

    @Override
    public void deleteBookByUserIdAndNovelId(int userID, int novelId) throws Exception {
        List<YikeNovelBookshelf> bookShelfByUserId = getBookShelfByUserId(userID);
        int flag = 0;
        for (YikeNovelBookshelf yikeNovelBookshelf : bookShelfByUserId) {
            if (yikeNovelBookshelf.getNovelId() == novelId) {
                flag = 1;
                break;
            }
        }
        if(flag== 0){
            throw new RuntimeException("即将删除的小说不存在");
        }
        bookShelfMapper.deleteBookByUserIdAndNovelId(userID,novelId);
        RedisUtils.remove(RedisConstant.preUserId +  String.valueOf(userID));
        getBookShelfByUserId(userID);

        RedisUtils.deleteUserIdFromNovelId(userID,novelId);

    }

    @Override
    public void subscribeNovel(int userID, int novelId) throws Exception {
        List<YikeNovelBookshelf> bookShelfByUserId = getBookShelfByUserId(userID);
        if(bookShelfByUserId.size() >= 5){
            throw new RuntimeException("小说已经满5本，不可以继续添加");
        }
        NovelInfo novelInfoByNovelId = novelInfoService.getNovelInfoByNovelId(novelId);
        YikeNovelBookshelf yikeNovelBookshelf = new YikeNovelBookshelf();
        yikeNovelBookshelf.setUserId(userID);
        yikeNovelBookshelf.setNovelId(novelId);
        yikeNovelBookshelf.setBookName(novelInfoByNovelId.getBookName());
        yikeNovelBookshelf.setCoverUrl(novelInfoByNovelId.getCover());
        yikeNovelBookshelf.setAuthorName(novelInfoByNovelId.getAuthorName());
        yikeNovelBookshelf.setLastUpdateTime(novelInfoByNovelId.getLastUpdateTime());
        yikeNovelBookshelf.setLatestChapterId(novelInfoByNovelId.getLatestChapterId());
        yikeNovelBookshelf.setLatestChapter(novelInfoByNovelId.getLatestChapter());
        bookShelfMapper.subscribeBook(yikeNovelBookshelf);
        RedisUtils.remove(RedisConstant.preUserId +  String.valueOf(userID));

        String redisKey = RedisConstant.preNovelId + novelId;
        // 使用 Redis Set 存储用户 ID
        try {
            RedisUtils.addToSet(redisKey, userID, RedisUtils.EXRP_ONE_HOUR,true);
            System.out.println("用户 ID: " + userID + " 已添加到书籍: " + novelId + " 的用户列表中");
        } catch (Exception e) {
            throw new RuntimeException("添加用户 ID 到 Redis Set 时出现异常", e);
        }


    }

    @Override
    public void updateIsReadByUserIdAndNovelId(int userID, int novelId) throws Exception {
        bookShelfMapper.updateIsReadByUserIdAndNovelId(userID,novelId);
        RedisUtils.remove(RedisConstant.preUserId +  String.valueOf(userID));
        getBookShelfByUserId(userID);
    }


    @Override
    public void updateIsReadByNovelId(int userID,int NovelId) throws Exception {
        // redis 批量更新
        Set<String> setMembers = RedisUtils.getSetMembers(RedisConstant.preNovelId + String.valueOf(NovelId));
        if (setMembers == null || setMembers.isEmpty()) {
            return;
        }
        List<Integer> userIDs = setMembers.stream()
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        final int batchSize = 100;
        int totalSize = userIDs.size();

        for (int i = 0; i < totalSize; i += batchSize) {
            int end = Math.min(i + batchSize, totalSize);
            List<Integer> batchUserIDs = userIDs.subList(i, end);
            bookShelfMapper.updateIsReadByNovelId(NovelId, batchUserIDs);
        }
         /*
         或者使用多线程处理
        // 批处理大小
        final int batchSize = 100; // 根据需要调整
        int totalSize = userIDs.size();

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(10); // 根据需要调整线程数
        CountDownLatch latch = new CountDownLatch((totalSize + batchSize - 1) / batchSize); // 计算总的批次数

        // 分批处理
        for (int i = 0; i < totalSize; i += batchSize) {
            final List<Integer> batchUserIDs = userIDs.subList(i, Math.min(i + batchSize, totalSize));

            // 提交任务到线程池
            executor.submit(() -> {
                try {
                    // 数据库更新操作
                    bookShelfMapper.updateIsReadByNovelId(NovelId, batchUserIDs);
                } finally {
                    latch.countDown(); // 完成该批次后减少计数
                }
            });
        }

        // 等待所有任务完成
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace(); // 处理异常
        } finally {
            executor.shutdown(); // 关闭线程池
        }

          */

        for (Integer userid : userIDs) {
            if(RedisUtils.get(RedisConstant.preUserId +  String.valueOf(userID)) != null){
                RedisUtils.remove(RedisConstant.preUserId +  String.valueOf(userID));
            }
        }

    }

    @Override
    public void updateIsReadByNovelIdList(int userID, List<Integer> novelIds) throws Exception {
        for (Integer NovelId : novelIds) {
            // redis 批量更新
            Set<String> setMembers = RedisUtils.getSetMembers(RedisConstant.preNovelId + String.valueOf(NovelId));
            if (setMembers == null || setMembers.isEmpty()) {
                return;
            }
            List<Integer> userIDs = setMembers.stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());

            final int batchSize = 100;
            int totalSize = userIDs.size();

            for (int i = 0; i < totalSize; i += batchSize) {
                int end = Math.min(i + batchSize, totalSize);
                List<Integer> batchUserIDs = userIDs.subList(i, end);
                bookShelfMapper.updateIsReadByNovelId(NovelId, batchUserIDs);
            }
         /*
         或者使用多线程处理
        // 批处理大小
        final int batchSize = 100; // 根据需要调整
        int totalSize = userIDs.size();

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(10); // 根据需要调整线程数
        CountDownLatch latch = new CountDownLatch((totalSize + batchSize - 1) / batchSize); // 计算总的批次数

        // 分批处理
        for (int i = 0; i < totalSize; i += batchSize) {
            final List<Integer> batchUserIDs = userIDs.subList(i, Math.min(i + batchSize, totalSize));

            // 提交任务到线程池
            executor.submit(() -> {
                try {
                    // 数据库更新操作
                    bookShelfMapper.updateIsReadByNovelId(NovelId, batchUserIDs);
                } finally {
                    latch.countDown(); // 完成该批次后减少计数
                }
            });
        }

        // 等待所有任务完成
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace(); // 处理异常
        } finally {
            executor.shutdown(); // 关闭线程池
        }

          */
            for (Integer userid : userIDs) {
                if(RedisUtils.get(RedisConstant.preUserId +  String.valueOf(userID)) != null){
                    RedisUtils.remove(RedisConstant.preUserId +  String.valueOf(userID));
                }
            }
        }
    }


}
