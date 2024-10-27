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

import java.util.Comparator;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.extensions.audittrail.applib.CausewayModuleExtAuditTrailApplib;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntry;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@Action(
        commandPublishing = Publishing.DISABLED,
        domainEvent = Object_createdByCommand.ActionDomainEvent.class,
        executionPublishing = Publishing.DISABLED,
        semantics = SemanticsOf.SAFE
)
@ActionLayout(
        cssClassFa = "fa-bolt",
        position = ActionLayout.Position.PANEL_DROPDOWN,
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        sequence = "900.4"
)
@RequiredArgsConstructor
public class Object_createdByCommand {

    private final Object domainObject;

    public static class ActionDomainEvent
            extends CausewayModuleExtAuditTrailApplib.ActionDomainEvent<Object_createdByCommand> {}

    @MemberSupport public Object act() {
        var commandIfAny = bookmarkService.bookmarksFor(domainObject)
                .stream()
                .map(target -> auditTrailEntryRepository.findFirstByTarget(target))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(AuditTrailEntry::getInteractionId)
                .map(x -> commandLogEntryRepository.findByInteractionId(x))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.comparing(CommandLogEntry::getTimestamp));

        return commandIfAny.isPresent()
                    ? commandIfAny.get()
                    : domainObject;
    }
    @MemberSupport public boolean hideAct() {
        var domainClass = domainObject.getClass();
        BeanSort beanSort = metaModelService.sortOf(domainClass, MetaModelService.Mode.RELAXED);
        return !beanSort.isEntity();
    }

    @Inject MetaModelService metaModelService;
    @Inject AuditTrailEntryRepository auditTrailEntryRepository;
    @Inject CommandLogEntryRepository commandLogEntryRepository;
    @Inject BookmarkService bookmarkService;
}
