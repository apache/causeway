package demoapp.dom.annotDomain.DomainObject.publishing.spiimpl;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom.annotDomain.DomainObject.publishing.DomainObjectPublishingJdo;
import demoapp.dom.annotDomain.Property.publishing.spiimpl.PublisherServiceSpiForProperties;

@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "publishedObjects"
)
public class DomainObjectPublishingJdo_clearPublishedObjects {

    private final DomainObjectPublishingJdo domainObjectPublishingJdo;

    public DomainObjectPublishingJdo_clearPublishedObjects(DomainObjectPublishingJdo domainObjectPublishingJdo) {
        this.domainObjectPublishingJdo = domainObjectPublishingJdo;
    }

    public DomainObjectPublishingJdo act() {
        publisherServiceSpiForDomainObject.clear();
        return domainObjectPublishingJdo;
    }

    @Inject
    private PublisherServiceSpiForDomainObject publisherServiceSpiForDomainObject;
}
