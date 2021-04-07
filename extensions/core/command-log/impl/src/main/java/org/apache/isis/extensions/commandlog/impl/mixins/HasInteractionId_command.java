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

import java.util.UUID;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;

import lombok.RequiredArgsConstructor;


/**
 * This mixin contributes a <tt>command</tt> action to any (non-command) implementation of
 * {@link HasInteractionId}; that is: audit entries, and published events.  Thus, it
 * is possible to navigate from the effect back to the cause.
 *
 * @since 2.0 {@index}
 */
@Action(
    semantics = SemanticsOf.SAFE
    , domainEvent = HasInteractionId_command.ActionDomainEvent.class,
    associateWith = "interactionId"
)
@ActionLayout(sequence="1")
@RequiredArgsConstructor
public class HasInteractionId_command {

    public static class ActionDomainEvent
            extends IsisModuleExtCommandLogImpl.ActionDomainEvent<HasInteractionId_command> { }

    private final HasInteractionId hasInteractionId;

    public CommandJdo act() {
        return findCommand();
    }
    /**
     * Hide if the contributee is a {@link Command}, because {@link Command}s already have a
     * {@link Command#getParent() parent} property.
     */
    public boolean hideAct() {
        return (hasInteractionId instanceof CommandJdo);
    }
    public String disableAct() {
        return findCommand() == null ? "No command found for unique Id": null;
    }

    private CommandJdo findCommand() {
        final UUID transactionId = hasInteractionId.getInteractionId();
        return commandServiceRepository
                .findByInteractionId(transactionId)
                .orElse(null);
    }

    @Inject CommandJdoRepository commandServiceRepository;
}
