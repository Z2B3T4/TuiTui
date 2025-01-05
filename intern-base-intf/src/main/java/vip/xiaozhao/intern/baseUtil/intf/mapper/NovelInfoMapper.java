package vip.xiaozhao.intern.baseUtil.intf.mapper;


import vip.xiaozhao.intern.baseUtil.intf.annotation.SlaveDataSource;
import vip.xiaozhao.intern.baseUtil.intf.entity.NovelInfo;

public interface NovelInfoMapper {
    /*
       加了 @SlaveDataSource 是从库读取，可以指定从库的名称，没加的是默认主库操作
       或者加了名称是master的都是从主库读取
    */
    @SlaveDataSource(name = "slave0")
    public NovelInfo getNovelInfoByNovelId(int novelId);
}
