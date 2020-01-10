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
package org.apache.isis.runtimeservices.command;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.cmd.v1.CommandDto;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.cmd.v1.ParamsDto;
import org.apache.isis.schema.cmd.v1.PropertyDto;
import org.apache.isis.schema.common.v1.InteractionType;
import org.apache.isis.schema.common.v1.OidsDto;
import org.apache.isis.schema.common.v1.ValueWithTypeDto;

import lombok.val;

@Service
@Named("isisRuntimeServices.CommandDtoServiceInternalDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class CommandDtoServiceInternalDefault implements CommandDtoServiceInternal {

    @Inject private CommandContext commandContext;
    @Inject private BookmarkService bookmarkService;

    @Override
    public CommandDto asCommandDto(
            final List<ManagedObject> targetAdapters,
            final ObjectAction objectAction,
            final List<ManagedObject> argAdapters) {

        final CommandDto dto = asCommandDto(targetAdapters);

        final ActionDto actionDto = new ActionDto();
        actionDto.setInteractionType(InteractionType.ACTION_INVOCATION);
        dto.setMember(actionDto);

        addActionArgs(objectAction, actionDto, argAdapters);

        return dto;
    }

    @Override
    public CommandDto asCommandDto(
            final List<ManagedObject> targetAdapters,
            final OneToOneAssociation property,
            final ManagedObject valueAdapterOrNull) {

        final CommandDto dto = asCommandDto(targetAdapters);

        final PropertyDto propertyDto = new PropertyDto();
        propertyDto.setInteractionType(InteractionType.PROPERTY_EDIT);
        dto.setMember(propertyDto);

        addPropertyValue(property, propertyDto, valueAdapterOrNull);

        return dto;
    }

    private CommandDto asCommandDto(final List<ManagedObject> targetAdapters) {
        final CommandDto dto = new CommandDto();
        dto.setMajorVersion("1");
        dto.setMinorVersion("0");

        String transactionId = determineTransactionId().toString();
        dto.setTransactionId(transactionId);

        for (val targetAdapter : targetAdapters) {
            final RootOid rootOid = (RootOid) ManagedObject._identify(targetAdapter);
            final Bookmark bookmark = rootOid.asBookmark();
            final OidsDto targets = CommandDtoUtils.targetsFor(dto);
            targets.getOid().add(bookmark.toOidDto());
        }
        return dto;
    }

    protected UUID determineTransactionId() {
        Command command = commandContext.getCommand();
        if (command != null && command.getUniqueId() != null) {
            return command.getUniqueId();
        } else {
            return UUID.randomUUID();
        }
    }

    @Override
    public void addActionArgs(
            final ObjectAction objectAction,
            final ActionDto actionDto,
            final List<ManagedObject> argAdapters) {
        
        final String actionId = CommandUtil.memberIdentifierFor(objectAction);
        final ObjectSpecification onType = objectAction.getOnType();
        final String objectType = onType.getSpecId().asString();
        final String localId = objectAction.getIdentifier().toNameIdentityString();
        actionDto.setLogicalMemberIdentifier(objectType + "#" + localId);
        actionDto.setMemberIdentifier(actionId);

        val actionParameters = objectAction.getParameters();
        for (int paramNum = 0; paramNum < actionParameters.size(); paramNum++) {
            final ObjectActionParameter actionParameter = actionParameters.getOrThrow(paramNum);
            final String parameterName = actionParameter.getName();
            final Class<?> paramType = actionParameter.getSpecification().getCorrespondingClass();
            final ManagedObject argAdapter = argAdapters.get(paramNum);
            final Object arg = argAdapter != null? argAdapter.getPojo(): null;
            final ParamsDto parameters = CommandDtoUtils.parametersFor(actionDto);
            final List<ParamDto> parameterList = parameters.getParameter();

            ParamDto paramDto = CommonDtoUtils.newParamDto(
                    parameterName, paramType, arg, bookmarkService);
            parameterList.add(paramDto);
        }
    }

    @Override
    public void addPropertyValue(
            final OneToOneAssociation property,
            final PropertyDto propertyDto,
            final ManagedObject valueAdapter) {

        final String actionIdentifier = CommandUtil.memberIdentifierFor(property);
        final ObjectSpecification onType = property.getOnType();
        final String objectType = onType.getSpecId().asString();
        final String localId = property.getIdentifier().toNameIdentityString();
        propertyDto.setLogicalMemberIdentifier(objectType + "#" + localId);
        propertyDto.setMemberIdentifier(actionIdentifier);

        final ObjectSpecification valueSpec = property.getSpecification();
        final Class<?> valueType = valueSpec.getCorrespondingClass();

        final ValueWithTypeDto newValue = CommonDtoUtils.newValueWithTypeDto(
                valueType, ManagedObject.unwrapSingle(valueAdapter), bookmarkService);
        propertyDto.setNewValue(newValue);
    }


}
