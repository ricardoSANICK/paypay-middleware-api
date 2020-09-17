package com.paypay.baymax.front.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

@Configuration
public class AuthenticationProviderConfig {

	@Value("${com.paypay.baymax.config.mssql.datasource.driverClassName}")
	private String driverClassName;

	@Value("${com.paypay.baymax.config.mssql.datasource.url}")
	private String dbUrl;

	@Value("${com.paypay.baymax.config.mssql.datasource.username}")
	private String dbUsername;

	@Value("${com.paypay.baymax.config.mssql.datasource.password}")
	private String dbPassword;

	@Bean(name = "dataSource")
	public DriverManagerDataSource dataSource() {
		DriverManagerDataSource dmds = new DriverManagerDataSource();
		dmds.setDriverClassName(driverClassName);
		dmds.setUrl(dbUrl);
		dmds.setUsername(dbUsername);
		dmds.setPassword(dbPassword);
		return dmds;
	}

	@Bean
	public UserDetailsService userDetailsService() {
		JdbcDaoImpl jdbcImpl = new JdbcDaoImpl();
		jdbcImpl.setEnableGroups(true);
		jdbcImpl.setEnableAuthorities(false);
		jdbcImpl.setDataSource(dataSource());
		return jdbcImpl;
	}

}
