package com.paypay.baymax.front.hibernate;

import org.hibernate.envers.boot.internal.EnversServiceImpl;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateEventAdapter extends EnversServiceImpl
		implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener, PreInsertEventListener {

	private static final long serialVersionUID = 3811536912139881296L;
	public final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void onPostDelete(PostDeleteEvent postDeleteEvent) {
	}

	@Override
	public void onPostInsert(PostInsertEvent postInsertEvent) {
	}

	@Override
	public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
		return true;
	}

	@Override
	public boolean onPreInsert(PreInsertEvent event) {
		return false;
	}

}
