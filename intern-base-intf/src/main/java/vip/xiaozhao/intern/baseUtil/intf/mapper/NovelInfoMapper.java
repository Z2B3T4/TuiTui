package vip.xiaozhao.intern.baseUtil.intf.mapper;

import vip.xiaozhao.intern.baseUtil.intf.annotation.ReadOnly;

import vip.xiaozhao.intern.baseUtil.intf.entity.NovelInfo;

public interface NovelInfoMapper {
    /*
    这个加了 @ReadOnly 是从库读取，没加的是默认主库操作
     */
    @ReadOnly
    public NovelInfo getNovelInfoByNovelId(int novelId);
}
