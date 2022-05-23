package org.apache.isis.sessionlog.applib;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import({

})
public class IsisModuleExtSessionLogApplib {

    public static final String NAMESPACE = "isis.ext.sessionlog";
    public static final String SCHEMA = "isisExtSessionLog";

    public abstract static class ActionDomainEvent<S>
    extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> {}

    public abstract static class CollectionDomainEvent<S, T>
    extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S, T> {}

    public abstract static class PropertyDomainEvent<S, T>
    extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S, T> {}

}
