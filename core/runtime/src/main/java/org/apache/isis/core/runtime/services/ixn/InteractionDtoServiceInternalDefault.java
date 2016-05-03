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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.services.ixn.InteractionDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.ixn.v1.ActionInvocationDto;
import org.apache.isis.schema.ixn.v1.PropertyModificationDto;
import org.apache.isis.schema.ixn.v1.ValueWithTypeDto;
import org.apache.isis.schema.utils.InteractionDtoUtils;

@DomainService(nature = NatureOfService.DOMAIN)
public class InteractionDtoServiceInternalDefault implements InteractionDtoServiceInternal {


    @Override @Programmatic
    public ActionInvocationDto asActionInvocationDto(
            final ObjectAction objectAction,
            final ObjectAdapter targetAdapter,
            final List<ObjectAdapter> parameterAdapters,
            final ObjectAdapter resultAdapter) {

        final Command command = commandContext.getCommand();
        final UUID transactionId = command.getTransactionId();

        final Interaction.SequenceName sequenceName = Interaction.SequenceName.PUBLISHED_EVENT;
        final int nextEventSequence = command.next(sequenceName.abbr());

        final Object targetPojo = targetAdapter.getObject();
        final Bookmark targetBookmark = bookmarkService.bookmarkFor(targetPojo);

        final String actionClassNameId = objectAction.getIdentifier().toClassAndNameIdentityString();
        final String actionId = actionClassNameId.substring(actionClassNameId.indexOf('#')+1);
        final String targetTitle = targetBookmark.toString() + ": " + actionId;

        final String currentUser = userService.getUser().getName();

        final ActionDto actionDto = new ActionDto();
        commandDtoServiceInternal.addActionArgs(
                objectAction, actionDto, parameterAdapters.toArray(new ObjectAdapter[]{}));

        final ObjectSpecification returnSpec = objectAction.getReturnType();

        final Class<?> returnType = returnSpec.getCorrespondingClass();
        final Object resultPojo = resultAdapter != null? resultAdapter.getObject(): null;

        final ValueWithTypeDto returnDto = new ValueWithTypeDto();
        InteractionDtoUtils.setValue(returnDto, returnType, resultPojo);

        final String transactionIdStr = transactionId.toString();

        // done above, I believe
        // InteractionDtoUtils.addReturn(invocationDto, returnType, resultPojo, bookmarkService);
        return InteractionDtoUtils.newActionInvocation(
                nextEventSequence, targetBookmark, targetTitle,
                actionDto.getMemberIdentifier(), actionDto.getParameters(), returnDto, currentUser,
                transactionIdStr);
    }

    @Override @Programmatic
    public PropertyModificationDto asPropertyModificationDto(
            final OneToOneAssociation property,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter newValueAdapterIfAny) {

        //
        //
        //

        throw new RuntimeException("not yet implemented");
    }


    @Inject
    CommandDtoServiceInternal commandDtoServiceInternal;

    @Inject
    private BookmarkService bookmarkService;

    @Inject
    private CommandContext commandContext;

    @Inject
    private ClockService clockService;

    @Inject
    private UserService userService;


}
