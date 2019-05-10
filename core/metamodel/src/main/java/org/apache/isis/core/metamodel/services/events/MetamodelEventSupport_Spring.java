package org.apache.isis.core.metamodel.services.events;

import javax.enterprise.event.Event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.events.ui.CssClassUiEvent;
import org.apache.isis.applib.events.ui.IconUiEvent;
import org.apache.isis.applib.events.ui.LayoutUiEvent;
import org.apache.isis.applib.events.ui.TitleUiEvent;
import org.apache.isis.commons.internal.spring._Spring;

@Configuration
public class MetamodelEventSupport_Spring {
    
    @Bean
    public Event<CssClassUiEvent<Object>> cssClassUiEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }
    
    @Bean
    public Event<IconUiEvent<Object>> iconUiEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }
    
    @Bean
    public Event<LayoutUiEvent<Object>> layoutUiEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }
    
    @Bean
    public Event<TitleUiEvent<Object>> titleUiEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }
    
    @Bean
    public Event<ActionDomainEvent<?>> actionDomainEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }
    
    @Bean
    public Event<PropertyDomainEvent<?, ?>> propertyDomainEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }
    
    @Bean
    public Event<CollectionDomainEvent<?, ?>> collectionDomainEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }
    
    
}
