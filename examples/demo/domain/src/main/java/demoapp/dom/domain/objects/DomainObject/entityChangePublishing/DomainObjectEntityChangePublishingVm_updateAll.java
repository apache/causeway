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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.disabled.DomainObjectEntityChangePublishingDisabledEntity;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.enabled.DomainObjectEntityChangePublishingEnabledEntity;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnot.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotatedEntity;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnotOverridden.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenEntity;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@ActionLayout(
    describedAs = "Updates all publishing enabled entities and all publishing disabled entities",
    sequence = "2.0")
@RequiredArgsConstructor
public class DomainObjectEntityChangePublishingVm_updateAll {

    private final DomainObjectEntityChangePublishingVm domainObjectAuditingVm;

    public DomainObjectEntityChangePublishingVm act(
            boolean publishingEnabled
            , boolean publishingDisabled
            , boolean publishingEnabledMetaAnnotated
            , boolean publishingEnabledMetaAnnotOverridden
    ) {

        if(publishingEnabled) {
            renumber((List)publishingEnabledEntities.all());
        }
        if(publishingDisabled) {
            renumber((List)publishingDisabledEntities.all());
        }
        if(publishingEnabledMetaAnnotated) {
            renumber((List)publishingEnabledMetaAnnotatedEntities.all());
        }
        if(publishingEnabledMetaAnnotOverridden) {
            renumber((List)publishingEnabledMetaAnnotOverriddenEntities.all());
        }

        return domainObjectAuditingVm;
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

    final static AtomicInteger counter = new AtomicInteger(0);
    private static void renumber(List<DomainObjectEntityChangePublishingEntity> all) {
        all.forEach(x -> x.setPropertyUpdatedByAction("Object #" + counter.incrementAndGet()));
    }

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingEnabledEntity> publishingEnabledEntities;

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingDisabledEntity> publishingDisabledEntities;

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingEnabledMetaAnnotatedEntity> publishingEnabledMetaAnnotatedEntities;

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenEntity> publishingEnabledMetaAnnotOverriddenEntities;

}
//end::class[]
