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
package org.apache.isis.core.runtimeservices.command;

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
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.services.command.CommandDtoFactory;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.cmd.v2.ActionDto;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.cmd.v2.ParamsDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidsDto;

import lombok.val;

@Service
@Named("isisRuntimeServices.CommandDtoServiceInternalDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class CommandDtoServiceInternalDefault implements CommandDtoFactory {

    @Inject BookmarkService bookmarkService;
    @Inject ClockService clockService;
    @Inject UserService userService;

    @Override
    public CommandDto asCommandDto(
            final UUID uniqueId,
            final Can<ManagedObject> targetAdapters,
            final ObjectAction objectAction,
            final Can<ManagedObject> argAdapters) {

        final CommandDto dto = asCommandDto(uniqueId, targetAdapters);

        final ActionDto actionDto = new ActionDto();
        actionDto.setInteractionType(InteractionType.ACTION_INVOCATION);
        dto.setMember(actionDto);

        addActionArgs(objectAction, actionDto, argAdapters);

        return dto;
    }

    @Override
    public CommandDto asCommandDto(
            final UUID uniqueId,
            final Can<ManagedObject> targetAdapters,
            final OneToOneAssociation property,
            final ManagedObject valueAdapterOrNull) {

        final CommandDto dto = asCommandDto(uniqueId, targetAdapters);

        final PropertyDto propertyDto = new PropertyDto();
        propertyDto.setInteractionType(InteractionType.PROPERTY_EDIT);
        dto.setMember(propertyDto);

        addPropertyValue(property, propertyDto, valueAdapterOrNull);

        return dto;
    }

    @Override
    public void addActionArgs(
            final ObjectAction objectAction,
            final ActionDto actionDto,
            final Can<ManagedObject> argAdapters) {

        actionDto.setLogicalMemberIdentifier(CommandUtil.logicalMemberIdentifierFor(objectAction));
        actionDto.setMemberIdentifier(CommandUtil.memberIdentifierFor(objectAction));

        val actionParameters = objectAction.getParameters();
        for (int paramNum = 0; paramNum < actionParameters.size(); paramNum++) {
            final ObjectActionParameter actionParameter = actionParameters.getElseFail(paramNum);
            final String parameterName = actionParameter.getName();
            final Class<?> paramType = actionParameter.getSpecification().getCorrespondingClass();
            final ManagedObject argAdapter = argAdapters.getElseFail(paramNum);
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

        propertyDto.setLogicalMemberIdentifier(CommandUtil.logicalMemberIdentifierFor(property));
        propertyDto.setMemberIdentifier(CommandUtil.memberIdentifierFor(property));

        val valueSpec = property.getSpecification();
        val valueType = valueSpec.getCorrespondingClass();

        val newValue = CommonDtoUtils.newValueWithTypeDto(
                valueType, UnwrapUtil.single(valueAdapter), bookmarkService);
        propertyDto.setNewValue(newValue);
    }

    // -- HELPER
    
    private CommandDto asCommandDto(final UUID uniqueId, final Can<ManagedObject> targetAdapters) {

        val dto = new CommandDto();
        dto.setMajorVersion("2");
        dto.setMinorVersion("0");

        dto.setTransactionId(uniqueId.toString());
        dto.setUser(userService.getUser().getName());
        dto.setTimestamp(clockService.getClock().xmlGregorianCalendar());

        for (val targetAdapter : targetAdapters) {
            final Bookmark bookmark = ManagedObjects.bookmarkElseFail(targetAdapter);
            final OidsDto targets = CommandDtoUtils.targetsFor(dto);
            targets.getOid().add(bookmark.toOidDto());
        }
        return dto;
    }
    

}
