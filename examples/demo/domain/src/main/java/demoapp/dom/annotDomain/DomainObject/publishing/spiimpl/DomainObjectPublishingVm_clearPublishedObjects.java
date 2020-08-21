package demoapp.dom.annotDomain.DomainObject.publishing.spiimpl;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom.annotDomain.DomainObject.publishing.DomainObjectPublishingVm;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "publishedObjects"
)
public class DomainObjectPublishingVm_clearPublishedObjects {
    // ...
//end::class[]

    private final DomainObjectPublishingVm domainObjectPublishingVm;

    public DomainObjectPublishingVm_clearPublishedObjects(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }


    //tag::class[]
    public DomainObjectPublishingVm act() {
        publisherServiceSpiForDomainObject.clear();
        return domainObjectPublishingVm;
    }

    @Inject
    PublisherServiceSpiForDomainObject publisherServiceSpiForDomainObject;
}
//end::class[]
