package demoapp.dom.annotDomain.DomainObject.publishing;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;

@Collection()
public class DomainObjectPublishingVm_publishingEnabledEntities {

    private final DomainObjectPublishingVm domainObjectPublishingVm;

    public DomainObjectPublishingVm_publishingEnabledEntities(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    public List<DomainObjectPublishingEnabledJdo> coll() {
        return publishingEnabledJdoEntities.all();
    }

    @Inject
    DomainObjectPublishingEnabledJdoEntities publishingEnabledJdoEntities;
}
