package vip.xiaozhao.intern.baseUtil.service.Impl;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import vip.xiaozhao.intern.baseUtil.intf.constant.RedisConstant;
import vip.xiaozhao.intern.baseUtil.intf.entity.NovelInfo;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelBookshelf;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelSubscribeAudit;
import vip.xiaozhao.intern.baseUtil.intf.mapper.BookShelfMapper;
import vip.xiaozhao.intern.baseUtil.intf.service.BookShelfService;
import vip.xiaozhao.intern.baseUtil.intf.service.NovelInfoService;
import vip.xiaozhao.intern.baseUtil.intf.utils.JsonUtils;
import vip.xiaozhao.intern.baseUtil.intf.utils.redis.RedisUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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

    private ObjectMapper objectMapper = new ObjectMapper();

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
        List<YikeNovelSubscribeAudit> subscribeAuditByUserIdAndNovelId = bookShelfMapper.getSubscribeAuditByUserIdAndNovelId(userID, novelId);

        if(subscribeAuditByUserIdAndNovelId != null && !subscribeAuditByUserIdAndNovelId.isEmpty()){
            bookShelfMapper.updateSubscribeAuditChapterId(userID,novelId,chapterId);
        }else{
            bookShelfMapper.readChapter(userID,novelId,chapterId);
        }
        RedisUtils.remove(RedisConstant.preUserId +  String.valueOf(userID));
        getBookShelfByUserId(userID);
    }
    @Override
    public void updateTopBook(int userID, int novelId) throws Exception {
        List<YikeNovelBookshelf> bookShelfByUserId = getBookShelfByUserId(userID);
        int isTopID = -1;
        for (YikeNovelBookshelf yikeNovelBookshelf : bookShelfByUserId) {
            if(yikeNovelBookshelf.getIsTop() == 1){
                isTopID = yikeNovelBookshelf.getNovelId();
                break;
            }
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

        RedisUtils.remove(RedisConstant.preUserId +  String.valueOf(userID));
        getBookShelfByUserId(userID);

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

        }

        RedisUtils.remove(RedisConstant.preUserId +  String.valueOf(userID));
        getBookShelfByUserId(userID);
    }


}
