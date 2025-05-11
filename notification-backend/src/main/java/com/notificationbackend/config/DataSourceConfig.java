package com.notificationbackend.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.notification.common.db.CustomJpaVendor;
import com.notification.common.db.DatasourceRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("file:/Users/harsh.shukla/notification-service-properties/db.properties")
public class DataSourceConfig {

    private static final Logger logger = LogManager.getLogger(DataSourceConfig.class);

    @Bean(name = "masterDataSource", destroyMethod = "close")
    @Scope("singleton")
    @Primary
    public DataSource masterDataSource(
            @Value("${driver}") String driverClass,
            @Value("${url}") String jdbcUrl,
            @Value("${userName}") String username,
            @Value("${password}") String password)
            throws PropertyVetoException {

        logger.info("username is" + username);
        logger.info("password is" + password);
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(driverClass);
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setMinPoolSize(10);
        dataSource.setMaxPoolSize(40);
        dataSource.setMaxIdleTime(5);
        dataSource.setAutoCommitOnClose(false);
        return dataSource;
    }

    @Bean(name = "slaveDataSource", destroyMethod = "close")
    @Scope("singleton")
    public DataSource slaveDataSource(
            @Value("${driver}") String driverClass,
            @Value("${slave_url}") String jdbcUrl,
            @Value("${userName}") String username,
            @Value("${password}") String password)
            throws PropertyVetoException {

        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(driverClass);
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setMinPoolSize(10);
        dataSource.setMaxPoolSize(40);
        dataSource.setMaxIdleTime(5);
        dataSource.setAutoCommitOnClose(false);
        return dataSource;
    }

    @Bean
    public DatasourceRouter dataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") DataSource slaveDataSource) {

        DatasourceRouter datasourceRouter = new DatasourceRouter();

        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put("WRITE", masterDataSource);
        dataSources.put("READ", slaveDataSource);

        datasourceRouter.setDefaultTargetDataSource(masterDataSource);
        datasourceRouter.setTargetDataSources(dataSources);

        return datasourceRouter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("dataSource") DatasourceRouter dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.notificationbackend.model");
        em.setJpaVendorAdapter(new CustomJpaVendor());
        em.setPersistenceUnitName("default");
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
