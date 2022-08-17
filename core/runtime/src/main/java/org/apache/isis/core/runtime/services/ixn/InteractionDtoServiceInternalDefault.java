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

package org.apache.isis.core.runtime.services.ixn;

import java.util.List;
import java.util.UUID;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.services.ixn.InteractionDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.cmd.v1.PropertyDto;
import org.apache.isis.schema.common.v1.ValueWithTypeDto;
import org.apache.isis.schema.ixn.v1.ActionInvocationDto;
import org.apache.isis.schema.ixn.v1.PropertyEditDto;
import org.apache.isis.schema.utils.CommandDtoUtils;
import org.apache.isis.schema.utils.InteractionDtoUtils;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
)
public class InteractionDtoServiceInternalDefault implements InteractionDtoServiceInternal {


    @Override @Programmatic
    public ActionInvocationDto asActionInvocationDto(
            final ObjectAction objectAction,
            final ObjectAdapter targetAdapter,
            final List<ObjectAdapter> argumentAdapters) {

        final Interaction interaction = interactionContext.getInteraction();
        final UUID transactionId = interaction.getTransactionId();

        final int nextEventSequence = interaction.next(Interaction.Sequence.INTERACTION.id());

        final Object targetPojo = targetAdapter.getObject();
        final Bookmark targetBookmark = bookmarkService.bookmarkFor(targetPojo)
                .orElseThrow(() -> new NonRecoverableException("Bookmark not found for pojo "+targetPojo));

        final String actionIdentifier = objectAction.getIdentifier().toClassAndNameIdentityString();
        final String actionId = actionIdentifier.substring(actionIdentifier.indexOf('#')+1);
        final String targetTitle = targetBookmark.toString() + ": " + actionId;

        final String currentUser = userService.getUser().getName();

        final ActionDto actionDto = new ActionDto();
        commandDtoServiceInternal.addActionArgs(
                objectAction, actionDto, argumentAdapters.toArray(new ObjectAdapter[]{}));
        final List<ParamDto> parameterDtos = CommandDtoUtils.parametersFor(actionDto).getParameter();

        final String transactionIdStr = transactionId.toString();

        return InteractionDtoUtils.newActionInvocation(
                nextEventSequence, targetBookmark, targetTitle,
                actionDto.getMemberIdentifier(),
                parameterDtos, currentUser
        );
    }

    @Override @Programmatic
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

    @Override @Programmatic
    public PropertyEditDto asPropertyEditDto(
            final OneToOneAssociation property,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter newValueAdapterIfAny) {

        final Interaction interaction = interactionContext.getInteraction();

        final int nextEventSequence = interaction.next(Interaction.Sequence.INTERACTION.id());

        final Object targetPojo = targetAdapter.getObject();
        final Bookmark targetBookmark = bookmarkService.bookmarkFor(targetPojo)
                .orElseThrow(() -> new NonRecoverableException("Bookmark not found for pojo "+targetPojo));

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

    @javax.inject.Inject
    CommandDtoServiceInternal commandDtoServiceInternal;

    @javax.inject.Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private InteractionContext interactionContext;

    @javax.inject.Inject
    private UserService userService;


}
