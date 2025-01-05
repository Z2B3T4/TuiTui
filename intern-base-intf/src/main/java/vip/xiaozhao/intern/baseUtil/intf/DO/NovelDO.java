package vip.xiaozhao.intern.baseUtil.intf.DO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
public class NovelDO {

    private int id;                  // 主键，自增ID
    private String bookName;              // 书名
    private String authorName;            // 作者名
    private String desc;            // 描述
    private String cover;                  // 封面
    private int status;                    // 状态
    private int subscribeNum = 0;     // 订阅数量，默认为0
    private int latestChapterId = 0;   // 最新章节ID，默认为0
    private String latestChapter;           // 最新章节
    private String chapterUrl;              // 章节URL

}
