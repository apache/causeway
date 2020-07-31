package demoapp.dom.annotDomain.Action.domainEvent.subscribers;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.event.EventListener;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.registry.ServiceRegistry;

import demoapp.dom.annotDomain.Action.domainEvent.ActionDomainEventVm;
import demoapp.dom.annotDomain.Action.domainEvent.ActionDomainEventVm_mixinUpdateText;

// tag::class[]
@DomainService(objectType = "demo.ActionDomainEventControlService")
class ActionDomainEventControlService {

    ActionDomainEventControlStrategy controlStrategy = ActionDomainEventControlStrategy.DO_NOTHING;         // <.>

    @EventListener(ActionDomainEventVm.UpdateTextDomainEvent.class)       // <.>
    public void on(ActionDomainEventVm.UpdateTextDomainEvent ev) {
        controlStrategy.on(ev, serviceRegistry);
    }

    @EventListener(ActionDomainEventVm_mixinUpdateText.DomainEvent.class) // <.>
    public void on(ActionDomainEventVm_mixinUpdateText.DomainEvent ev) {
        controlStrategy.on(ev, serviceRegistry);
    }

    @Inject
    ServiceRegistry serviceRegistry;
}
// end::class[]
