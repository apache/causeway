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
package org.apache.causeway.core.runtimeservices.interaction;

import java.util.List;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.util.schema.InteractionDtoUtils;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.execution.InteractionInternal;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.services.command.CommandDtoFactory;
import org.apache.causeway.core.metamodel.services.ixn.InteractionDtoFactory;
import org.apache.causeway.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;
import org.apache.causeway.schema.ixn.v2.PropertyEditDto;

import lombok.val;

/**
* The design of this service is similar to
* {@link org.apache.causeway.core.runtimeservices.command.CommandDtoFactoryDefault}
*
* @see org.apache.causeway.core.runtimeservices.command.CommandDtoFactoryDefault
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".InteractionDtoServiceInternalDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class InteractionDtoFactoryDefault implements InteractionDtoFactory {

    @Inject private CommandDtoFactory commandDtoServiceInternal;
    @Inject private SchemaValueMarshaller valueMarshaller;
    @Inject private javax.inject.Provider<InteractionProvider> interactionProviderProvider;
    @Inject private UserService userService;

    @Override
    public ActionInvocationDto asActionInvocationDto(
            final ObjectAction objectAction,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters) {

        _Assert.assertEquals(objectAction.getParameterCount(), argumentAdapters.size(),
                "action's parameter count and provided argument count must match");

        val interaction = interactionProviderProvider.get().currentInteractionElseFail();
        final int nextEventSequence = ((InteractionInternal) interaction).getThenIncrementExecutionSequence();

        val owner = head.getOwner();

        // transient/detached entities have no bookmark, fail early
        val targetBookmark = ManagedObjects.bookmarkElseFail(owner);

        final String currentUser = userService.currentUserNameElseNobody();

        final ActionDto actionDto = new ActionDto();
        commandDtoServiceInternal.addActionArgs(head, objectAction, actionDto, argumentAdapters);
        final List<ParamDto> parameterDtos = CommandDtoUtils.parametersFor(actionDto).getParameter();

        return InteractionDtoUtils.newActionInvocation(
                nextEventSequence, targetBookmark,
                actionDto.getLogicalMemberIdentifier(),
                parameterDtos, currentUser
                );
    }

    @Override
    public ActionInvocationDto updateResult(
            final ActionInvocationDto actionInvocationDto,
            final ObjectAction objectAction,
            final ManagedObject resultObject) {

        val elementSpec = objectAction.getElementType();

        if(objectAction.getReturnType().isSingular()) {
            //scalar
            valueMarshaller.recordActionResultScalar(actionInvocationDto, objectAction, resultObject);
        } else {
            //non-scalar
            val values = ManagedObjects.unpack(elementSpec, resultObject);
            valueMarshaller.recordActionResultNonScalar(actionInvocationDto, objectAction, values);
        }
        return actionInvocationDto;
    }


    @Override
    public PropertyEditDto asPropertyEditDto(
            final OneToOneAssociation property,
            final ManagedObject targetAdapter,
            final ManagedObject newValueAdapterIfAny,
            final InteractionHead interactionHead) {

        final Interaction interaction = interactionProviderProvider.get().currentInteractionElseFail();
        final int nextEventSequence = ((InteractionInternal) interaction).getThenIncrementExecutionSequence();

        // transient/detached entities have no bookmark, fail early
        val targetBookmark = ManagedObjects.bookmarkElseFail(targetAdapter);

        final String currentUser = userService.currentUserNameElseNobody();

        final PropertyDto propertyDto = new PropertyDto();
        commandDtoServiceInternal.addPropertyValue(interactionHead, property, propertyDto, newValueAdapterIfAny);
        final ValueWithTypeDto newValue = propertyDto.getNewValue();

        return InteractionDtoUtils.newPropertyEdit(
                nextEventSequence, targetBookmark,
                propertyDto.getLogicalMemberIdentifier(),
                newValue, currentUser
                );
    }

}
