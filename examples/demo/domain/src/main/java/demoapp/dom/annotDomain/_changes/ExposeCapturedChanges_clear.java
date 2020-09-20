package demoapp.dom.annotDomain._changes;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom.annotDomain.DomainObject.publishing.DomainObjectPublishingVm;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "changes"
)
public class ExposeCapturedChanges_clear {
    // ...
//end::class[]

    private final DomainObjectPublishingVm domainObjectPublishingVm;

    public ExposeCapturedChanges_clear(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }


    //tag::class[]
    public DomainObjectPublishingVm act() {
        publisherServiceToCaptureChangesInMemory.clear();
        return domainObjectPublishingVm;
    }

    @Inject
    PublisherServiceToCaptureChangesInMemory publisherServiceToCaptureChangesInMemory;
}
//end::class[]
