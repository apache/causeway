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
