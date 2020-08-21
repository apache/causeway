package demoapp.dom.annotDomain.DomainObject.publishing;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;

@Collection()
public class DomainObjectPublishingVm_publishingDisabledEntities {

    private final DomainObjectPublishingVm domainObjectPublishingVm;

    public DomainObjectPublishingVm_publishingDisabledEntities(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    public List<DomainObjectPublishingDisabledJdo> coll() {
        return publishingDisabledJdoEntities.all();
    }

    @Inject
    DomainObjectPublishingDisabledJdoEntities publishingDisabledJdoEntities;
}
