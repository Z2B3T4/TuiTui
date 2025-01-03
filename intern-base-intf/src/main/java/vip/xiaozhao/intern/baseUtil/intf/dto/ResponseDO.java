package vip.xiaozhao.intern.baseUtil.intf.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/****
 * 所有ajax接口的返回对象
 */
@ToString
@Setter
@Getter
public class ResponseDO {

    private boolean success;
    private String msg;
   // private int errorCode;
    private Object data;

    public ResponseDO() {
    }

    public ResponseDO(boolean success, String msg,  Object data) {
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    public static  ResponseDO success(Object data){
        return new ResponseDO(true, "success", data);
    }

    public static  ResponseDO fail(String msg){
        return new ResponseDO(false, msg, null);
    }

    public static  ResponseDO fail(String msg,  Object data){
        return new ResponseDO(false, msg, data);
    }


}
