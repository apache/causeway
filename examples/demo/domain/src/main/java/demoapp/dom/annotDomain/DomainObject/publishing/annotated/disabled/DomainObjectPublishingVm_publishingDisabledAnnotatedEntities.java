package demoapp.dom.annotDomain.DomainObject.publishing.annotated.disabled;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;

import demoapp.dom.annotDomain.DomainObject.publishing.DomainObjectPublishingVm;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.disabled.DomainObjectPublishingDisabledJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.disabled.DomainObjectPublishingDisabledJdoEntities;

@Collection()
public class DomainObjectPublishingVm_publishingDisabledAnnotatedEntities {

    private final DomainObjectPublishingVm domainObjectPublishingVm;

    public DomainObjectPublishingVm_publishingDisabledAnnotatedEntities(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    public List<DomainObjectPublishingDisabledJdo> coll() {
        return publishingDisabledJdoEntities.all();
    }

    @Inject
    DomainObjectPublishingDisabledJdoEntities publishingDisabledJdoEntities;
}
