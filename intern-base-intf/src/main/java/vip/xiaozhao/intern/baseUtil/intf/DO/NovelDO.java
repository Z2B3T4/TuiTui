package vip.xiaozhao.intern.baseUtil.intf.DO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
public class NovelDO {

    private int id;                  // 主键，自增ID
    private int authorId;             // 作者ID
    private String bookName;              // 书名
    private String authorName;            // 作者名
    private String description;            // 描述
    private String bookUrl;                // 书的URL
    private String cover;                  // 封面
    private int status;                    // 状态

}
