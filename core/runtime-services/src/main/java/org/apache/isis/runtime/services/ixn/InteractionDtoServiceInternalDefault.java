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

package org.apache.isis.runtime.services.ixn;

import lombok.extern.log4j.Log4j2;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.metamodel.services.ixn.InteractionDtoServiceInternal;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.cmd.v1.PropertyDto;
import org.apache.isis.schema.common.v1.ValueWithTypeDto;
import org.apache.isis.schema.ixn.v1.ActionInvocationDto;
import org.apache.isis.schema.ixn.v1.PropertyEditDto;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Named("isisRuntimeServices.InteractionDtoServiceInternalDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class InteractionDtoServiceInternalDefault implements InteractionDtoServiceInternal {

    @Inject CommandDtoServiceInternal commandDtoServiceInternal;
    @Inject private BookmarkService bookmarkService;
    @Inject private InteractionContext interactionContext;
    @Inject private UserService userService;
    
    @Override
    public ActionInvocationDto asActionInvocationDto(
            final ObjectAction objectAction,
            final ManagedObject targetAdapter,
            final List<ManagedObject> argumentAdapters) {

        final Interaction interaction = interactionContext.getInteraction();
        final int nextEventSequence = interaction.next(Interaction.Sequence.INTERACTION.id());

        final Object targetPojo = targetAdapter.getPojo();
        final Bookmark targetBookmark = bookmarkService.bookmarkForElseThrow(targetPojo);

        final String actionIdentifier = objectAction.getIdentifier().toClassAndNameIdentityString();
        final String actionId = actionIdentifier.substring(actionIdentifier.indexOf('#')+1);
        final String targetTitle = targetBookmark.toString() + ": " + actionId;

        final String currentUser = userService.getUser().getName();

        final ActionDto actionDto = new ActionDto();
        commandDtoServiceInternal.addActionArgs(
                objectAction, actionDto, argumentAdapters.toArray(new ManagedObject[]{}));
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
            final Object resultPojo) {

        final ObjectSpecification returnSpec = objectAction.getReturnType();
        final Class<?> returnType = returnSpec.getCorrespondingClass();

        InteractionDtoUtils.addReturn(
                actionInvocationDto, returnType, resultPojo, bookmarkService);

        return actionInvocationDto;
    }

    @Override
    public PropertyEditDto asPropertyEditDto(
            final OneToOneAssociation property,
            final ManagedObject targetAdapter,
            final ManagedObject newValueAdapterIfAny) {

        final Interaction interaction = interactionContext.getInteraction();

        final int nextEventSequence = interaction.next(Interaction.Sequence.INTERACTION.id());

        final Object targetPojo = targetAdapter.getPojo();
        final Bookmark targetBookmark = bookmarkService.bookmarkForElseThrow(targetPojo);

        final String propertyIdentifier = property.getIdentifier().toClassAndNameIdentityString();
        final String propertyId = propertyIdentifier.substring(propertyIdentifier.indexOf('#')+1);
        final String targetTitle = targetBookmark.toString() + ": " + propertyId;

        final String currentUser = userService.getUser().getName();

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
