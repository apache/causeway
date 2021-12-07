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
package org.apache.isis.core.metamodel.services.ixn;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;
import org.apache.isis.schema.ixn.v2.PropertyEditDto;

/**
 * Used to serialize the execution of an action invocation or property edit as
 * an {@link org.apache.isis.schema.ixn.v2.InteractionDto}, for example that
 * it can be published to downstream systems.
 *
 * <p>
 *     There are some similarities to
 *     {@link org.apache.isis.core.metamodel.services.command.CommandDtoFactory},
 *     which is used to instantiate an
 *     {@link org.apache.isis.schema.cmd.v2.CommandDto} that represents
 *     the <i>intention</i> to invoke an action or edit a property.
 * </p>
 *
 * @see org.apache.isis.core.metamodel.services.command.CommandDtoFactory
 * @since 1.x {@index}
 */
public interface InteractionDtoFactory {


    /**
     * Called by the framework when invoking an action, to create an
     * {@link ActionInvocationDto} capturing the details of the action
     * invocation (target, arguments etc).
     *
     * <p>
     *     {@link ActionInvocationDto} is a subtype of
     *     {@link org.apache.isis.schema.ixn.v2.MemberExecutionDto} that is
     *     in turn a part of the overall
     *     {@link org.apache.isis.schema.ixn.v2.InteractionDto}.
     * </p>
     *
     * @see org.apache.isis.schema.ixn.v2.MemberExecutionDto
     * @see org.apache.isis.schema.ixn.v2.InteractionDto
     * @see PropertyEditDto
     */
    ActionInvocationDto asActionInvocationDto(
            ObjectAction objectAction,
            InteractionHead head,
            Can<ManagedObject> argumentAdapters);

    /**
     * Called by the framework when editing a property, to create an
     * {@link PropertyEditDto} capturing the details of the action
     * invocation (target, arguments etc).
     *
     * <p>
     *     {@link PropertyEditDto} is a subtype of
     *     {@link org.apache.isis.schema.ixn.v2.MemberExecutionDto} that is
     *     in turn a part of the overall
     *     {@link org.apache.isis.schema.ixn.v2.InteractionDto}.
     * </p>
     *
     * @see org.apache.isis.schema.ixn.v2.MemberExecutionDto
     * @see org.apache.isis.schema.ixn.v2.InteractionDto
     * @see ActionInvocationDto
     */
    PropertyEditDto asPropertyEditDto(
            OneToOneAssociation property,
            ManagedObject targetAdapter,
            ManagedObject newValueAdapterIfAny);

    /**
     * Called by the framework to attach the result of an action invocation
     * to the aforementioned {@link ActionInvocationDto}.
     */
    ActionInvocationDto updateResult(
            ActionInvocationDto actionInvocationDto,
            ObjectAction objectAction,
            ManagedObject resultObject);


}
