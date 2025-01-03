package vip.xiaozhao.intern.baseUtil.controller.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import vip.xiaozhao.intern.baseUtil.controller.util.IPUtil;
import vip.xiaozhao.intern.baseUtil.intf.constant.CommonConstant;
import vip.xiaozhao.intern.baseUtil.intf.utils.JjwtUtil;

/**
 * 全局预处理拦截器
 */
@Component
@Slf4j
public class PrehandleInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object object) throws Exception {
        return isSecurity(request, response);
    }

    @Override
    public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
                           Object arg2, ModelAndView arg3) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest arg0,
                                HttpServletResponse arg1, Object arg2, Exception arg3)
            throws Exception {
    }

    /**
     * 是否安全的请求
     *
     * @param request
     * @param response
     * @return
     */
    private Boolean isSecurity(HttpServletRequest request, HttpServletResponse response) {
        String requestIP = IPUtil.getRequestIP(request);

        String token = request.getHeader(CommonConstant.LOGIN_HEAD_KEY);
        int currentUserId = 5;
        //int currentUserId = JjwtUtil.verifyLoginToken(token);

        if (currentUserId < 0) {//非法请求

            if (isInWhiteSheet(request.getRequestURI())) {
                log.warn("白名单请求，path：" + request.getRequestURI() + "; ip：" + requestIP);
                currentUserId = 0;
            } else {
                log.warn("非法请求，path：" + request.getRequestURI() + "; ip：" + requestIP);
                return false;
            }
        }

        log.info(request.getRequestURI() + "; useId=" + currentUserId + "; ip=" + IPUtil.getRequestIP(request));
        request.setAttribute(CommonConstant.LOGIN_USERID_KEY, currentUserId);

        return true;
    }

    private boolean isInWhiteSheet(String url) {
        //这部分要加内网ip权限， 外网也可以访问
        String[] strings = {"/hello","code/generate"};
        for (String s : strings) {
            if (url.contains(s)) {
                log.info("white sheet，in ：" + url);
                return true;
            }
        }
        return false;
    }

}
