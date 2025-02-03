package vip.xiaozhao.intern.baseUtil.intf.mapper;


import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelSubscribeAudit;

import java.util.List;

public interface MessageLevelMapper {

    public int getSendLevelByUserIdAndNovelId(int userId,int novelId);

    public void updateSendLevelByUserIdAndNovelId(int sendLevel,int userId,int novelId);

    public List<YikeNovelSubscribeAudit> getYikeNovelSubscribeAuditByUserIdAndNovelId(int userId, int novelId);


}
