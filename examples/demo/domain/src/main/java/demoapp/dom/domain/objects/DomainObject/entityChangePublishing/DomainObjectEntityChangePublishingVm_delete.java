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

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.disabled.DomainObjectEntityChangePublishingDisabledJdo;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.disabled.DomainObjectEntityChangePublishingDisabledJdoEntities;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.enabled.DomainObjectAuditingEnabledJdoEntities;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.enabled.DomainObjectEntityChangePublishingEnabledJdo;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnot.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotatedJdo;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnot.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotatedJdoEntities;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnotOverridden.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenJdo;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnotOverridden.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenJdoEntities;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@ActionLayout(
    describedAs = "Deletes one publishing enabled entity and one publishing disabled entity",
    sequence = "3.0")
public class DomainObjectEntityChangePublishingVm_delete {

    private final DomainObjectEntityChangePublishingVm domainObjectAuditingVm;
    public DomainObjectEntityChangePublishingVm_delete(DomainObjectEntityChangePublishingVm domainObjectAuditingVm) {
        this.domainObjectAuditingVm = domainObjectAuditingVm;
    }
    
    public DomainObjectEntityChangePublishingVm act(
            @Nullable DomainObjectEntityChangePublishingEnabledJdo enabledJdo
            , @Nullable DomainObjectEntityChangePublishingDisabledJdo disabledJdo
            , @Nullable DomainObjectEntityChangePublishingEnabledMetaAnnotatedJdo metaAnnotatedJdo
            , @Nullable DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenJdo metaAnnotOverriddenJdo
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
        return domainObjectAuditingVm;
    }
    public DomainObjectEntityChangePublishingEnabledJdo default0Act() {
        return publishingEnabledJdoEntities.first().get();
    }
    public DomainObjectEntityChangePublishingDisabledJdo default1Act() {
        return publishingDisabledJdoEntities.first().get();
    }
    public DomainObjectEntityChangePublishingEnabledMetaAnnotatedJdo default2Act() {
        return publishingEnabledMetaAnnotatedJdoEntities.first().get();
    }
    public DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenJdo default3Act() {
        return publishingEnabledMetaAnnotOverriddenJdoEntities.first().get();
    }
    public String disableAct() {
        if(!publishingEnabledJdoEntities.first().isPresent()) { return "No EnabledJdo to delete"; }
        if(!publishingDisabledJdoEntities.first().isPresent()) { return "No DisabledJdo to delete"; }
        if(!publishingEnabledMetaAnnotatedJdoEntities.first().isPresent()) { return "No MetaAnnotated to delete"; }
        if(!publishingEnabledMetaAnnotOverriddenJdoEntities.first().isPresent()) { return "No MetaAnnotated But Overridden to delete"; }
        return null;
    }

    @Inject DomainObjectAuditingEnabledJdoEntities publishingEnabledJdoEntities;
    @Inject DomainObjectEntityChangePublishingDisabledJdoEntities publishingDisabledJdoEntities;
    @Inject DomainObjectEntityChangePublishingEnabledMetaAnnotatedJdoEntities publishingEnabledMetaAnnotatedJdoEntities;
    @Inject DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenJdoEntities publishingEnabledMetaAnnotOverriddenJdoEntities;
}
//end::class[]
