package org.apache.isis.runtime.system.context.session;

import javax.enterprise.event.Event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.isis.applib.fixturescripts.events.FixturesInstalledEvent;
import org.apache.isis.applib.fixturescripts.events.FixturesInstallingEvent;
import org.apache.isis.commons.internal.ioc.spring._Spring;

@Configuration
public class RuntimeEventSupport_Spring {
    
    @Bean
    public Event<AppLifecycleEvent> appLifecycleEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }
    
    @Bean
    public Event<SessionLifecycleEvent> sessionLifecycleEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }
    
    @Bean
    public Event<FixturesInstallingEvent> fixturesInstallingEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }
    
    @Bean
    public Event<FixturesInstalledEvent> fixturesInstalledEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }
    

}
