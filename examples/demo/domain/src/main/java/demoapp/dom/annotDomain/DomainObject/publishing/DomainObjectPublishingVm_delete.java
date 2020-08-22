package demoapp.dom.annotDomain.DomainObject.publishing;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom.annotDomain.DomainObject.publishing.annotated.disabled.DomainObjectPublishingDisabledJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.disabled.DomainObjectPublishingDisabledJdoEntities;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.enabled.DomainObjectPublishingEnabledJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.enabled.DomainObjectPublishingEnabledJdoEntities;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot.enabled.DomainObjectPublishingEnabledMetaAnnotatedJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot.enabled.DomainObjectPublishingEnabledMetaAnnotatedJdoEntities;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnotOverridden.enabled.DomainObjectPublishingEnabledMetaAnnotOverriddenJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnotOverridden.enabled.DomainObjectPublishingEnabledMetaAnnotOverriddenJdoEntities;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@ActionLayout(
    describedAs = "Deletes one publishing enabled entity and one publishing disabled entity"
)
public class DomainObjectPublishingVm_delete {

    private final DomainObjectPublishingVm domainObjectPublishingVm;
    public DomainObjectPublishingVm_delete(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    @MemberOrder(sequence = "3.0")
    public DomainObjectPublishingVm act(
            @Nullable DomainObjectPublishingEnabledJdo enabledJdo
            , @Nullable DomainObjectPublishingDisabledJdo disabledJdo
            , @Nullable DomainObjectPublishingEnabledMetaAnnotatedJdo metaAnnotatedJdo
            , @Nullable DomainObjectPublishingEnabledMetaAnnotOverriddenJdo metaAnnotOverriddenJdo
            ) {
        if(enabledJdo != null) {
            publishingEnabledJdoEntities.remove(enabledJdo);
        }
        if(disabledJdo != null) {
            publishingDisabledJdoEntities.remove(disabledJdo);
        }
        if(metaAnnotatedJdo != null) {
            publishingEnabledMetaAnnotatedJdoEntities.remove(metaAnnotatedJdo);
        }
        if(metaAnnotOverriddenJdo != null) {
            publishingEnabledMetaAnnotOverriddenJdoEntities.remove(metaAnnotOverriddenJdo);
        }
        return domainObjectPublishingVm;
    }
    public DomainObjectPublishingEnabledJdo default0Act() {
        return publishingEnabledJdoEntities.first();
    }
    public DomainObjectPublishingDisabledJdo default1Act() {
        return publishingDisabledJdoEntities.first();
    }
    public DomainObjectPublishingEnabledMetaAnnotatedJdo default2Act() {
        return publishingEnabledMetaAnnotatedJdoEntities.first();
    }
    public DomainObjectPublishingEnabledMetaAnnotOverriddenJdo default3Act() {
        return publishingEnabledMetaAnnotOverriddenJdoEntities.first();
    }

    @Inject
    DomainObjectPublishingEnabledJdoEntities publishingEnabledJdoEntities;

    @Inject
    DomainObjectPublishingDisabledJdoEntities publishingDisabledJdoEntities;

    @Inject
    DomainObjectPublishingEnabledMetaAnnotatedJdoEntities publishingEnabledMetaAnnotatedJdoEntities;

    @Inject
    DomainObjectPublishingEnabledMetaAnnotOverriddenJdoEntities publishingEnabledMetaAnnotOverriddenJdoEntities;
}
//end::class[]
