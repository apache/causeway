package demoapp.dom.annotDomain.DomainObject.publishing;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;

import lombok.val;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom.types.Samples;

//tag::class[]
@Action(semantics = SemanticsOf.NON_IDEMPOTENT)
@ActionLayout(
        describedAs = "Creates one publishing enabled entity and one publishing disabled entity"
)
public class DomainObjectPublishingVm_create {

    private final DomainObjectPublishingVm domainObjectPublishingVm;
    public DomainObjectPublishingVm_create(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    @MemberOrder(sequence = "1.0")
    public DomainObjectPublishingVm act(String newValue) {
        repositoryService.persistAndFlush(new DomainObjectPublishingEnabledJdo(newValue));
        repositoryService.persistAndFlush(new DomainObjectPublishingDisabledJdo(newValue));
        return domainObjectPublishingVm;
    }
    public String default0Act() {
        return nameSamples.random();
    }

    @Inject
    RepositoryService repositoryService;
    @Inject
    NameSamples nameSamples;
}
//end::class[]
