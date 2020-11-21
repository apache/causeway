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
package demoapp.dom.annotDomain.DomainObject.entityChangePublishing;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom.annotDomain.DomainObject.entityChangePublishing.annotated.disabled.DomainObjectEntityChangePublishingDisabledJdo;
import demoapp.dom.annotDomain.DomainObject.entityChangePublishing.annotated.disabled.DomainObjectEntityChangePublishingDisabledJdoEntities;
import demoapp.dom.annotDomain.DomainObject.entityChangePublishing.annotated.enabled.DomainObjectEntityChangePublishingEnabledJdo;
import demoapp.dom.annotDomain.DomainObject.entityChangePublishing.annotated.enabled.DomainObjectAuditingEnabledJdoEntities;
import demoapp.dom.annotDomain.DomainObject.entityChangePublishing.metaAnnot.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotatedJdo;
import demoapp.dom.annotDomain.DomainObject.entityChangePublishing.metaAnnot.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotatedJdoEntities;
import demoapp.dom.annotDomain.DomainObject.entityChangePublishing.metaAnnotOverridden.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenJdo;
import demoapp.dom.annotDomain.DomainObject.entityChangePublishing.metaAnnotOverridden.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenJdoEntities;

//tag::class[]
@Action(semantics = SemanticsOf.NON_IDEMPOTENT)
@ActionLayout(
        describedAs = "Creates one publishing enabled entity and one publishing disabled entity"
)
public class DomainObjectEntityChangePublishingVm_create {

    private final DomainObjectEntityChangePublishingVm domainObjectAuditingVm;
    public DomainObjectEntityChangePublishingVm_create(DomainObjectEntityChangePublishingVm domainObjectAuditingVm) {
        this.domainObjectAuditingVm = domainObjectAuditingVm;
    }

    @MemberOrder(sequence = "1.0")
    public DomainObjectEntityChangePublishingVm act(
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
        return domainObjectAuditingVm;
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
    DomainObjectAuditingEnabledJdoEntities publishingEnabledJdoEntities;

    @Inject
    DomainObjectEntityChangePublishingDisabledJdoEntities publishingDisabledJdoEntities;

    @Inject
    DomainObjectEntityChangePublishingEnabledMetaAnnotatedJdoEntities publishingEnabledMetaAnnotatedJdoEntities;

    @Inject
    DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenJdoEntities publishingEnabledMetaAnnotOverriddenJdoEntities;

    @Inject
    NameSamples nameSamples;
}
//end::class[]
