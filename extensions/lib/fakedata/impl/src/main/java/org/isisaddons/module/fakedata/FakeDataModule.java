package org.isisaddons.module.fakedata;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
public class FakeDataModule {

    public abstract static class ActionDomainEvent<S> extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> {}
    public abstract static class CollectionDomainEvent<S,T> extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S,T> {}
    public abstract static class PropertyDomainEvent<S,T> extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S,T> { }

}
