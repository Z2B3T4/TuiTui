package vip.xiaozhao.intern.baseUtil.intf.mapper;

import vip.xiaozhao.intern.baseUtil.intf.DO.NovelDO;
import vip.xiaozhao.intern.baseUtil.intf.annotation.TargetDataSource;
import vip.xiaozhao.intern.baseUtil.intf.DO.HotNovelDO;

import java.util.List;

public interface SearchBookMapper {

    @TargetDataSource(name = "slave")
    public List<NovelDO> searchNovelList(int start,int pageSize,String query);

    @TargetDataSource(name = "master")
    public void incrementNovelSearchNum(int novellId);

    @TargetDataSource(name = "master")
    public void inserIntoHotBook(int id,String bookName);

    @TargetDataSource(name = "slave")
    public HotNovelDO getHotNovelByNovelId(int novelId);

    @TargetDataSource(name = "slave")
    public List<HotNovelDO> getHotNovelList();

}
