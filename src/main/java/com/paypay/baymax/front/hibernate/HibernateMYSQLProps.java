package com.paypay.baymax.front.hibernate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HibernateMYSQLProps {
	
	@Value("${com.paypay.baymax.config.mssql.jpa.show-sql}")
	private String showSQL;
	
	@Value("${com.paypay.baymax.config.mssql.jpa.hibernate.dll-auto}")
	private String dllAuto;
	
	@Value("${com.paypay.baymax.config.mssql.jpa.hibernate.naming.implicit-strategy}")
	private String ImplicitNamingStrategy;
	
	@Value("${com.paypay.baymax.config.mssql.jpa.hibernate.naming.physical-strategy}")
	private String SpringPhysicalNamingStrategy;
	
	@Value("${com.paypay.baymax.config.mssql.jpa.properties.hibernate.dialect}")
	private String hibernateDialect;
	
	@Value("${com.paypay.baymax.config.mssql.jpa.hibernate.default_schema}")
	private String hibernateDefaultSchema;
		
	
	public HibernateMYSQLProps() {
		super();
	}

	public String getShowSQL() {
		return showSQL;
	}

	public void setShowSQL(String showSQL) {
		this.showSQL = showSQL;
	}

	public String getDllAuto() {
		return dllAuto;
	}

	public void setDllAuto(String dllAuto) {
		this.dllAuto = dllAuto;
	}

	public String getImplicitNamingStrategy() {
		return ImplicitNamingStrategy;
	}

	public void setImplicitNamingStrategy(String implicitNamingStrategy) {
		ImplicitNamingStrategy = implicitNamingStrategy;
	}

	public String getSpringPhysicalNamingStrategy() {
		return SpringPhysicalNamingStrategy;
	}

	public void setSpringPhysicalNamingStrategy(String springPhysicalNamingStrategy) {
		SpringPhysicalNamingStrategy = springPhysicalNamingStrategy;
	}

	public String getHibernateDialect() {
		return hibernateDialect;
	}

	public void setHibernateDialect(String hibernateDialect) {
		this.hibernateDialect = hibernateDialect;
	}

	public String getHibernateDefaultSchema() {
		return hibernateDefaultSchema;
	}

	public void setHibernateDefaultSchema(String hibernateDefaultSchema) {
		this.hibernateDefaultSchema = hibernateDefaultSchema;
	}

}
