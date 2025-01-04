package vip.xiaozhao.intern.baseUtil.intf.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import vip.xiaozhao.intern.baseUtil.intf.annotation.TargetDataSource;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelBookshelf;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelSubscribeAudit;

import java.util.List;

@Mapper
public interface BookShelfMapper {

    @TargetDataSource(name = "slave")
    public List<YikeNovelBookshelf> getBookShelfByUserId(int userId);

    @TargetDataSource(name = "master")
    public void readChapter(int userId,int novelId,int chapterId);

    @TargetDataSource(name = "master")
    public void updateTopBook(int userId,int novelId);

    @TargetDataSource(name = "master")
    public void deleteBookByUserIdAndNovelId(int userId,int novelId);

    @TargetDataSource(name = "master")
    public void subscribeBook(YikeNovelBookshelf yikeNovelBookshelf);

    @TargetDataSource(name = "master")
    public void updateIsReadByUserIdAndNovelId(int userId,int novelId);

    @TargetDataSource(name = "master")
    public void updateIsReadByNovelId(int NovelId,List<Integer> userIDs);

    @TargetDataSource(name = "slave")
    public List<YikeNovelSubscribeAudit> getSubscribeAuditByUserIdAndNovelId(int userId,int novelId);

    @TargetDataSource(name = "master")
    public void updateSubscribeAuditChapterId(int userId,int novelId,int chapterId);




}
