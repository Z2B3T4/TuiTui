package vip.xiaozhao.intern.baseUtil.intf.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfig {

    @Autowired
    private DataSourceProperties dataSourceProperties;

    // 配置主数据源
    @Bean(name = "masterDataSource")
    @Primary
    public DataSource masterDataSource() {
        DataSourceProperties.DataSourceConfig masterConfig = dataSourceProperties.getMaster();
        return DataSourceBuilder.create()
                .url(masterConfig.getUrl())
                .username(masterConfig.getUsername())
                .password(masterConfig.getPassword())
                .driverClassName(masterConfig.getDriverClassName())
                .type(DruidDataSource.class)
                .build();
    }

    // 配置从数据源
    @Bean
    public DynamicDataSource dataSource(@Autowired DataSource masterDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", masterDataSource);

        List<DataSourceProperties.SlaveDataSourceConfig> slaveConfigs = dataSourceProperties.getSlaves();
        for (int i = 0; i < slaveConfigs.size(); i++) {
            DataSourceProperties.SlaveDataSourceConfig slaveConfig = slaveConfigs.get(i);
            if(slaveConfig.getName() == null){
                throw new RuntimeException("从数据源" + slaveConfig.getUrl() + "未配置名称");
            }
            DataSource slaveDataSource = DataSourceBuilder.create()
                    .url(slaveConfig.getUrl())
                    .username(slaveConfig.getUsername())
                    .password(slaveConfig.getPassword())
                    .driverClassName(slaveConfig.getDriverClassName())
                    .type(DruidDataSource.class)
                    .build();
            targetDataSources.put(slaveConfig.getName(), slaveDataSource);
        }

        return new DynamicDataSource(masterDataSource, targetDataSources);
    }
}
