package vip.xiaozhao.intern.baseUtil.intf.service;

import vip.xiaozhao.intern.baseUtil.intf.entity.NovelInfo;
import vip.xiaozhao.intern.baseUtil.intf.mapper.NovelInfoMapper;

public interface NovelInfoService {

    public NovelInfo getNovelInfoByNovelId(int novelId);
}
