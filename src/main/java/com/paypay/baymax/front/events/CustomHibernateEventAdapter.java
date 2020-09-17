package com.paypay.baymax.front.events;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.event.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paypay.baymax.front.hibernate.HibernateEventAdapter;

public class CustomHibernateEventAdapter extends HibernateEventAdapter {

	public final Logger log = LoggerFactory.getLogger(this.getClass());

	public CustomHibernateEventAdapter() {
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
//      if(isEntityCandidateForPreSet(event.getEntity())) {
//          event.getSession().getActionQueue().registerProcess(
//                  (b, sharedSessionContractImplementor) -> this.setPreset ("postDelete", event.getEntity())
//          );
//      }
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {

//      if(isEntityCandidateForPreSet(event.getEntity())) {
//          event.getSession().getActionQueue().registerProcess(
//                  (b, sharedSessionContractImplementor) -> this.updatePicturePreset("postUpdate", event.getEntity())
//          );
//      }
		// combosService.refreshCacheOfCurrentEventEntity(event.getEntity().getClass().getSimpleName());
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
//      if(isEntityCandidateForPreSet(event.getEntity())) {
//          event.getSession().getActionQueue().registerProcess(
//                  (b, sharedSessionContractImplementor) -> this.updatePicturePreset("postInsert", event.getEntity())
//          );
//      }
		// combosService.refreshCacheOfCurrentEventEntity(event.getEntity().getClass().getSimpleName());
	}

	/**
	 *
	 * @param action
	 * @param entity
	 */
	private void printLog(String action, Object entity) {
		log.info("Hibernate event [" + action + "] on Entity [" + entity.getClass().getSimpleName() + "]");
	}

	private void getEntity(Object entity, PreInsertEvent event) {

	}

	void setValue(Object[] currentState, String[] propertyNames, String propertyToSet, Object value, Object entity) {
		if (value == null)
			return;
		int index = ArrayUtils.indexOf(propertyNames, propertyToSet);
		if (index >= 0) {
			currentState[index] = value;
		} else {
			log.error(
					"Field '" + propertyToSet + "' no encontrado en la entidad '" + entity.getClass().getName() + "'.");
		}
	}

}
