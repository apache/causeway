package demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot.enabled;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;

import demoapp.dom.annotDomain.DomainObject.publishing.DomainObjectPublishingVm;

@Collection()
public class DomainObjectPublishingVm_publishingEnabledMetaAnnotatedEntities {

    private final DomainObjectPublishingVm domainObjectPublishingVm;

    public DomainObjectPublishingVm_publishingEnabledMetaAnnotatedEntities(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    public List<DomainObjectPublishingEnabledMetaAnnotatedJdo> coll() {
        return publishingEnabledJdoEntities.all();
    }

    @Inject
    DomainObjectPublishingEnabledMetaAnnotatedJdoEntities publishingEnabledJdoEntities;
}
