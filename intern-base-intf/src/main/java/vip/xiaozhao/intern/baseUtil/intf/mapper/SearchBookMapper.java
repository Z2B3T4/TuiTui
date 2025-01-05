package vip.xiaozhao.intern.baseUtil.intf.mapper;

import vip.xiaozhao.intern.baseUtil.intf.DO.NovelDO;


import vip.xiaozhao.intern.baseUtil.intf.DO.HotNovelDO;
import vip.xiaozhao.intern.baseUtil.intf.annotation.SlaveDataSource;

import java.util.List;

public interface SearchBookMapper {
    /*
       加了 @SlaveDataSource 是从库读取，可以指定从库的名称，没加的是默认主库操作
       或者加了名称是master的都是从主库读取
    */
    @SlaveDataSource(name = "slave0")
    public List<NovelDO> searchNovelList(int start,int pageSize,String query);

    public void incrementNovelSearchNum(int novellId);

    public void inserIntoHotBook(int id,String bookName);
    /*
       加了 @SlaveDataSource 是从库读取，可以指定从库的名称，没加的是默认主库操作
       或者加了名称是master的都是从主库读取
    */
    @SlaveDataSource(name = "slave0")
    public HotNovelDO getHotNovelByNovelId(int novelId);

    public List<HotNovelDO> getHotNovelList();

}
