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

import java.util.Objects;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.services.publishing.CommandPublisher;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

import lombok.NonNull;
import lombok.val;

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

        val commandFacet = facetHolder.getFacet(CommandPublishingFacet.class);
        if(commandFacet!=null) {
            return true;
        }
        return false;
    }

    /**
     * Will only run the runnable, if command and objectMember have a matching member-id
     * and if the facetHoler has a CommandPublishingFacet.
     */
    public static void ifPublishingEnabledForCommand(
            final @NonNull Command command,
            final @NonNull ObjectMember objectMember,
            final @NonNull FacetHolder facetHolder,
            final @NonNull Runnable runnable) {

        val memberId1 = objectMember.getIdentifier().getLogicalIdentityString("#");
        val memberId2 = command.getLogicalMemberIdentifier();

        if(Objects.equals(memberId1, memberId2)
                && isPublishingEnabled(facetHolder)) {
            runnable.run();
        }
    }
}
