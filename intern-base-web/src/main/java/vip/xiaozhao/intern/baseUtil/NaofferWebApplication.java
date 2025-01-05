package vip.xiaozhao.intern.baseUtil;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.PropertySource;


@SpringBootApplication(scanBasePackages = {"vip.xiaozhao.intern.baseUtil"})
@MapperScan("vip.xiaozhao.intern.baseUtil.intf.mapper")
@PropertySource("classpath:application.properties")
public class NaofferWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(NaofferWebApplication.class, args);
    }

}
