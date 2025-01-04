package vip.xiaozhao.intern.baseUtil.intf.mapper;

import vip.xiaozhao.intern.baseUtil.intf.annotation.TargetDataSource;
import vip.xiaozhao.intern.baseUtil.intf.entity.NovelInfo;

public interface NovelInfoMapper {

    @TargetDataSource(name = "slave")
    public NovelInfo getNovelInfoByNovelId(int novelId);
}
