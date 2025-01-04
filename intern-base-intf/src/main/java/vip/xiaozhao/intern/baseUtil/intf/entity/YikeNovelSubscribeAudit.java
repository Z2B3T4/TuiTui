package vip.xiaozhao.intern.baseUtil.intf.entity;


import lombok.Data;
import java.util.Date;


@Data
public class YikeNovelSubscribeAudit {

    private int id;           // 主键
    private int userId;      // 用户ID
    private int novelId;     // 小说ID
    private int chapterId;   // 最新章节ID
    private Date addTime;        // 添加时间
}
