package demoapp.dom.annotDomain.DomainObject.publishing;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@ActionLayout(
    describedAs = "Deletes one publishing enabled entity and one publishing disabled entity"
)
public class DomainObjectPublishingVm_delete {

    private final DomainObjectPublishingVm domainObjectPublishingVm;
    public DomainObjectPublishingVm_delete(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    @MemberOrder(sequence = "3.0")
    public DomainObjectPublishingVm act(DomainObjectPublishingEnabledJdo enabledJdo, DomainObjectPublishingDisabledJdo disabledJdo) {
        enabledJdoEntities.remove(enabledJdo);
        disabledJdoEntities.remove(disabledJdo);
        return domainObjectPublishingVm;
    }
    public DomainObjectPublishingEnabledJdo default0Act() {
        return enabledJdoEntities.first();
    }
    public DomainObjectPublishingDisabledJdo default1Act() {
        return disabledJdoEntities.first();
    }

    @Inject
    DomainObjectPublishingEnabledJdoEntities enabledJdoEntities;
    @Inject
    DomainObjectPublishingDisabledJdoEntities disabledJdoEntities;
}
//end::class[]
