package demoapp.dom.annotDomain.Property.domainEvent.subscribers;

import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.registry.ServiceRegistry;

import demoapp.dom.annotDomain.Property.domainEvent.PropertyDomainEventVm;

// tag::class[]
enum ControlStrategy {

    DO_NOTHING{
        @Override
        void on(PropertyDomainEventVm.TextDomainEvent ev
                , ServiceRegistry serviceRegistry) {
        }
    },
    // ...
// end::class[]

// tag::hide[]
    HIDE {
        @Override
        void on(PropertyDomainEventVm.TextDomainEvent ev
                , ServiceRegistry serviceRegistry) {
            switch (ev.getEventPhase()) {
                case HIDE:
                    ev.hide();
                    break;
            }
        }
    },
// end::hide[]
// tag::disable[]
    DISABLE{
        @Override
        void on(PropertyDomainEventVm.TextDomainEvent ev
                , ServiceRegistry serviceRegistry) {
            switch (ev.getEventPhase()) {
                case DISABLE:
                    ev.disable("ControlStrategy set to DISABLE");
                    break;
            }

        }
    },
// end::disable[]
// tag::validate[]
    VALIDATE_MUST_BE_UPPER_CASE{
        @Override
        void on(PropertyDomainEventVm.TextDomainEvent ev
                , ServiceRegistry serviceRegistry) {
            switch (ev.getEventPhase()) {
                case VALIDATE:
                    String newValue = ev.getNewValue();
                    if(!newValue.toUpperCase().equals(newValue)) {
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
        void on(PropertyDomainEventVm.TextDomainEvent ev
                , ServiceRegistry serviceRegistry) {

            switch (ev.getEventPhase()) {
                case EXECUTING:
                    String newValue = ev.getNewValue().toUpperCase();
                    ev.setNewValue(newValue);

                    break;
            }
        }
    },
// end::executing[]
// tag::executed[]
    EXECUTED_ANNOUNCE{
        @Override
        void on(PropertyDomainEventVm.TextDomainEvent ev
                , ServiceRegistry serviceRegistry) {
            switch (ev.getEventPhase()) {
                case EXECUTED:
                    serviceRegistry
                        .lookupService(MessageService.class)
                        .ifPresent(ms ->
                                ms.informUser(
                                    String.format("Changed from %s to %s"
                                            , ev.getOldValue()
                                            , ev.getNewValue()))
                        );
                    break;
            }
        }
    }
// end::executed[]

// tag::class[]
    ;
    abstract void on(PropertyDomainEventVm.TextDomainEvent ev
            , ServiceRegistry serviceRegistry);
}
// end::class[]
