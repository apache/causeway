package demoapp.dom.annotDomain.DomainObject.publishing.spiimpl;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.schema.chg.v2.ChangesDto;

import demoapp.dom.annotDomain.DomainObject.publishing.DomainObjectPublishingJdo;

@Collection
public class DomainObjectPublishingJdo_publishedObjects {

    private final DomainObjectPublishingJdo domainObjectPublishingJdo;
    public DomainObjectPublishingJdo_publishedObjects(DomainObjectPublishingJdo domainObjectPublishingJdo) {
        this.domainObjectPublishingJdo = domainObjectPublishingJdo;
    }

    public List<ChangesDto> coll() {
        return publisherServiceSpiForDomainObject
                .streamPublishedObjects()
                .collect(Collectors.toList());
    }

    @Inject
    private PublisherServiceSpiForDomainObject publisherServiceSpiForDomainObject;
}
