package vip.xiaozhao.intern.baseUtil.intf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

// 读取配置文件中的数据映射
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {

    private DataSourceConfig master;
    private List<SlaveDataSourceConfig> slaves;

    public DataSourceConfig getMaster() {
        return master;
    }

    public void setMaster(DataSourceConfig master) {
        this.master = master;
    }

    public List<SlaveDataSourceConfig> getSlaves() {
        return slaves;
    }

    public void setSlaves(List<SlaveDataSourceConfig> slaves) {
        this.slaves = slaves;
    }

    // 内部静态类，用来映射主数据源配置
    public static class DataSourceConfig {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private String type;

        // getters and setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    // 用来映射从数据源配置的类
    public static class SlaveDataSourceConfig {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private String type;
        private String name;

        // getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
