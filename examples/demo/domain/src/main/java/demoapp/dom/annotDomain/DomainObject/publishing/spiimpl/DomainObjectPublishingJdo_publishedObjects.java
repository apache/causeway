package demoapp.dom.annotDomain.DomainObject.publishing.spiimpl;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.schema.chg.v2.ChangesDto;

import lombok.val;

import demoapp.dom.annotDomain.DomainObject.publishing.DomainObjectPublishingJdo;

//tag::class[]
@Collection
public class DomainObjectPublishingJdo_publishedObjects {
    // ...
//end::class[]

    private final DomainObjectPublishingJdo domainObjectPublishingJdo;
    public DomainObjectPublishingJdo_publishedObjects(DomainObjectPublishingJdo domainObjectPublishingJdo) {
        this.domainObjectPublishingJdo = domainObjectPublishingJdo;
    }

//tag::class[]
    public List<ChangesDto> coll() {
        val list = new LinkedList<ChangesDto>();
        publisherServiceSpiForDomainObject
                .streamPublishedObjects()
                .forEach(list::push);   // reverse order
        return list;
    }

    @Inject
    PublisherServiceSpiForDomainObject publisherServiceSpiForDomainObject;
}
//end::class[]
