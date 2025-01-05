package vip.xiaozhao.intern.baseUtil.intf.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import vip.xiaozhao.intern.baseUtil.intf.annotation.SlaveDataSource;
import vip.xiaozhao.intern.baseUtil.intf.config.DynamicDataSource;


import java.lang.reflect.Method;
import java.util.Random;

@Aspect
@Component
public class DataSourceAspect {

    @Before("execution(* vip.xiaozhao.intern.baseUtil.intf.mapper.*.*(..))")
    public void setDataSource(JoinPoint joinPoint) {
        // 判断方法是否有 @ReadOnly 注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        if (method.isAnnotationPresent(SlaveDataSource.class)) {
            // 获取 @ReadOnly 注解的 name 属性值
            SlaveDataSource readOnlyAnnotation = method.getAnnotation(SlaveDataSource.class);
            String selectedSlave = readOnlyAnnotation.name();

            System.out.println("Setting to read-only slave: " + selectedSlave);
            DynamicDataSource.setDataSourceType(selectedSlave);
        } else {
            // 切换到主库
            System.out.println("Setting to master data source");
            DynamicDataSource.setDataSourceType("master");
        }
    }

    @After("execution(* vip.xiaozhao.intern.baseUtil.intf.mapper.*.*(..))")
    public void clearDataSource(JoinPoint joinPoint) {
        DynamicDataSource.clearDataSourceType();
    }

}
