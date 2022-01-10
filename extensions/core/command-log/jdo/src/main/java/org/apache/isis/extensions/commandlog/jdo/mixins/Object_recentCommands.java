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
package org.apache.isis.extensions.commandlog.jdo.mixins;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.mixins.layout.LayoutMixinConstants;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.extensions.commandlog.jdo.IsisModuleExtCommandLogJdo;
import org.apache.isis.extensions.commandlog.jdo.entities.CommandJdo;
import org.apache.isis.extensions.commandlog.jdo.entities.CommandJdoRepository;

import lombok.RequiredArgsConstructor;

/**
 * This mixin contributes a <tt>recentCommands</tt> action to any domain object
 * (unless also {@link HasInteractionId} - commands don't themselves have commands).
 *
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = Object_recentCommands.ActionDomainEvent.class,
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING
)
@ActionLayout(
        cssClassFa = "fa-bolt",
        position = ActionLayout.Position.PANEL_DROPDOWN,
        associateWith = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME,
        sequence = "900.1"
)
@RequiredArgsConstructor
public class Object_recentCommands {

    public static class ActionDomainEvent
            extends IsisModuleExtCommandLogJdo.ActionDomainEvent<Object_recentCommands> { }

    private final Object domainObject; // mixee

    @MemberSupport public List<CommandJdo> act() {
        return bookmarkService.bookmarkFor(domainObject)
        .map(commandServiceRepository::findRecentByTarget)
        .orElse(Collections.emptyList());
    }

    /**
     * Hide if the mixee is itself {@link HasInteractionId}
     * (commands don't have commands).
     */
    @MemberSupport public boolean hideAct() {
        return (domainObject instanceof HasInteractionId);
    }

    @Inject CommandJdoRepository commandServiceRepository;
    @Inject BookmarkService bookmarkService;

}
