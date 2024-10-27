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
package org.apache.causeway.core.runtimeservices.command;

import java.util.UUID;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.services.command.CommandDtoFactory;
import org.apache.causeway.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.common.v2.OidsDto;

/**
 * Default implementation of {@link CommandDtoFactory}.
 *
 * <p>
 * The design of this service is similar to
 * {@link org.apache.causeway.core.runtimeservices.interaction.InteractionDtoFactoryDefault}.
 * </p>
 *
 * @see org.apache.causeway.core.runtimeservices.interaction.InteractionDtoFactoryDefault
 *
 * @since 2.0
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".CommandDtoFactoryDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class CommandDtoFactoryDefault implements CommandDtoFactory {

    @Inject private SchemaValueMarshaller valueMarshaller;
    @Inject private ClockService clockService;
    @Inject private UserService userService;
    @Inject private CausewayConfiguration causewayConfiguration;

    @Override
    public CommandDto asCommandDto(
            final UUID interactionId,
            final InteractionHead targetHead,
            final ObjectAction objectAction,
            final Can<ManagedObject> argAdapters) {

        var commandDto = asCommandDto(interactionId, targetHead);

        var actionDto = new ActionDto();
        actionDto.setInteractionType(InteractionType.ACTION_INVOCATION);
        commandDto.setMember(actionDto);

        addActionArgs(targetHead, objectAction, actionDto, argAdapters);

        return commandDto;
    }

    @Override
    public CommandDto asCommandDto(
            final UUID interactionId,
            final InteractionHead targetHead,
            final OneToOneAssociation property,
            final ManagedObject valueAdapter) {

        final CommandDto dto = asCommandDto(interactionId, targetHead);

        final PropertyDto propertyDto = new PropertyDto();
        propertyDto.setInteractionType(InteractionType.PROPERTY_EDIT);
        dto.setMember(propertyDto);

        addPropertyValue(targetHead, property, propertyDto, valueAdapter);

        return dto;
    }

    @Override
    public void addActionArgs(
            final InteractionHead head,
            final ObjectAction objectAction,
            final ActionDto actionDto,
            final Can<ManagedObject> argAdapters) {

        actionDto.setLogicalMemberIdentifier(IdentifierUtil.logicalMemberIdentifierFor(head, objectAction));

        var actionParameters = objectAction.getParameters();
        for (int paramNum = 0; paramNum < actionParameters.size(); paramNum++) {
            final ObjectActionParameter actionParameter = actionParameters.getElseFail(paramNum);

            var argAdapter = argAdapters.getElseFail(paramNum);

            var paramDto = CommonDtoUtils.paramDto(
                                            isParamIdentifierStrategySetToUseId()
                                                ? actionParameter.getId()
                                                : actionParameter.getCanonicalFriendlyName()
                                             );

            actionParameter.getFeatureIdentifier();

            if(actionParameter.getFeatureType() != FeatureType.ACTION_PARAMETER_PLURAL) {
                //scalar
                valueMarshaller.recordParamScalar(paramDto, actionParameter, argAdapter);
            } else {
                //non-scalar
                var values = ManagedObjects.unpack(argAdapter);
                valueMarshaller.recordParamNonScalar(paramDto, actionParameter, values);
            }

            CommandDtoUtils.parametersFor(actionDto)
                .getParameter()
                .add(paramDto);
        }
    }

    @Override
    public void addPropertyValue(
            final InteractionHead interactionHead,
            final OneToOneAssociation property,
            final PropertyDto propertyDto,
            final ManagedObject valueAdapter) {

        propertyDto.setLogicalMemberIdentifier(IdentifierUtil.logicalMemberIdentifierFor(interactionHead, property));

        valueMarshaller.recordPropertyValue(propertyDto, property, valueAdapter);
    }

    // -- HELPER

    private CommandDto asCommandDto(final UUID interactionId, final InteractionHead targetHead) {

        var dto = new CommandDto();
        dto.setMajorVersion("2");
        dto.setMinorVersion("0");

        dto.setInteractionId(interactionId.toString());
        dto.setUsername(userService.currentUserNameElseNobody());
        dto.setTimestamp(clockService.getClock().nowAsXmlGregorianCalendar());

        // transient/detached entities have no bookmark, fail early
        var bookmark = ManagedObjects.bookmarkElseFail(targetHead.getOwner());
        final OidsDto targetOids = CommandDtoUtils.targetsFor(dto);
        targetOids.getOid().add(bookmark.toOidDto());

        return dto;
    }

    private boolean isParamIdentifierStrategySetToUseId() {
        return causewayConfiguration.getSchema().getCommand().getParamIdentifierStrategy() == CausewayConfiguration.Schema.Command.ParamIdentifierStrategy.BY_ID;
    }

}
