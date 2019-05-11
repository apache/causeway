package org.apache.isis.core.metamodel.services.events;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.events.ui.CssClassUiEvent;
import org.apache.isis.applib.events.ui.IconUiEvent;
import org.apache.isis.applib.events.ui.LayoutUiEvent;
import org.apache.isis.applib.events.ui.TitleUiEvent;

import lombok.Builder;

/**
 * 
 * @since 2.0.0-M3
 *
 */
@Singleton 
@Builder //for JUnit Test support
public class MetamodelEventService {

	@Inject Event<CssClassUiEvent<Object>> cssClassUiEvents;
	@Inject Event<IconUiEvent<Object>> iconUiEvents;
	@Inject Event<LayoutUiEvent<Object>> layoutUiEvents;
	@Inject Event<TitleUiEvent<Object>> titleUiEvents;
	
	@Inject Event<ActionDomainEvent<?>> actionDomainEvents;
	@Inject Event<PropertyDomainEvent<?, ?>> propertyDomainEvents;
	@Inject Event<CollectionDomainEvent<?, ?>> collectionDomainEvents;
	
	// -- METAMODEL UI EVENTS
	
	public void fireCssClassUiEvent(CssClassUiEvent<Object> event) {
		cssClassUiEvents.fire(event);
	}

	public void fireIconUiEvent(IconUiEvent<Object> event) {
		iconUiEvents.fire(event);
	}

	public void fireLayoutUiEvent(LayoutUiEvent<Object> event) {
		layoutUiEvents.fire(event);
	}

	public void fireTitleUiEvent(TitleUiEvent<Object> event) {
		titleUiEvents.fire(event);
	}

	public void fireActionDomainEvent(ActionDomainEvent<?> event) {
		actionDomainEvents.fire(event);
	}

	public void firePropertyDomainEvent(PropertyDomainEvent<?, ?> event) {
		propertyDomainEvents.fire(event);
	}

	public void fireCollectionDomainEvent(CollectionDomainEvent<?, ?> event) {
		collectionDomainEvents.fire(event);
	}
	

}
