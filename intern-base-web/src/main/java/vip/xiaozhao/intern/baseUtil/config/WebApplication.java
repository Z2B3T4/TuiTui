package vip.xiaozhao.intern.baseUtil.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vip.xiaozhao.intern.baseUtil.controller.interceptor.PrehandleInterceptor;

/**
 * 拦截器配置类
 */
@Configuration
public class WebApplication implements WebMvcConfigurer {
    @Autowired
    private PrehandleInterceptor prehandleInterceptor;

    /**
     * 注册拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(prehandleInterceptor);
        //拦截的 url
        registration.addPathPatterns("/**");
    }

    /**
     * 跨域白名单配置
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedMethods("*")
                .maxAge(3600)
                .allowedHeaders("*")
                .allowedOriginPatterns("*");
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
