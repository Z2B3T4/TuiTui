package vip.xiaozhao.intern.baseUtil.intf.entity;


import lombok.*;

import java.util.Date;

@Data                                   // Lombok 注解，生成 getter, setter, toString, equals, hashCode 等方法
@NoArgsConstructor                      // 无参构造函数
@AllArgsConstructor
@Getter
@Setter
public class NovelInfo {

    private int id;                  // 主键，自增ID

    private int authorId;             // 作者ID
    private String bookName;              // 书名
    private String authorName;            // 作者名
    private String description;            // 描述
    private int subscribeNum = 0;     // 订阅数量，默认为0
    private int weekSubNum = 0;       // 每周订阅数量，默认为0
    private int monthSubNum = 0;      // 每月订阅数量，默认为0
    private int searchNum = 0;        // 搜索数量，默认为0
    private String bookUrl;                // 书的URL
    private String cover;                  // 封面
    private Date lastUpdateTime;           // 最后更新时间
    private Date searchUpdateTime;         // 搜索更新时间
    private String regex;
    private String searchKey;              // 搜索关键字
    private int latestChapterId = 0;   // 最新章节ID，默认为0
    private String latestChapter;           // 最新章节
    private String chapterUrl;              // 章节URL
    private String searchLatestChapter;     // 搜索最新章节
    private int status;                    // 状态
    private Date addTime = new Date();      // 默认为当前时间
    private Date updateTime;                // 默认为当前时间
}
