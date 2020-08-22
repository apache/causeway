package demoapp.dom.annotDomain.DomainObject.publishing.annotated.enabled;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;

import demoapp.dom.annotDomain.DomainObject.publishing.DomainObjectPublishingVm;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.enabled.DomainObjectPublishingEnabledJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.enabled.DomainObjectPublishingEnabledJdoEntities;

@Collection()
public class DomainObjectPublishingVm_publishingEnabledAnnotatedEntities {

    private final DomainObjectPublishingVm domainObjectPublishingVm;

    public DomainObjectPublishingVm_publishingEnabledAnnotatedEntities(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    public List<DomainObjectPublishingEnabledJdo> coll() {
        return publishingEnabledJdoEntities.all();
    }

    @Inject
    DomainObjectPublishingEnabledJdoEntities publishingEnabledJdoEntities;
}
