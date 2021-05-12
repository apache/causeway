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

import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.disabled.DomainObjectEntityChangePublishingDisabledJdoEntities;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.enabled.DomainObjectAuditingEnabledJdoEntities;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnot.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotatedJdoEntities;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnotOverridden.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenJdoEntities;
import lombok.RequiredArgsConstructor;

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
    private static void renumber(List<DomainObjectEntityChangePublishingJdo> all) {
        all.forEach(x -> x.setPropertyUpdatedByAction("Object #" + counter.incrementAndGet()));
    }

    @Inject
    DomainObjectAuditingEnabledJdoEntities publishingEnabledJdoEntities;

    @Inject
    DomainObjectEntityChangePublishingDisabledJdoEntities publishingDisabledJdoEntities;

    @Inject
    DomainObjectEntityChangePublishingEnabledMetaAnnotatedJdoEntities publishingEnabledMetaAnnotatedJdoEntities;

    @Inject
    DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenJdoEntities publishingEnabledMetaAnnotOverriddenJdoEntities;
}
//end::class[]
