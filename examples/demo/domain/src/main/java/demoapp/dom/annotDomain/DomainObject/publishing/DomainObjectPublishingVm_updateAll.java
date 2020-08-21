package demoapp.dom.annotDomain.DomainObject.publishing;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;

import lombok.val;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@ActionLayout(
    describedAs = "Updates all publishing enabled entities and all publishing disabled entities"
)
public class DomainObjectPublishingVm_updateAll {

    private final DomainObjectPublishingVm domainObjectPublishingVm;
    public DomainObjectPublishingVm_updateAll(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    @MemberOrder(sequence = "2.0")
    public DomainObjectPublishingVm act() {

        renumber((List)publishingEnabledJdoEntities.all());
        renumber((List)publishingDisabledJdoEntities.all());

        return domainObjectPublishingVm;
    }

    private static void renumber(List<DomainObjectPublishingJdo> all) {
        val ai = new AtomicInteger(0);
        all.forEach(x -> x.setPropertyUpdatedByAction("Object #" + ai.incrementAndGet()));
    }

    @Inject
    DomainObjectPublishingDisabledJdoEntities publishingDisabledJdoEntities;

    @Inject
    DomainObjectPublishingEnabledJdoEntities publishingEnabledJdoEntities;
}
//end::class[]
