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

    /*
    * 从配置文件中读取配置信息，动态配置
    * */

    // 配置主数据源
    @Bean(name = "masterDataSource")
    @Primary
    public DataSource masterDataSource() throws ClassNotFoundException {
        DataSourceProperties.DataSourceConfig masterConfig = dataSourceProperties.getMaster();

        // 显式指定数据源的类类型
        Class<? extends DataSource> dataSourceClass;

        try {
            // 确保这里使用 DruidDataSource
            dataSourceClass = (Class<? extends DataSource>) Class.forName(masterConfig.getType());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Data source class not found: " + masterConfig.getType(), e);
        }

        return DataSourceBuilder.create()
                .url(masterConfig.getUrl())
                .username(masterConfig.getUsername())
                .password(masterConfig.getPassword())
                .driverClassName(masterConfig.getDriverClassName())
                .type(dataSourceClass) // 使用从配置中读取的类型
                .build();

    }

    // 配置从数据源
    @Bean
    public DynamicDataSource dataSource(@Autowired DataSource masterDataSource) throws ClassNotFoundException {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", masterDataSource);
        List<DataSourceProperties.SlaveDataSourceConfig> slaveConfigs = dataSourceProperties.getSlaves();
        for (int i = 0; i < slaveConfigs.size(); i++) {
            DataSourceProperties.SlaveDataSourceConfig slaveConfig = slaveConfigs.get(i);
            // 显式指定数据源的类类型
            Class<? extends DataSource> dataSourceClass;
            try {
                // 确保这里使用 DruidDataSource
                dataSourceClass = (Class<? extends DataSource>) Class.forName(slaveConfig.getType());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Data source class not found: " + slaveConfig.getType(), e);
            }

            if(slaveConfig.getName() == null){
                throw new RuntimeException("从数据源" + slaveConfig.getUrl() + "未配置名称");
            }
            DataSource slaveDataSource = DataSourceBuilder.create()
                    .url(slaveConfig.getUrl())
                    .username(slaveConfig.getUsername())
                    .password(slaveConfig.getPassword())
                    .driverClassName(slaveConfig.getDriverClassName())
                    .type(dataSourceClass)
                    .build();
            targetDataSources.put(slaveConfig.getName(), slaveDataSource);
        }

        return new DynamicDataSource(masterDataSource, targetDataSources);
    }
}
