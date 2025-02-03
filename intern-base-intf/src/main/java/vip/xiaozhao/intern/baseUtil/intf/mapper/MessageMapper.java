package vip.xiaozhao.intern.baseUtil.intf.mapper;

// 这个是将来处理消息的mapper对象，因为本期不做发送，所以这里面应该只有查询这个用户这本小说的消息发送时间，实体类也是查询发送时间

import vip.xiaozhao.intern.baseUtil.intf.entity.Message;

public interface MessageMapper {

    public Message getSendTime(int userId, int novelId, int chapterId);

    public Message getLeastSendTime(int userId, int novelId);

}
