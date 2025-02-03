package vip.xiaozhao.intern.baseUtil.intf.entity;

import lombok.*;

import java.util.Date;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private int id;
    private int userId;
    private int novelId;
    private int chapterId;
    private Date sendTime;
    private int status;
}
