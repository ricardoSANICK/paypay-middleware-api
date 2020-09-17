package com.paypay.baymax.front.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.event.spi.EnversListenerDuplicationStrategy;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.stereotype.Component;

import com.paypay.baymax.front.events.CustomHibernateEventAdapter;

import javax.annotation.PostConstruct;

@Component
public class HibernateConfigurer {


  private SessionFactory sessionFactory;
  private CustomHibernateEventAdapter customHibernateEventAdapter;

  public HibernateConfigurer(SessionFactory sessionFactory, CustomHibernateEventAdapter customHibernateEventAdapter){
    this.sessionFactory = sessionFactory;
    this.customHibernateEventAdapter = customHibernateEventAdapter;
  }

  @PostConstruct
  public void registerListeners(){

    SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;

    EventListenerRegistry eventListenerRegistry = sessionFactoryImpl.getServiceRegistry().getService(EventListenerRegistry.class);

    eventListenerRegistry.addDuplicationStrategy(EnversListenerDuplicationStrategy.INSTANCE );

    EnversService enversService = sessionFactoryImpl.getServiceRegistry().getService(EnversService.class);
    if (!enversService.isInitialized()) {
      throw new HibernateException("Expecting EnversService to have been initialized prior to call to EnversIntegrator#integrate");
    }

    if(enversService.getEntitiesConfigurations().hasAuditedEntities()) {
      eventListenerRegistry.appendListeners( EventType.POST_UPDATE, customHibernateEventAdapter );
      eventListenerRegistry.appendListeners( EventType.POST_INSERT, customHibernateEventAdapter );
      eventListenerRegistry.appendListeners( EventType.POST_DELETE, customHibernateEventAdapter );
      eventListenerRegistry.appendListeners( EventType.PRE_INSERT, customHibernateEventAdapter );
    }

  }
}