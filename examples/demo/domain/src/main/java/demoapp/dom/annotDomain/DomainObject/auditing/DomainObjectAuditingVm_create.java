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
package demoapp.dom.annotDomain.DomainObject.auditing;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom.annotDomain.DomainObject.auditing.annotated.disabled.DomainObjectAuditingDisabledJdo;
import demoapp.dom.annotDomain.DomainObject.auditing.annotated.disabled.DomainObjectAuditingDisabledJdoEntities;
import demoapp.dom.annotDomain.DomainObject.auditing.annotated.enabled.DomainObjectAuditingEnabledJdo;
import demoapp.dom.annotDomain.DomainObject.auditing.annotated.enabled.DomainObjectAuditingEnabledJdoEntities;
import demoapp.dom.annotDomain.DomainObject.auditing.metaAnnot.enabled.DomainObjectAuditingEnabledMetaAnnotatedJdo;
import demoapp.dom.annotDomain.DomainObject.auditing.metaAnnot.enabled.DomainObjectAuditingEnabledMetaAnnotatedJdoEntities;
import demoapp.dom.annotDomain.DomainObject.auditing.metaAnnotOverridden.enabled.DomainObjectAuditingEnabledMetaAnnotOverriddenJdo;
import demoapp.dom.annotDomain.DomainObject.auditing.metaAnnotOverridden.enabled.DomainObjectAuditingEnabledMetaAnnotOverriddenJdoEntities;

//tag::class[]
@Action(semantics = SemanticsOf.NON_IDEMPOTENT)
@ActionLayout(
        describedAs = "Creates one publishing enabled entity and one publishing disabled entity"
)
public class DomainObjectAuditingVm_create {

    private final DomainObjectAuditingVm domainObjectAuditingVm;
    public DomainObjectAuditingVm_create(DomainObjectAuditingVm domainObjectAuditingVm) {
        this.domainObjectAuditingVm = domainObjectAuditingVm;
    }

    @MemberOrder(sequence = "1.0")
    public DomainObjectAuditingVm act(
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
    DomainObjectAuditingDisabledJdoEntities publishingDisabledJdoEntities;

    @Inject
    DomainObjectAuditingEnabledMetaAnnotatedJdoEntities publishingEnabledMetaAnnotatedJdoEntities;

    @Inject
    DomainObjectAuditingEnabledMetaAnnotOverriddenJdoEntities publishingEnabledMetaAnnotOverriddenJdoEntities;

    @Inject
    NameSamples nameSamples;
}
//end::class[]
