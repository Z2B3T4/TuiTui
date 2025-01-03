package vip.xiaozhao.intern.baseUtil;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("vip.xiaozhao.intern.baseUtil.intf.mapper")
public class NaofferWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(NaofferWebApplication.class, args);
    }

}
