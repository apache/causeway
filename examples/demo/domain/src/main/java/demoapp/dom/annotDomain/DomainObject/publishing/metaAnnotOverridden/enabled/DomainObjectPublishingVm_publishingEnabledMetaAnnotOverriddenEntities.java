package demoapp.dom.annotDomain.DomainObject.publishing.metaAnnotOverridden.enabled;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;

import demoapp.dom.annotDomain.DomainObject.publishing.DomainObjectPublishingVm;

@Collection()
public class DomainObjectPublishingVm_publishingEnabledMetaAnnotOverriddenEntities {

    private final DomainObjectPublishingVm domainObjectPublishingVm;

    public DomainObjectPublishingVm_publishingEnabledMetaAnnotOverriddenEntities(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    public List<DomainObjectPublishingEnabledMetaAnnotOverriddenJdo> coll() {
        return publishingEnabledJdoEntities.all();
    }

    @Inject
    DomainObjectPublishingEnabledMetaAnnotOverriddenJdoEntities publishingEnabledJdoEntities;
}
