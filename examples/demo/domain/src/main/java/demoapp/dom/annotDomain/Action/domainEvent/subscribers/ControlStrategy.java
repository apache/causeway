package demoapp.dom.annotDomain.Action.domainEvent.subscribers;

import java.util.List;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.registry.ServiceRegistry;

import demoapp.dom.annotDomain.Action.domainEvent.ActionDomainEventVm;
import demoapp.dom.annotDomain.Action.domainEvent.ActionDomainEventVm_mixinUpdateText;

// tag::class[]
enum ControlStrategy {

    DO_NOTHING{
        @Override
        void on(ActionDomainEvent<?> ev
                , ServiceRegistry serviceRegistry) {
        }
    },
    // ...
// end::class[]

// tag::hide[]
    HIDE_BOTH {
        @Override
        void on(ActionDomainEvent<?> ev
                , ServiceRegistry serviceRegistry) {
            switch (ev.getEventPhase()) {
                case HIDE:
                    ev.hide();
                    break;
            }
        }
    },
    HIDE_REGULAR_ACTION {
        @Override
        void on(ActionDomainEvent<?> ev
                , ServiceRegistry serviceRegistry) {
            if (ev instanceof ActionDomainEventVm.UpdateTextDomainEvent) {
                switch (ev.getEventPhase()) {
                    case HIDE:
                        ev.hide();
                        break;
                }
            }
        }
    },
// end::hide[]
// tag::disable[]
    DISABLE_BOTH {
        @Override
        void on(ActionDomainEvent<?> ev
                , ServiceRegistry serviceRegistry) {
            switch (ev.getEventPhase()) {
                case DISABLE:
                    ev.disable("ControlStrategy set to DISABLE_BOTH");
                    break;
            }

        }
    },
    DISABLE_MIXIN_ACTION {
        @Override
        void on(ActionDomainEvent<?> ev
                , ServiceRegistry serviceRegistry) {
            if(ev instanceof ActionDomainEventVm_mixinUpdateText.DomainEvent) {
                switch (ev.getEventPhase()) {
                    case DISABLE:
                        ev.disable("ControlStrategy set to DISABLE_MIXIN_ACTION");
                        break;
                }
            }

        }
    },
// end::disable[]
// tag::validate[]
    VALIDATE_MUST_BE_UPPER_CASE{
        @Override
        void on(ActionDomainEvent<?> ev
                , ServiceRegistry serviceRegistry) {
            switch (ev.getEventPhase()) {
                case VALIDATE:
                    String argument = (String) ev.getArguments().get(0);
                    if(!argument.toUpperCase().equals(argument)) {
                        ev.invalidate("must be upper case");
                    }
                    break;
            }

        }
    },
// end::validate[]
// tag::executing[]
    EXECUTING_FORCE_UPPER_CASE{
        @Override
        void on(ActionDomainEvent<?> ev
                , ServiceRegistry serviceRegistry) {

            switch (ev.getEventPhase()) {
                case EXECUTING:
                    List<Object> arguments = ev.getArguments();
                    String newValue = ((String) arguments.get(0)).toUpperCase();
                    arguments.set(0, newValue);
                    break;
            }
        }
    },
// end::executing[]
// tag::executed[]
    EXECUTED_ANNOUNCE{
        @Override
        void on(ActionDomainEvent<?> ev
                , ServiceRegistry serviceRegistry) {
            switch (ev.getEventPhase()) {
                case EXECUTED:
                    serviceRegistry
                        .lookupService(MessageService.class)
                        .ifPresent(ms ->
                                ms.informUser("Changed using updateText")
                        );
                    break;
            }
        }
    }
// end::executed[]

// tag::class[]
    ;
    abstract void on(ActionDomainEvent<?> ev
            , ServiceRegistry serviceRegistry);
}
// end::class[]
