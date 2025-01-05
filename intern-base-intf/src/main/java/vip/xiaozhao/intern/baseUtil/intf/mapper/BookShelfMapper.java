package vip.xiaozhao.intern.baseUtil.intf.mapper;

import org.apache.ibatis.annotations.Mapper;


import vip.xiaozhao.intern.baseUtil.intf.annotation.SlaveDataSource;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelBookshelf;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelSubscribeAudit;

import java.util.List;

@Mapper
public interface BookShelfMapper {
    /*
       加了 @SlaveDataSource 是从库读取，可以指定从库的名称，没加的是默认主库操作
       或者加了名称是master的都是从主库读取
    */
    @SlaveDataSource(name = "slave0")
    public List<YikeNovelBookshelf> getBookShelfByUserId(int userId);

    public void readChapter(int userId,int novelId,int chapterId);

    public void updateTopBook(int userId,int novelId);

    public void deleteBookByUserIdAndNovelId(int userId,int novelId);

    public void subscribeBook(YikeNovelBookshelf yikeNovelBookshelf);

    public void updateIsReadByUserIdAndNovelId(int userId,int novelId);

    public void updateIsReadByNovelId(int NovelId,List<Integer> userIDs);
    /*
       加了 @SlaveDataSource 是从库读取，可以指定从库的名称，没加的是默认主库操作
       或者加了名称是master的都是从主库读取
    */
    @SlaveDataSource(name = "slave0")
    public List<YikeNovelSubscribeAudit> getSubscribeAuditByUserIdAndNovelId(int userId,int novelId);

    public void updateSubscribeAuditChapterId(int userId,int novelId,int chapterId);




}
