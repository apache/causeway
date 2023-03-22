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

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.disabled.DomainObjectEntityChangePublishingDisabledEntity;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.enabled.DomainObjectEntityChangePublishingEnabledEntity;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnot.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotatedEntity;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnotOverridden.enabled.DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenEntity;
import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@ActionLayout(
    describedAs = "Deletes one publishing enabled entity and one publishing disabled entity",
    sequence = "3.0")
@RequiredArgsConstructor
public class DomainObjectEntityChangePublishingVm_delete {

    private final DomainObjectEntityChangePublishingVm domainObjectAuditingVm;

    @MemberSupport public DomainObjectEntityChangePublishingVm act(
            @Nullable final DomainObjectEntityChangePublishingEnabledEntity enabledEntity
            , @Nullable final DomainObjectEntityChangePublishingDisabledEntity disabledEntity
            , @Nullable final DomainObjectEntityChangePublishingEnabledMetaAnnotatedEntity metaAnnotatedEntity
            , @Nullable final DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenEntity metaAnnotOverriddenEntity
            ) {
        if(enabledEntity != null) {
            publishingEnabledEntities.remove(enabledEntity);
        }
        if(disabledEntity != null) {
            publishingDisabledEntities.remove(disabledEntity);
        }
        if(metaAnnotatedEntity != null) {
            publishingEnabledMetaAnnotatedEntities.remove(metaAnnotatedEntity);
        }
        if(metaAnnotOverriddenEntity != null) {
            publishingEnabledMetaAnnotOverriddenEntities.remove(metaAnnotOverriddenEntity);
        }
        return domainObjectAuditingVm;
    }
    @MemberSupport public DomainObjectEntityChangePublishingEnabledEntity default0Act() {
        return publishingEnabledEntities.first().orElse(null);
    }
    @MemberSupport public List<? extends DomainObjectEntityChangePublishingEnabledEntity> choices0Act() {
        return listOf(default0Act());
    }
    @MemberSupport public DomainObjectEntityChangePublishingDisabledEntity default1Act() {
        return publishingDisabledEntities.first().orElse(null);
    }
    @MemberSupport public List<? extends DomainObjectEntityChangePublishingDisabledEntity> choices1Act() {
        return listOf(default1Act());
    }
    @MemberSupport public DomainObjectEntityChangePublishingEnabledMetaAnnotatedEntity default2Act() {
        return publishingEnabledMetaAnnotatedEntities.first().orElse(null);
    }
    @MemberSupport public List<? extends DomainObjectEntityChangePublishingEnabledMetaAnnotatedEntity> choices2Act() {
        return listOf(default2Act());
    }
    @MemberSupport public DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenEntity default3Act() {
        return publishingEnabledMetaAnnotOverriddenEntities.first().orElse(null);
    }
    @MemberSupport public List<? extends DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenEntity> choices3Act() {
        return listOf(default3Act());
    }
    @MemberSupport public String disableAct() {
        if(publishingEnabledEntities.first().isEmpty()) { return "No Enabled entity to delete"; }
        if(publishingDisabledEntities.first().isEmpty()) { return "No Disabled entity to delete"; }
        if(publishingEnabledMetaAnnotatedEntities.first().isEmpty()) { return "No MetaAnnotated entity to delete"; }
        if(publishingEnabledMetaAnnotOverriddenEntities.first().isEmpty()) { return "No MetaAnnotated But Overridden entity to delete"; }
        return null;
    }

    private static <T> List<? extends T> listOf(final T t) {
        return t == null ? Collections.emptyList() : Collections.singletonList(t);
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingEnabledEntity> publishingEnabledEntities;
    @Inject ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingDisabledEntity> publishingDisabledEntities;
    @Inject ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingEnabledMetaAnnotatedEntity> publishingEnabledMetaAnnotatedEntities;
    @Inject ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenEntity> publishingEnabledMetaAnnotOverriddenEntities;

}
//end::class[]
