package vip.xiaozhao.intern.baseUtil.intf.entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

import java.util.Date;

@Data
public class YikeNovelBookshelf {

    private int id;                  // 主键
    private int userId;              // 用户ID
    private int novelId;             // 小说ID
    private int latestChapterId;     // 最新章节ID
    private String bookName;         // 书名
    private String authorName;       // 作者名
    private String latestChapter;     // 小说最新章节

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;     // 小说最新更新时间

    private String coverUrl;         // 小说封面图片URL
    private int status;              // 状态: 0-异常, 1-未完结, 2-已完结
    private int isRead;              // 是否已读: 0-未读, 1-已读
    private int isTop;               // 是否置顶: 0-否, 1-是

/*
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonIgnore
    private Date addTime;            // 添加时间

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonIgnore
    private Date updateTime;         // 更新时间

 */
}
