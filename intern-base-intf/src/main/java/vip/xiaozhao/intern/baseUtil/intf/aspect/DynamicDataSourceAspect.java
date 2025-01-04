package vip.xiaozhao.intern.baseUtil.intf.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import vip.xiaozhao.intern.baseUtil.intf.annotation.TargetDataSource;
import vip.xiaozhao.intern.baseUtil.intf.config.DynamicDataSource;

import java.lang.reflect.Method;

@Aspect
@Component
public class DynamicDataSourceAspect {
    @Before("@annotation(targetDataSource)")
    public void switchDataSource(JoinPoint point,TargetDataSource targetDataSource) {
        // 获取方法上的注解
        MethodSignature signature = (MethodSignature) point.getSignature();
             Method method = signature.getMethod();
             TargetDataSource ds = method.getAnnotation(TargetDataSource.class);
        if (ds != null) {
            // 根据注解值设置数据源
            System.out.println("使用的数据源是："+ds.name()+"\n");
            DynamicDataSource.setDataSourceType(ds.name());
        } else {
            // 如果没有注解，默认使用主库
            DynamicDataSource.setDataSourceType("master");
        }
    }
 
    @After("@annotation(targetDataSource)")
    public void restoreDataSource(JoinPoint point,TargetDataSource targetDataSource) {
        DynamicDataSource.clearDataSourceType();
    }
}
