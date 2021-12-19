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
package org.apache.isis.core.runtimeservices.interaction;

import java.util.List;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.execution.InteractionInternal;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.services.command.CommandDtoFactory;
import org.apache.isis.core.metamodel.services.ixn.InteractionDtoFactory;
import org.apache.isis.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.cmd.v2.ActionDto;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;
import org.apache.isis.schema.ixn.v2.PropertyEditDto;

import lombok.val;

/**
* The design of this service is similar to
* {@link org.apache.isis.core.runtimeservices.command.CommandDtoFactoryDefault}
*
* @see org.apache.isis.core.runtimeservices.command.CommandDtoFactoryDefault
 */
@Service
@Named("isis.runtimeservices.InteractionDtoServiceInternalDefault")
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
        final Bookmark targetBookmark = owner.getBookmark()
                .orElseThrow(()->_Exceptions.noSuchElement("Object provides no Bookmark: %s", owner));

        final String actionId = objectAction.getFeatureIdentifier().getMemberNameAndParameterClassNamesIdentityString();
        final String targetTitle = targetBookmark.toString() + ": " + actionId;

        final String currentUser = userService.currentUserNameElseNobody();

        final ActionDto actionDto = new ActionDto();
        commandDtoServiceInternal.addActionArgs(objectAction, actionDto, argumentAdapters);
        final List<ParamDto> parameterDtos = CommandDtoUtils.parametersFor(actionDto).getParameter();

        return InteractionDtoUtils.newActionInvocation(
                nextEventSequence, targetBookmark, targetTitle,
                actionDto.getMemberIdentifier(),
                parameterDtos, currentUser
                );
    }

    @Override
    public ActionInvocationDto updateResult(
            final ActionInvocationDto actionInvocationDto,
            final ObjectAction objectAction,
            final ManagedObject resultObject) {

        val elementSpec = objectAction.getElementType();

        if(objectAction.getReturnType().isScalar()) {
            //scalar
            valueMarshaller.recordActionResultScalar(actionInvocationDto, elementSpec, resultObject);
        } else {
            //non-scalar
            val values = ManagedObjects.unpack(elementSpec, resultObject);
            valueMarshaller.recordActionResultNonScalar(actionInvocationDto, elementSpec, values);
        }
        return actionInvocationDto;
    }


    @Override
    public PropertyEditDto asPropertyEditDto(
            final OneToOneAssociation property,
            final ManagedObject targetAdapter,
            final ManagedObject newValueAdapterIfAny) {

        final Interaction interaction = interactionProviderProvider.get().currentInteractionElseFail();
        final int nextEventSequence = ((InteractionInternal) interaction).getThenIncrementExecutionSequence();

        final Bookmark targetBookmark = targetAdapter.getBookmark()
                .orElseThrow(()->_Exceptions.noSuchElement("Object provides no Bookmark: %s", targetAdapter));

        final String propertyId = property.getFeatureIdentifier().getMemberLogicalName();
        final String targetTitle = targetBookmark.toString() + ": " + propertyId;

        final String currentUser = userService.currentUserNameElseNobody();

        final PropertyDto propertyDto = new PropertyDto();
        commandDtoServiceInternal.addPropertyValue(property, propertyDto, newValueAdapterIfAny);
        final ValueWithTypeDto newValue = propertyDto.getNewValue();

        return InteractionDtoUtils.newPropertyEdit(
                nextEventSequence, targetBookmark, targetTitle,
                propertyDto.getMemberIdentifier(),
                newValue, currentUser
                );
    }

}
