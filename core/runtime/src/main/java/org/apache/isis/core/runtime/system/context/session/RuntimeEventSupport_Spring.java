package org.apache.isis.core.runtime.system.context.session;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;

import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.util.TypeLiteral;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.isis.applib.fixturescripts.events.FixturesInstalledEvent;
import org.apache.isis.applib.fixturescripts.events.FixturesInstallingEvent;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;

@Configuration
public class RuntimeEventSupport_Spring {
    
    @Bean
    public Event<AppLifecycleEvent> appLifecycleEvents() {
        return new SimpleEvent<AppLifecycleEvent>();
    }
    
    @Bean
    public Event<SessionLifecycleEvent> sessionLifecycleEvents() {
        return new SimpleEvent<SessionLifecycleEvent>();
    }
    
    @Bean
    public Event<FixturesInstallingEvent> fixturesInstallingEvents() {
        return new SimpleEvent<FixturesInstallingEvent>();
    }
    
    @Bean
    public Event<FixturesInstalledEvent> fixturesInstalledEvents() {
        return new SimpleEvent<FixturesInstalledEvent>();
    }
    
    // -- HELPER
    
    static class SimpleEvent<T> implements Event<T> {

        _Probe probe = _Probe.unlimited().label("SimpleEvent");
        
        @Override
        public void fire(T event) {
            probe.warnNotImplementedYet("fire("+event+")");
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
