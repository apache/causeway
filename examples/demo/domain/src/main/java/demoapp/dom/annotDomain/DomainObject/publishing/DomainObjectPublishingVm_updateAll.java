package demoapp.dom.annotDomain.DomainObject.publishing;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.val;

import demoapp.dom.annotDomain.DomainObject.publishing.annotated.disabled.DomainObjectPublishingDisabledJdoEntities;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.enabled.DomainObjectPublishingEnabledJdoEntities;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot.enabled.DomainObjectPublishingEnabledMetaAnnotatedJdoEntities;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnotOverridden.enabled.DomainObjectPublishingEnabledMetaAnnotOverriddenJdoEntities;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@ActionLayout(
    describedAs = "Updates all publishing enabled entities and all publishing disabled entities"
)
public class DomainObjectPublishingVm_updateAll {

    private final DomainObjectPublishingVm domainObjectPublishingVm;
    public DomainObjectPublishingVm_updateAll(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    @MemberOrder(sequence = "2.0")
    public DomainObjectPublishingVm act(
            boolean publishingEnabled
            , boolean publishingDisabled
            , boolean publishingEnabledMetaAnnotated
            , boolean publishingEnabledMetaAnnotOverridden
    ) {

        if(publishingEnabled) {
            renumber((List)publishingEnabledJdoEntities.all());
        }
        if(publishingDisabled) {
            renumber((List)publishingDisabledJdoEntities.all());
        }
        if(publishingEnabledMetaAnnotated) {
            renumber((List)publishingEnabledMetaAnnotatedJdoEntities.all());
        }
        if(publishingEnabledMetaAnnotOverridden) {
            renumber((List)publishingEnabledMetaAnnotOverriddenJdoEntities.all());
        }

        return domainObjectPublishingVm;
    }
    public boolean default0Act() {
        return true;
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

    private static void renumber(List<DomainObjectPublishingJdo> all) {
        val ai = new AtomicInteger(0);
        all.forEach(x -> x.setPropertyUpdatedByAction("Object #" + ai.incrementAndGet()));
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
