package com.jfeat.am.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.enums.DBType;
import com.baomidou.mybatisplus.plugins.PaginationInterceptor;
import com.jfeat.am.config.properties.DruidProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlus配置
 *
 * @author Admin
 * @Date 2017/5/20 21:58
 */
@Configuration
@MapperScan(basePackages = {"com.jfeat.**.dao", "com.jfeat.**.mapper"})
public class MybatisPlusConfig {

    @Autowired
    DruidProperties druidProperties;

    /**
     * mybatis-plus分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        String driverClassName = this.druidProperties.getDriverClassName();
        if(driverClassName != null && driverClassName.contains("mysql")){
            paginationInterceptor.setDialectType(DBType.MYSQL.getDb());
        }else if(driverClassName != null && driverClassName.contains("sqlserver")){
            paginationInterceptor.setDialectType(DBType.SQLSERVER2005.getDb());
        }else if (driverClassName != null && driverClassName.contains("h2")) {
            paginationInterceptor.setDialectType(DBType.H2.getDb());
        }

        return paginationInterceptor;
    }

    /**
     * druid数据库连接池
     */
    @Bean(initMethod = "init")
    public DruidDataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        druidProperties.config(dataSource);
        return dataSource;
    }
}
