package org.isisaddons.module.audit.dom;

import java.util.List;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        objectType = "isisaudit.AuditingServiceMenu"
)
@DomainServiceLayout(
        named = "Activity",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        menuOrder = "30"
)
public class AuditingServiceMenu {

    //region > domain events
    public static abstract class PropertyDomainEvent<T>
            extends org.apache.isis.applib.services.eventbus.PropertyDomainEvent<AuditingServiceMenu, T> {
    }

    public static abstract class CollectionDomainEvent<T>
            extends org.apache.isis.applib.services.eventbus.CollectionDomainEvent<AuditingServiceMenu, T> {
    }

    public static abstract class ActionDomainEvent
            extends org.apache.isis.applib.services.eventbus.ActionDomainEvent<AuditingServiceMenu> {
    }
    //endregion

    //region > findAuditEntries (action)
    public static class FindAuditEntriesDomainEvent extends ActionDomainEvent { }

    @Action(
            domainEvent = FindAuditEntriesDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            cssClassFa = "fa-search"
    )
    @MemberOrder(sequence="10")
    public List<AuditEntry> findAuditEntries(
            @Parameter(optionality= Optionality.OPTIONAL)
            @ParameterLayout(named="From")
            final LocalDate from,
            @Parameter(optionality=Optionality.OPTIONAL)
            @ParameterLayout(named="To")
            final LocalDate to) {
        return auditingServiceRepository.findByFromAndTo(from, to);
    }
    public boolean hideFindAuditEntries() {
        return auditingServiceRepository == null;
    }
    public LocalDate default0FindAuditEntries() {
        return clockService.now().minusDays(7);
    }
    public LocalDate default1FindAuditEntries() {
        return clockService.now();
    }
    //endregion

    //region > injected services
    @javax.inject.Inject
    private AuditingServiceRepository auditingServiceRepository;
    
    @javax.inject.Inject
    private ClockService clockService;
    //endregion

}

