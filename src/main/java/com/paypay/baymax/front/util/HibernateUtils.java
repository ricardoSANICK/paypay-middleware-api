package com.paypay.baymax.front.util;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtils {
	
	private static Logger log = LoggerFactory.getLogger(HibernateUtils.class);
	private static SessionFactory sessionFactory;
	
	public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    @SuppressWarnings("static-access")
	public void setSessionFactory(SessionFactory sessionFactory) {
    	if (this.sessionFactory == null){
    		this.sessionFactory = sessionFactory;
    	}
    	else{
    		log.debug("No es posible sobreescribir la session de Hibernate.");
    	}
    }

}
