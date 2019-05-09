package org.apache.isis.core.runtime.system.context.session;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;

import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.util.TypeLiteral;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.isis.applib.fixturescripts.events.FixturesInstalledEvent;
import org.apache.isis.applib.fixturescripts.events.FixturesInstallingEvent;
import org.apache.isis.commons.internal.debug._Probe;

import lombok.RequiredArgsConstructor;

@Configuration
public class RuntimeEventSupport_Spring {
    
    @Bean
    public Event<AppLifecycleEvent> appLifecycleEvents(ApplicationEventPublisher publisher) {
        return new SimpleEvent<AppLifecycleEvent>(publisher);
    }
    
    @Bean
    public Event<SessionLifecycleEvent> sessionLifecycleEvents(ApplicationEventPublisher publisher) {
        return new SimpleEvent<SessionLifecycleEvent>(publisher);
    }
    
    @Bean
    public Event<FixturesInstallingEvent> fixturesInstallingEvents(ApplicationEventPublisher publisher) {
        return new SimpleEvent<FixturesInstallingEvent>(publisher);
    }
    
    @Bean
    public Event<FixturesInstalledEvent> fixturesInstalledEvents(ApplicationEventPublisher publisher) {
        return new SimpleEvent<FixturesInstalledEvent>(publisher);
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
