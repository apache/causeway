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
package org.apache.isis.extensions.commandlog.impl.mixins;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.mixins.layout.LayoutMixinConstants;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;

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
        associateWith = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME,
        restrictTo = RestrictTo.PROTOTYPING
)
@ActionLayout(
        cssClassFa = "fa-bolt",
        position = ActionLayout.Position.PANEL_DROPDOWN,
        sequence = "900.1"
)
public class Object_recentCommands {

    public static class ActionDomainEvent
            extends IsisModuleExtCommandLogImpl.ActionDomainEvent<Object_recentCommands> { }

    private final Object domainObject;
    public Object_recentCommands(final Object domainObject) {
        this.domainObject = domainObject;
    }

    public List<CommandJdo> act() {
        return bookmarkService.bookmarkFor(domainObject)
        .map(commandServiceRepository::findRecentByTarget)
        .orElse(Collections.emptyList());
    }
    /**
     * Hide if the contributee is itself {@link HasInteractionId}
     * (commands don't have commands).
     */
    public boolean hideAct() {
        return (domainObject instanceof HasInteractionId);
    }

    @Inject CommandJdoRepository commandServiceRepository;
    @Inject BookmarkService bookmarkService;

}
