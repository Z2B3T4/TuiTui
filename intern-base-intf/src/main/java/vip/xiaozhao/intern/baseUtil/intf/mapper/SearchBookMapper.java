package vip.xiaozhao.intern.baseUtil.intf.mapper;

import vip.xiaozhao.intern.baseUtil.intf.DO.NovelDO;
import vip.xiaozhao.intern.baseUtil.intf.annotation.ReadOnly;

import vip.xiaozhao.intern.baseUtil.intf.DO.HotNovelDO;

import java.util.List;

public interface SearchBookMapper {
    /*
    这个加了 @ReadOnly 是从库读取，没加的是默认主库操作
     */
    @ReadOnly
    public List<NovelDO> searchNovelList(int start,int pageSize,String query);

    public void incrementNovelSearchNum(int novellId);

    public void inserIntoHotBook(int id,String bookName);
    /*
    这个加了 @ReadOnly 是从库读取，没加的是默认主库操作
     */
    @ReadOnly
    public HotNovelDO getHotNovelByNovelId(int novelId);

    public List<HotNovelDO> getHotNovelList();

}
