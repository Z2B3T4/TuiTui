package vip.xiaozhao.intern.baseUtil.intf.service;

import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelBookshelf;

import java.util.List;

public interface BookShelfService {

    public List<YikeNovelBookshelf> getBookShelfByUserId(int userId) throws Exception;

    public void readChapter(int userId,int novelId,int chapterId) throws Exception;

    public void updateTopBook(int userID,int novelId) throws Exception;

    public void deleteBookByUserIdAndNovelId(int userID,int novelId) throws Exception;

    public void subscribeNovel(int userID,int novelId) throws Exception;

    public void updateIsReadByUserIdAndNovelId(int userId,int novelId) throws Exception;

    public void updateIsReadByNovelId(int userId,int NovelId) throws Exception;

    public void updateIsReadByNovelIdList(int userID,List<Integer> novelIds) throws Exception;



}
