/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package demoapp.dom.domain.objects.DomainObject.entityChangePublishing;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.disabled.DomainObjectEntityChangePublishingDisabledEntity;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.enabled.DomainObjectEntityChangePublishingEnabledEntity;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnot.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotatedEntity;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnotOverridden.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenEntity;

//tag::class[]
@Action(semantics = SemanticsOf.NON_IDEMPOTENT)
@ActionLayout(
        describedAs = "Creates one publishing enabled entity and one publishing disabled entity",
        sequence = "1.0")
public class DomainObjectEntityChangePublishingVm_create {

    private final DomainObjectEntityChangePublishingVm domainObjectAuditingVm;
    public DomainObjectEntityChangePublishingVm_create(final DomainObjectEntityChangePublishingVm domainObjectAuditingVm) {
        this.domainObjectAuditingVm = domainObjectAuditingVm;
    }

    @MemberSupport public DomainObjectEntityChangePublishingVm act(
            final String newValue
            , final boolean publishingEnabled
            , final boolean publishingDisabled
            , final boolean publishingEnabledMetaAnnotated
            , final boolean publishingEnabledMetaAnnotOverridden
    ) {
        if(publishingEnabled) {
            publishingEnabledEntities.create(newValue);
        }
        if(publishingDisabled) {
            publishingDisabledEntities.create(newValue);
        }
        if(publishingEnabledMetaAnnotated) {
            publishingEnabledMetaAnnotatedEntities.create(newValue);
        }
        if(publishingEnabledMetaAnnotOverridden) {
            publishingEnabledMetaAnnotOverriddenEntities.create(newValue);
        }
        return domainObjectAuditingVm;
    }
    @MemberSupport public String default0Act() {
        return nameSamples.random();
    }
    @MemberSupport public boolean default1Act() {
        return true;
    }
    @MemberSupport public boolean default2Act() {
        return true;
    }
    @MemberSupport public boolean default3Act() {
        return true;
    }
    @MemberSupport public boolean default4Act() {
        return true;
    }

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingEnabledEntity> publishingEnabledEntities;

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingDisabledEntity> publishingDisabledEntities;

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingEnabledMetaAnnotatedEntity> publishingEnabledMetaAnnotatedEntities;

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenEntity> publishingEnabledMetaAnnotOverriddenEntities;

    @Inject
    NameSamples nameSamples;
}
//end::class[]
