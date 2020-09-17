package com.paypay.baymax.front.hibernate;

import java.util.Properties;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.paypay.baymax.front.events.CustomHibernateEventAdapter;

@Configuration
@EnableTransactionManagement
public class HibernateConf {

	private DataSourceSQLProps dsMYSQLProps;
	private HibernateMYSQLProps hibernateMYSQLProps;

	@Autowired
	public HibernateConf(DataSourceSQLProps dsMSSQLProps, HibernateMYSQLProps hibernateMSSQLProps) {
		this.dsMYSQLProps = dsMSSQLProps;
		this.hibernateMYSQLProps = hibernateMSSQLProps;
	}

	@Bean
	public LocalSessionFactoryBean sessionFactoryMSSQL() {

		LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();

		sessionFactory.setDataSource(dataSourceMYSQL());
		sessionFactory.setPackagesToScan("com.paypay.baymax.domain");
		sessionFactory.setHibernateProperties(hibernatePropertiesMYSQL());

		return sessionFactory;
	}

	@Bean
	@Primary
	public BasicDataSource dataSourceMYSQL() {

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(dsMYSQLProps.getDriverClassName());
		dataSource.setUrl(dsMYSQLProps.getUrl());
		dataSource.setUsername(dsMYSQLProps.getUsername());
		dataSource.setPassword(dsMYSQLProps.getPassword());

		return dataSource;
	}

	@Bean
	public PlatformTransactionManager hibernateTransactionManagerMSSQL() {
		HibernateTransactionManager transactionManager = new HibernateTransactionManager();
		transactionManager.setSessionFactory(sessionFactoryMSSQL().getObject());
		return transactionManager;
	}

	@Bean
	public CustomHibernateEventAdapter customEventAdapter() {
		return new CustomHibernateEventAdapter();
	}

	private final Properties hibernatePropertiesMYSQL() {

		Properties hibernateProperties = new Properties();
		hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
		hibernateProperties.setProperty("hibernate.dialect", hibernateMYSQLProps.getHibernateDialect().trim());
		hibernateProperties.setProperty("spring.jpa.show-sql", hibernateMYSQLProps.getShowSQL());
		hibernateProperties.setProperty("spring.jpa.hibernate.naming.implicit-strategy",
				hibernateMYSQLProps.getImplicitNamingStrategy().trim());
		hibernateProperties.setProperty("spring.jpa.hibernate.naming.physical-strategy",
				hibernateMYSQLProps.getSpringPhysicalNamingStrategy().trim());

		return hibernateProperties;
	}

}
