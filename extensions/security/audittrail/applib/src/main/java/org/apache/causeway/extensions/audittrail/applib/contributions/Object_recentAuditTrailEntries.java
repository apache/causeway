/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.causeway.extensions.audittrail.applib.contributions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.extensions.audittrail.applib.CausewayModuleExtAuditTrailApplib;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntry;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@Action(
        commandPublishing = Publishing.DISABLED,
        domainEvent = Object_recentAuditTrailEntries.ActionDomainEvent.class,
        executionPublishing = Publishing.DISABLED,
        semantics = SemanticsOf.SAFE
)
@ActionLayout(
        cssClassFa = "fa-bolt",
        position = ActionLayout.Position.PANEL_DROPDOWN,
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        sequence = "900.3"
)
@RequiredArgsConstructor
public class Object_recentAuditTrailEntries {

    private final Object domainObject;

    public static class ActionDomainEvent
            extends CausewayModuleExtAuditTrailApplib.ActionDomainEvent<Object_recentAuditTrailEntries> {}

    @MemberSupport public List<? extends AuditTrailEntry> act(
            final String propertyName) {
        List<AuditTrailEntry> auditTrailEntries = new ArrayList<>();
        bookmarkService.bookmarksFor(domainObject).forEach(
                bookmark -> {
                    List<AuditTrailEntry> recent = auditTrailEntryRepository.findRecentByTargetAndPropertyId(bookmark, propertyName);
                    auditTrailEntries.addAll(recent);
                });
        auditTrailEntries.sort(Comparator.comparing(AuditTrailEntry::getTimestamp).reversed());
        return auditTrailEntries;
    }
    @MemberSupport public List<String> choices0Act() {
        var domainClass = domainObject.getClass();
        var logicalTypeIfAny = metaModelService.lookupLogicalTypeByClass(domainClass);
        if(logicalTypeIfAny.isEmpty()) {
            // not expected, due to hide guard
            return Collections.emptyList();
        }
        var propertyFeatureIds = applicationFeatureRepository.propertyIdsFor(logicalTypeIfAny.get());
        return propertyFeatureIds.stream().map(ApplicationFeatureId::getLogicalMemberName).collect(Collectors.toList());
    }
    @MemberSupport public String default0Act() {
        var choices = choices0Act();
        return choices.size() == 1 ? choices.get(0): null;
    }
    @MemberSupport public boolean hideAct() {
        var domainClass = domainObject.getClass();
        BeanSort beanSort = metaModelService.sortOf(domainClass, MetaModelService.Mode.RELAXED);
        return !beanSort.isEntity();
    }

    @Inject MetaModelService metaModelService;
    @Inject ApplicationFeatureRepository applicationFeatureRepository;
    @Inject AuditTrailEntryRepository auditTrailEntryRepository;
    @Inject BookmarkService bookmarkService;
}
