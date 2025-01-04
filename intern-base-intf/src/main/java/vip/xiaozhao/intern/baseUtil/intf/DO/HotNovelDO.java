package vip.xiaozhao.intern.baseUtil.intf.DO;

import lombok.Data;

@Data
public class HotNovelDO
{

    private int id;                  // 主键，自增ID
    private int bookId;             // 作者ID
    private String bookName;              // 书名
    private int times;
    private int status;

}
