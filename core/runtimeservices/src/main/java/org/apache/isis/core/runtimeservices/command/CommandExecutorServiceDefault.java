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

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandExecutorService;
import org.apache.isis.applib.services.command.CommandOutcomeHandler;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.functional.IndexedFunction;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.schema.cmd.v2.ActionDto;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.MemberDto;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.cmd.v2.ParamsDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidDto;
import org.apache.isis.schema.common.v2.OidsDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isis.runtimeservices.CommandExecutorServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
@RequiredArgsConstructor
public class CommandExecutorServiceDefault implements CommandExecutorService {

    private static final Pattern ID_PARSER =
            Pattern.compile("(?<className>[^#]+)#?(?<localId>[^(]+)(?<args>[(][^)]*[)])?");

    @Inject final BookmarkService bookmarkService;
    @Inject final SudoService sudoService;
    @Inject final ClockService clockService;
    @Inject final TransactionService transactionService;
    @Inject final InteractionLayerTracker iInteractionLayerTracker;
    @Inject final SchemaValueMarshaller valueMarshaller;

    @Inject @Getter final InteractionService interactionService;
    @Inject @Getter final SpecificationLoader specificationLoader;

    @Override
    public Bookmark executeCommand(final Command command) {
        return executeCommand(InteractionContextPolicy.NO_SWITCH, command);
    }

    @Override
    public Bookmark executeCommand(
            final InteractionContextPolicy interactionContextPolicy,
            final Command command) {

        return doExecute(interactionContextPolicy, command.getCommandDto(), command.updater());
    }

    @Override
    public Bookmark executeCommand(
            final CommandDto dto,
            final CommandOutcomeHandler outcomeHandler) {

        return executeCommand(InteractionContextPolicy.NO_SWITCH, dto, outcomeHandler);
    }

    @Override
    public Bookmark executeCommand(
            final InteractionContextPolicy interactionContextPolicy,
            final CommandDto dto,
            final CommandOutcomeHandler outcomeHandler) {

        return doExecute(interactionContextPolicy, dto, outcomeHandler);
    }

    private Bookmark doExecute(
            final InteractionContextPolicy interactionContextPolicy,
            final CommandDto dto,
            final CommandOutcomeHandler commandUpdater) {

        val interaction = iInteractionLayerTracker.currentInteractionElseFail();
        val command = interaction.getCommand();
        if(command.getCommandDto() != dto) {
            command.updater().setCommandDto(dto);
        }

        copyStartedAtFromInteractionExecution(commandUpdater);

        val result = transactionService.callWithinCurrentTransactionElseCreateNew(
            () -> {
                if (interactionContextPolicy == InteractionContextPolicy.NO_SWITCH) {
                    // short-circuit
                    return doExecuteCommand(dto);
                }
                return sudoService.call(
                        context -> interactionContextPolicy.mapper.apply(context, dto),
                        () -> doExecuteCommand(dto));
            });

        result.ifFailure(ex->{
            log.warn("Exception when executing : {}",
                    dto.getMember().getLogicalMemberIdentifier(), ex);
        });

        return handleOutcomeAndSetCompletedAt(commandUpdater, result);
    }

    private void copyStartedAtFromInteractionExecution(
            final CommandOutcomeHandler commandOutcomeHandler) {

        val interaction = iInteractionLayerTracker.currentInteractionElseFail();
        val currentExecution = interaction.getCurrentExecution();

        val startedAt = currentExecution != null
                ? currentExecution.getStartedAt()
                : clockService.getClock().nowAsJavaSqlTimestamp();

        commandOutcomeHandler.setStartedAt(startedAt);
    }

    private Bookmark doExecuteCommand(final CommandDto dto) {

        log.info("Executing: {} {} {}",
                dto.getMember().getLogicalMemberIdentifier(),
                dto.getTimestamp(), dto.getInteractionId());

        final MemberDto memberDto = dto.getMember();
        final String memberId = memberDto.getMemberIdentifier();

        final OidsDto oidsDto = CommandDtoUtils.targetsFor(dto);
        final List<OidDto> targetOidDtos = oidsDto.getOid();

        final InteractionType interactionType = memberDto.getInteractionType();
        if(interactionType == InteractionType.ACTION_INVOCATION) {

            final ActionDto actionDto = (ActionDto) memberDto;

            for (OidDto targetOidDto : targetOidDtos) {

                val targetAdapter = valueMarshaller.recoverReferenceFrom(targetOidDto);
                final ObjectAction objectAction = findObjectAction(targetAdapter, memberId);

                // we pass 'null' for the mixedInAdapter; if this action _is_ a mixin then
                // it will switch the targetAdapter to be the mixedInAdapter transparently
                val argAdapters = argAdaptersFor(actionDto);

                InteractionHead head;
                if(objectAction instanceof ObjectActionMixedIn) {
                    ObjectActionMixedIn actionMixedIn = (ObjectActionMixedIn) objectAction;
                    head = actionMixedIn.interactionHead(targetAdapter);
                } else {
                    head = InteractionHead.regular(targetAdapter);
                }
                val resultAdapter = objectAction.execute(head, argAdapters, InteractionInitiatedBy.FRAMEWORK);

                // flush any Isis PersistenceCommands pending
                // (else might get transient objects for the return value)
                transactionService.flushTransaction();

                //
                // for the result adapter, we could alternatively have used...
                // (priorExecution populated by the push/pop within the interaction object)
                //
                // final Execution priorExecution = backgroundInteraction.getPriorExecution();
                // Object unused = priorExecution.getReturned();
                //

                // REVIEW: this doesn't really make sense if >1 action
                if(resultAdapter != null) {
                    return ManagedObjects.bookmark(resultAdapter)
                            .orElse(null);
                }
            }
        } else {

            final PropertyDto propertyDto = (PropertyDto) memberDto;

            for (OidDto targetOidDto : targetOidDtos) {

                val targetAdapter = valueMarshaller.recoverReferenceFrom(targetOidDto);

                if(ManagedObjects.isNullOrUnspecifiedOrEmpty(targetAdapter)) {
                    throw _Exceptions.unrecoverableFormatted("cannot recreate ManagedObject from bookmark %s",
                            Bookmark.forOidDto(targetOidDto));
                }

                final OneToOneAssociation property = findOneToOneAssociation(targetAdapter, memberId);

                val newValueAdapter = valueMarshaller.recoverValueFrom(propertyDto);

                property.set(targetAdapter, newValueAdapter, InteractionInitiatedBy.FRAMEWORK);

                // there is no return value for property modifications.
            }
        }
        return null;
    }

    private Bookmark handleOutcomeAndSetCompletedAt(
            final CommandOutcomeHandler outcomeHandler,
            final Result<Bookmark> result) {


        //
        // copy over the outcome
        //
        outcomeHandler.setResult(result);

        //
        // also, copy over the completedAt at to the command.
        //
        // NB: it's possible that there is no priorExecution, specifically if
        // there was an exception when performing the action invocation/property
        // edit.  We therefore need to guard that case.
        //
        val interaction = iInteractionLayerTracker.currentInteractionElseFail();

        final Execution<?, ?> priorExecution = interaction.getPriorExecution();
        if(priorExecution != null) {

            if (outcomeHandler.getStartedAt() == null) {
                // TODO: REVIEW - don't think this can happen ...
                //  Interaction/Execution is an in-memory object.
                outcomeHandler.setStartedAt(priorExecution.getStartedAt());
            }
            final Timestamp completedAt =
                    priorExecution.getCompletedAt();
            outcomeHandler.setCompletedAt(completedAt);
        }

        return result.getValue().orElse(null);
    }

    // //////////////////////////////////////

    private static ObjectAction findObjectAction(
            final ManagedObject targetAdapter,
            final String fullyQualifiedActionId) throws RuntimeException {

        final ObjectSpecification specification = targetAdapter.getSpecification();

        // we use the local identifier because the fullyQualified version includes the class name.
        // that is a problem for us if the property is inherited, because it will be the class name of the declaring
        // superclass, rather than the concrete class of the target that we are inspecting here.
        val localActionId = localPartOf(fullyQualifiedActionId);

        final ObjectAction objectAction = findActionElseNull(specification, localActionId);
        if(objectAction == null) {
            throw new RuntimeException(String.format("Unknown action '%s'", localActionId));
        }
        return objectAction;
    }

    private static OneToOneAssociation findOneToOneAssociation(
            final ManagedObject targetAdapter,
            final String fullyQualifiedPropertyId) throws RuntimeException {

        // we use the local identifier because the fullyQualified version includes the class name.
        // that is a problem for us if the property is inherited, because it will be the class name of the declaring
        // superclass, rather than the concrete class of the target that we are inspecting here.
        val localPropertyId = localPartOf(fullyQualifiedPropertyId);

        final ObjectSpecification specification = targetAdapter.getSpecification();

        final OneToOneAssociation property = findOneToOneAssociationElseNull(specification, localPropertyId);
        if(property == null) {
            throw new RuntimeException(String.format("Unknown property '%s'", localPropertyId));
        }
        return property;
    }

    private static String localPartOf(final String memberId) {
        val matcher = ID_PARSER.matcher(memberId);
        return matcher.matches()
                ? matcher.group("localId")
                : "";
    }

    private static ObjectAction findActionElseNull(
            final ObjectSpecification specification,
            final String localActionId) {

        return specification.getAction(localActionId).orElse(null);
    }

    private static OneToOneAssociation findOneToOneAssociationElseNull(
            final ObjectSpecification specification,
            final String localPropertyId) {

        return specification.getAssociation(localPropertyId)
                .filter(ObjectAssociation::isOneToOneAssociation)
                .map(OneToOneAssociation.class::cast)
                .orElse(null);
    }

    private Can<ManagedObject> argAdaptersFor(final ActionDto actionDto) {

        final Identifier actionIdentifier = valueMarshaller.actionIdentifier(actionDto);

        return streamParamDtosFrom(actionDto)
                .map(IndexedFunction.zeroBased((i, paramDto)->
                    valueMarshaller.recoverValuesFrom(actionIdentifier.withParameterIndex(i), paramDto)))
                .collect(Can.toCan());
    }

    private static Stream<ParamDto> streamParamDtosFrom(final ActionDto actionDto) {
        return Optional.ofNullable(actionDto.getParameters())
                .map(ParamsDto::getParameter)
                .map(_NullSafe::stream)
                .orElseGet(Stream::empty);
    }


}
