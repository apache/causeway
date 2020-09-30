package org.apache.isis.extensions.commandreplay.primary;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;
import org.apache.isis.extensions.commandreplay.primary.config.PrimaryConfig;
import org.apache.isis.extensions.commandreplay.primary.mixins.Object_openOnSecondary;
import org.apache.isis.extensions.commandreplay.primary.restapi.CommandRetrievalService;
import org.apache.isis.extensions.commandreplay.primary.spiimpl.CaptureResultOfCommand;
import org.apache.isis.extensions.commandreplay.primary.ui.CommandReplayOnPrimaryService;

@Configuration
@Import({
        // @Configuration's
        IsisModuleExtCommandLogImpl.class,

        // @Service's
        CommandRetrievalService.class,
        CommandReplayOnPrimaryService.class,
        CaptureResultOfCommand.class,
        PrimaryConfig.class,

        // mixins
        Object_openOnSecondary.class,

})
@Profile("primary")
public class IsisModuleExtCommandReplayPrimary {

    public abstract static class ActionDomainEvent<S>
            extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> { }

    public abstract static class CollectionDomainEvent<S,T>
            extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S,T> { }

    public abstract static class PropertyDomainEvent<S,T>
            extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S,T> { }

}
