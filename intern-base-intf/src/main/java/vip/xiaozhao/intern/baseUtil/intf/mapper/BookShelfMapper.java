package vip.xiaozhao.intern.baseUtil.intf.mapper;

import org.apache.ibatis.annotations.Mapper;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelBookshelf;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelSubscribeAudit;

import java.util.List;

@Mapper
public interface BookShelfMapper {
    public List<YikeNovelBookshelf> getBookShelfByUserId(int userId);

    public void readChapter(int userId,int novelId,int chapterId);

    public void updateTopBook(int userId,int novelId);

    public void deleteBookByUserIdAndNovelId(int userId,int novelId);

    public void subscribeBook(YikeNovelBookshelf yikeNovelBookshelf);

    public void updateIsReadByUserIdAndNovelId(int userId,int novelId);

    public void updateIsReadByNovelId(int NovelId,List<Integer> userIDs);


    public List<YikeNovelSubscribeAudit> getSubscribeAuditByUserIdAndNovelId(int userId,int novelId);

    public void updateSubscribeAuditChapterId(int userId,int novelId,int chapterId);




}
