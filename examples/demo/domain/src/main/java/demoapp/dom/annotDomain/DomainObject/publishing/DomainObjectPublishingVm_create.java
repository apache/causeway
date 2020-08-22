package demoapp.dom.annotDomain.DomainObject.publishing;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.disabled.DomainObjectPublishingDisabledJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.disabled.DomainObjectPublishingDisabledJdoEntities;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.enabled.DomainObjectPublishingEnabledJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.enabled.DomainObjectPublishingEnabledJdoEntities;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot.enabled.DomainObjectPublishingEnabledMetaAnnotatedJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot.enabled.DomainObjectPublishingEnabledMetaAnnotatedJdoEntities;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnotOverridden.enabled.DomainObjectPublishingEnabledMetaAnnotOverriddenJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnotOverridden.enabled.DomainObjectPublishingEnabledMetaAnnotOverriddenJdoEntities;

//tag::class[]
@Action(semantics = SemanticsOf.NON_IDEMPOTENT)
@ActionLayout(
        describedAs = "Creates one publishing enabled entity and one publishing disabled entity"
)
public class DomainObjectPublishingVm_create {

    private final DomainObjectPublishingVm domainObjectPublishingVm;
    public DomainObjectPublishingVm_create(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    @MemberOrder(sequence = "1.0")
    public DomainObjectPublishingVm act(
            String newValue
            , boolean publishingEnabled
            , boolean publishingDisabled
            , boolean publishingEnabledMetaAnnotated
            , boolean publishingEnabledMetaAnnotOverridden
    ) {
        if(publishingEnabled) {
            publishingEnabledJdoEntities.create(newValue);
        }
        if(publishingDisabled) {
            publishingDisabledJdoEntities.create(newValue);
        }
        if(publishingEnabledMetaAnnotated) {
            publishingEnabledMetaAnnotatedJdoEntities.create(newValue);
        }
        if(publishingEnabledMetaAnnotOverridden) {
            publishingEnabledMetaAnnotOverriddenJdoEntities.create(newValue);
        }
        return domainObjectPublishingVm;
    }
    public String default0Act() {
        return nameSamples.random();
    }
    public boolean default1Act() {
        return true;
    }
    public boolean default2Act() {
        return true;
    }
    public boolean default3Act() {
        return true;
    }
    public boolean default4Act() {
        return true;
    }

    @Inject
    DomainObjectPublishingEnabledJdoEntities publishingEnabledJdoEntities;

    @Inject
    DomainObjectPublishingDisabledJdoEntities publishingDisabledJdoEntities;

    @Inject
    DomainObjectPublishingEnabledMetaAnnotatedJdoEntities publishingEnabledMetaAnnotatedJdoEntities;

    @Inject
    DomainObjectPublishingEnabledMetaAnnotOverriddenJdoEntities publishingEnabledMetaAnnotOverriddenJdoEntities;

    @Inject
    NameSamples nameSamples;
}
//end::class[]
