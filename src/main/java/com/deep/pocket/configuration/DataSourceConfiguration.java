package com.deep.pocket.configuration;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataSourceConfiguration {

    @Value("${pocket.db.username}")
    private String pocketDbUsername;

    @Value("${pocket.db.password}")
    private String pocketDbPassword;

    @Value("${pocket.db.url}")
    private String pocketDbUrl;

//    @Value("${order.db.maxActive:100}")
//    private int maxActive;
//
//    @Value("${order.db.maxIdle:100}")
//    private int maxIdle;
//
//    @Value("${order.db.minIdle:10}")
//    private int minIdle;
//
//    @Value("${order.db.initialSize:10}")
//    private int initialSize;
//
//    @Value("${order.db.maxWait:30000}")
//    private int maxWait;

    @Bean
    public DataSource dataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(pocketDbUrl);
        dataSource.setUsername(pocketDbUsername);
        dataSource.setPassword(pocketDbPassword);
//        dataSource.setMaxActive(maxActive);
//        dataSource.setMaxIdle(maxIdle);
//        dataSource.setMinIdle(minIdle);
//        dataSource.setInitialSize(initialSize);
//        dataSource.setMaxWait(maxWait);
        dataSource.setValidationQuery("SELECT 1");
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource());
        return jdbcTemplate;
    }
}
