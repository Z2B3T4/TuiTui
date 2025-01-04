package vip.xiaozhao.intern.baseUtil.intf.service;

import vip.xiaozhao.intern.baseUtil.intf.DO.NovelDO;
import vip.xiaozhao.intern.baseUtil.intf.DO.HotNovelDO;

import java.util.List;

public interface SearchBookService {

    public List<NovelDO> searchNovelList(int start,int pageSize,String query);

    public void incrementNovelSearchNum(int novelId);

    public void inserIntoHotBook(int id,String bookName);

    public HotNovelDO getHotNovelByNovelId(int novelId);

    public List<HotNovelDO> getHotNovelList();

}
