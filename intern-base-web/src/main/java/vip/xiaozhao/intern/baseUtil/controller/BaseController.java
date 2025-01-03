package vip.xiaozhao.intern.baseUtil.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import vip.xiaozhao.intern.baseUtil.intf.constant.CommonConstant;


public class BaseController {

    protected static final String SUCCESS = "success";
    protected static final String FAIL = "fail";
    protected static final int SUCCESS_ID = 1;
    protected static final int FAIL_ID = 0;
    protected static final String NOT_LOGIN = "not_login";

    @Value("${home.url}")
    protected String PreFix;

    protected int getCurrentUserId(HttpServletRequest request){
        Object uId = request.getAttribute(CommonConstant.LOGIN_USERID_KEY);
        if (uId == null){
            return -1;
        }else {
            return NumberUtils.toInt(uId.toString());
        }
    }


}
