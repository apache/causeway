package org.apache.isis.core.metamodel.services.events;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;

import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.util.TypeLiteral;

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
import org.apache.isis.commons.internal.debug._Probe;

import lombok.RequiredArgsConstructor;

@Configuration
public class MetamodelEventSupport_Spring {
    
    @Bean
    public Event<CssClassUiEvent<Object>> cssClassUiEvents(ApplicationEventPublisher publisher) {
        return new SimpleEvent<CssClassUiEvent<Object>>(publisher);
    }
    
    @Bean
    public Event<IconUiEvent<Object>> iconUiEvents(ApplicationEventPublisher publisher) {
        return new SimpleEvent<IconUiEvent<Object>>(publisher);
    }
    
    @Bean
    public Event<LayoutUiEvent<Object>> layoutUiEvents(ApplicationEventPublisher publisher) {
        return new SimpleEvent<LayoutUiEvent<Object>>(publisher);
    }
    
    @Bean
    public Event<TitleUiEvent<Object>> titleUiEvents(ApplicationEventPublisher publisher) {
        return new SimpleEvent<TitleUiEvent<Object>>(publisher);
    }
    
    @Bean
    public Event<ActionDomainEvent<?>> actionDomainEvents(ApplicationEventPublisher publisher) {
        return new SimpleEvent<ActionDomainEvent<?>>(publisher);
    }
    
    @Bean
    public Event<PropertyDomainEvent<?, ?>> propertyDomainEvents(ApplicationEventPublisher publisher) {
        return new SimpleEvent<PropertyDomainEvent<?, ?>>(publisher);
    }
    
    @Bean
    public Event<CollectionDomainEvent<?, ?>> collectionDomainEvents(ApplicationEventPublisher publisher) {
        return new SimpleEvent<CollectionDomainEvent<?, ?>>(publisher);
    }
    
    // -- HELPER
    
    @RequiredArgsConstructor
    static class SimpleEvent<T> implements Event<T> {

        private final ApplicationEventPublisher publisher;
        
        _Probe probe = _Probe.unlimited().label("SimpleEvent");

        @Override
        public void fire(T event) {
            probe.println("fire(%s)", event.getClass());
            publisher.publishEvent(event);
        }

        @Override
        public <U extends T> CompletionStage<U> fireAsync(U event) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <U extends T> CompletionStage<U> fireAsync(U event, NotificationOptions options) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Event<T> select(Annotation... qualifiers) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <U extends T> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <U extends T> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
    

}
