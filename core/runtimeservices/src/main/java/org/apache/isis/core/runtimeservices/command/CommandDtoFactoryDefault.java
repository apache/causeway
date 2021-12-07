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

import java.util.UUID;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.services.command.CommandDtoFactory;
import org.apache.isis.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.cmd.v2.ActionDto;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidsDto;

import lombok.val;

/**
 * The design of this service is similar to
 * {@link org.apache.isis.core.runtimeservices.interaction.InteractionDtoFactoryDefault}.
 *
 * @see org.apache.isis.core.runtimeservices.interaction.InteractionDtoFactoryDefault
 */
@Service
@Named("isis.runtimeservices.CommandDtoFactoryDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class CommandDtoFactoryDefault implements CommandDtoFactory {

    @Inject private SchemaValueMarshaller valueMarshaller;
    @Inject private ClockService clockService;
    @Inject private UserService userService;

    @Override
    public CommandDto asCommandDto(
            final UUID interactionId,
            final Can<InteractionHead> targets,
            final ObjectAction objectAction,
            final Can<ManagedObject> argAdapters) {

        final CommandDto dto = asCommandDto(interactionId, targets);

        final ActionDto actionDto = new ActionDto();
        actionDto.setInteractionType(InteractionType.ACTION_INVOCATION);
        dto.setMember(actionDto);

        addActionArgs(objectAction, actionDto, argAdapters);

        return dto;
    }

    @Override
    public CommandDto asCommandDto(
            final UUID interactionId,
            final Can<InteractionHead> targets,
            final OneToOneAssociation property,
            final ManagedObject valueAdapterOrNull) {

        final CommandDto dto = asCommandDto(interactionId, targets);

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

        actionDto.setLogicalMemberIdentifier(IdentifierUtil.logicalMemberIdentifierFor(objectAction));
        actionDto.setMemberIdentifier(IdentifierUtil.memberIdentifierFor(objectAction));

        val actionParameters = objectAction.getParameters();
        for (int paramNum = 0; paramNum < actionParameters.size(); paramNum++) {
            final ObjectActionParameter actionParameter = actionParameters.getElseFail(paramNum);

            val argAdapter = argAdapters.getElseFail(paramNum);

            // in case of non-scalar params returns the element type
            val elementType = actionParameter.getElementType();

            val paramDto = new ParamDto();
            paramDto.setName(actionParameter.getStaticFriendlyName()
                    .orElseThrow(_Exceptions::unexpectedCodeReach));

            actionParameter.getFeatureIdentifier();

            if(actionParameter.getFeatureType() != FeatureType.ACTION_PARAMETER_COLLECTION) {
                //scalar
                valueMarshaller.recordParamScalar(
                        actionParameter.getFeatureIdentifier(), paramDto, elementType, argAdapter);
            } else {
                //non-scalar
                val values = ManagedObjects.unpack(elementType, argAdapter);
                valueMarshaller.recordParamNonScalar(
                        actionParameter.getFeatureIdentifier(), paramDto, elementType, values);
            }

            CommandDtoUtils.parametersFor(actionDto)
                .getParameter()
                .add(paramDto);
        }
    }

    @Override
    public void addPropertyValue(
            final OneToOneAssociation property,
            final PropertyDto propertyDto,
            final ManagedObject valueAdapter) {

        propertyDto.setLogicalMemberIdentifier(IdentifierUtil.logicalMemberIdentifierFor(property));
        propertyDto.setMemberIdentifier(IdentifierUtil.memberIdentifierFor(property));

        val valueSpec = property.getElementType();

        valueMarshaller.recordPropertyValue(propertyDto, valueSpec, valueAdapter);
    }

    // -- HELPER

    private CommandDto asCommandDto(final UUID interactionId, final Can<InteractionHead> targets) {

        val dto = new CommandDto();
        dto.setMajorVersion("2");
        dto.setMinorVersion("0");

        dto.setInteractionId(interactionId.toString());
        dto.setUser(userService.currentUserNameElseNobody());
        dto.setTimestamp(clockService.getClock().nowAsXmlGregorianCalendar());

        for (val targetHead : targets) {
            final Bookmark bookmark = ManagedObjects.bookmarkElseFail(targetHead.getOwner());
            final OidsDto targetOids = CommandDtoUtils.targetsFor(dto);
            targetOids.getOid().add(bookmark.toOidDto());
        }
        return dto;
    }


}
