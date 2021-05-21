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
package org.apache.isis.core.metamodel.services.command;

import java.util.UUID;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.cmd.v2.ActionDto;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;

/**
 * Used to serialize the intention to invoke an action or edit a property as a
 * {@link CommandDto}, for example such that it can be persisted and then
 * executed at some later time or even against some external system.
 *
 * <p>
 *     There are some similarities to
 *     {@link org.apache.isis.core.metamodel.services.ixn.InteractionDtoFactory},
 *     which is used to instantiate an
 *     {@link org.apache.isis.schema.ixn.v2.InteractionDto} that represents
 *     the <i>actual</i> execution of the action invocation or property edit.
 * </p>
 *
 * @see org.apache.isis.applib.services.wrapper.WrapperFactory
 * @see org.apache.isis.core.metamodel.services.ixn.InteractionDtoFactory
 * @since 1.x {@index}
 */
public interface CommandDtoFactory {

    /**
     * Returns a {@link CommandDto} that represents the intention to invoke
     * an action on a target object (or target objects, to support the notion
     * of bulk actions).
     *
     * @see #asCommandDto(UUID, Can, OneToOneAssociation, ManagedObject)
     */
    CommandDto asCommandDto(
            final UUID interactionId,
            final Can<InteractionHead> targets,
            final ObjectAction objectAction,
            final Can<ManagedObject> argAdapters);

    /**
     * Returns a {@link CommandDto} that represents the intention to edit
     * (set or clear) a property on a target (or possibly many targets, for
     * symmetry with actions).
     *
     * @see #asCommandDto(UUID, Can, ObjectAction, Can)
     */
    CommandDto asCommandDto(
            final UUID interactionId,
            final Can<InteractionHead> targets,
            final OneToOneAssociation association,
            final ManagedObject valueAdapterOrNull);

    /**
     * Adds the arguments of an action to an {@link ActionDto} (the element
     * within a {@link CommandDto} representing an action invocation).
     *
     * <p>
     *     This is used when the command is actually executed
     *     to populate the parameters of the equivalent
     *     {@link org.apache.isis.schema.ixn.v2.ActionInvocationDto}
     * </p>
     *
     * @see org.apache.isis.schema.ixn.v2.ActionInvocationDto
     * @see org.apache.isis.applib.services.iactn.InteractionContext
     * @see org.apache.isis.applib.services.iactn.Interaction
     */
    void addActionArgs(
            final ObjectAction objectAction,
            final ActionDto actionDto,
            final Can<ManagedObject> argAdapters);

    /**
     * Adds the new value argument of a property to a {@link PropertyDto} (the
     * element a {@link CommandDto} representing an property edit).
     *
     * <p>
     *  This is used when the command is actually executed to set the the new
     *  value of the equivalent {@link org.apache.isis.schema.ixn.v2.PropertyEditDto}.
     * </p>
     *
     * @see org.apache.isis.schema.ixn.v2.PropertyEditDto
     * @see org.apache.isis.applib.services.iactn.InteractionContext
     * @see org.apache.isis.applib.services.iactn.Interaction
     */
    void addPropertyValue(
            final OneToOneAssociation property,
            final PropertyDto propertyDto,
            final ManagedObject valueAdapter);

}
