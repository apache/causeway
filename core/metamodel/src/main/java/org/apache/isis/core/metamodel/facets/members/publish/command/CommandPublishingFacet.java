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
package org.apache.isis.core.metamodel.facets.members.publish.command;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command.CommandPublishingPhase;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.isis.core.metamodel.services.publishing.CommandPublisher;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

import lombok.NonNull;

/**
 * Indicates that details of the action invocation or property edit,
 * captured by a {@link Command},
 * should be dispatched via {@link CommandPublisher} to all subscribed
 * {@link CommandSubscriber}s.
 *
 * Corresponds to annotating the action method or property using
 * {@code @Action/@Property(commandPublishing=ENABLED)}
 *
 * @since 2.0
 */
public interface CommandPublishingFacet extends Facet {

    public CommandDtoProcessor getProcessor();

    public static boolean isPublishingEnabled(final @NonNull FacetHolder facetHolder) {
        return facetHolder.containsFacet(CommandPublishingFacet.class);
    }

    /**
     * Will set the command's CommandPublishingPhase to READY,
     * if command and objectMember have a matching member-id
     * and if the facetHoler has a CommandPublishingFacet (has commandPublishing=ENABLED).
     */
    public static void prepareCommandForPublishing(
            final @NonNull Command command,
            final @NonNull ObjectMember objectMember,
            final @NonNull FacetHolder facetHolder) {

        if(IdentifierUtil.isCommandForMember(command, objectMember)
                && isPublishingEnabled(facetHolder)) {
            command.updater().setPublishingPhase(CommandPublishingPhase.READY);
        }
    }
}
