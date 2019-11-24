package org.isisaddons.module.fakedata;

public class FakeDataModule {

    public abstract static class ActionDomainEvent<S> extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> {}
    public abstract static class CollectionDomainEvent<S,T> extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S,T> {}
    public abstract static class PropertyDomainEvent<S,T> extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S,T> { }

}
