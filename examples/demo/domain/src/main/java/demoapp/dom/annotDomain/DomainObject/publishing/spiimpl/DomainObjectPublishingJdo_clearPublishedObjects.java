package demoapp.dom.annotDomain.DomainObject.publishing.spiimpl;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom.annotDomain.DomainObject.publishing.DomainObjectPublishingJdo;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "publishedObjects"
)
public class DomainObjectPublishingJdo_clearPublishedObjects {
    // ...
//end::class[]

    private final DomainObjectPublishingJdo domainObjectPublishingJdo;

    public DomainObjectPublishingJdo_clearPublishedObjects(DomainObjectPublishingJdo domainObjectPublishingJdo) {
        this.domainObjectPublishingJdo = domainObjectPublishingJdo;
    }

    //tag::class[]
    public DomainObjectPublishingJdo act() {
        publisherServiceSpiForDomainObject.clear();
        return domainObjectPublishingJdo;
    }

    @Inject
    PublisherServiceSpiForDomainObject publisherServiceSpiForDomainObject;
}
//end::class[]
