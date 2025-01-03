package vip.xiaozhao.intern.baseUtil.service.Impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaozhao.intern.baseUtil.intf.entity.NovelInfo;
import vip.xiaozhao.intern.baseUtil.intf.mapper.NovelInfoMapper;
import vip.xiaozhao.intern.baseUtil.intf.service.NovelInfoService;

@Service
public class NovelInfoServiceImpl implements NovelInfoService {

    @Resource
    private NovelInfoMapper novelInfoMapper;
    @Override
    public NovelInfo getNovelInfoByNovelId(int novelId) {
        NovelInfo novelInfoByNovelId = novelInfoMapper.getNovelInfoByNovelId(novelId);
        return novelInfoByNovelId;
    }

}
