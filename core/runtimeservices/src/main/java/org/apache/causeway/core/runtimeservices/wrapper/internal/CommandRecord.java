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
package org.apache.causeway.core.runtimeservices.wrapper.internal;

import java.util.UUID;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.runtimeservices.session.InteractionIdGenerator;
import org.apache.causeway.schema.cmd.v2.CommandDto;

public record CommandRecord(
        /**
         * The unique {@link Command#getInteractionId() interactionId} of the parent {@link Command}, that is the
         * {@link Command} that was active in the original interaction where
         * {@link org.apache.causeway.applib.services.wrapper.WrapperFactory#asyncWrap(Object, AsyncControl)} (or its brethren)
         * was called.
         *
         * <p>This can be useful for custom implementations of {@link ExecutorService} that use the commandlog
         * extension's <code>CommandLogEntry</code>, to link parent and child commands together.
         */
        UUID parentInteractionId,
        InteractionContext interactionContext,
        /**
         * Details of the actual child command (action or property edit) to be performed.
         *
         * <p>(Ultimately this is handed onto the {@link org.apache.causeway.applib.services.command.CommandExecutorService}).
         */
         CommandDto commandDto) {

    // -- FACTORY

    public record Factory(
            InteractionIdGenerator interactionIdGenerator) {

        public CommandRecord forAction(InteractionHead head, ObjectAction act, Can<ManagedObject> args) {
            return new CommandRecord(
                currentParentInteractionId(act),
                currentInteractionContext(act),
                act.getCommandDtoFactory().asCommandDto(interactionIdGenerator.interactionId(), head, act, args));
        }

        public CommandRecord forProperty(InteractionHead head, OneToOneAssociation prop, ManagedObject arg) {
            return new CommandRecord(
                currentParentInteractionId(prop),
                currentInteractionContext(prop),
                prop.getCommandDtoFactory().asCommandDto(interactionIdGenerator.interactionId(), head, prop, arg));
        }

        // -- HELPER

        private static InteractionContext currentInteractionContext(HasMetaModelContext mmc) {
            return mmc.getInteractionService().currentInteractionContextElseFail();
        }
        private static UUID currentParentInteractionId(HasMetaModelContext mmc) {
            return mmc.getInteractionService().currentInteractionElseFail()
                    .getCommand()
                    .getInteractionId();
        }

    }

}
