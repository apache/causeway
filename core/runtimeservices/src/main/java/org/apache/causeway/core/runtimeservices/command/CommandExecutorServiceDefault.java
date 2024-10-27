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

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.commons.UtilStr;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.InteractionType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Default implementation of {@link CommandExecutorService}.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".CommandExecutorServiceDefault")
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
    @Inject final InteractionLayerTracker interactionLayerTracker;
    @Inject final SchemaValueMarshaller valueMarshaller;
    @Inject final MetaModelService metaModelService;
    @Inject Provider<CommandPublisher> commandPublisherProvider;

    @Inject @Getter final SpecificationLoader specificationLoader;

    @Override
    public Try<Bookmark> executeCommand(final Command command) {
        return executeCommand(InteractionContextPolicy.NO_SWITCH, command);
    }

    @Override
    public Try<Bookmark> executeCommand(
            final InteractionContextPolicy interactionContextPolicy,
            final Command command) {
        return doExecute(interactionContextPolicy, command.getCommandDto());
    }

    @Override
    public Try<Bookmark> executeCommand(final CommandDto dto) {
        return executeCommand(InteractionContextPolicy.NO_SWITCH, dto);
    }

    @Override
    public Try<Bookmark> executeCommand(
            final InteractionContextPolicy interactionContextPolicy,
            final CommandDto dto) {

        return doExecute(interactionContextPolicy, dto);
    }

    private Try<Bookmark> doExecute(
            final InteractionContextPolicy interactionContextPolicy,
            final CommandDto dto) {

        var interaction = interactionLayerTracker.currentInteractionElseFail();
        var command = interaction.getCommand();

        // replace the command with that of the DTO to be executed, and also the command's identifier
        //
        // nb: this should be sufficient; there are no other copies of interactionId to be updated.
        // In particular, both InteractionServiceDefault#getInteractionId() and Interaction#getInteractionId() just
        // delegate to the Command held within the Interaction;
        command.updater().setCommandDtoAndIdentifier(dto);

        // notify subscribers that the command is now ready for execution
        command.updater().setPublishingPhase(Command.CommandPublishingPhase.READY);
        commandPublisherProvider.get().ready(command);

        // start executing
        var startedAt = clockService.getClock().nowAsJavaSqlTimestamp();
        command.updater().setStartedAt(startedAt);
        command.updater().setPublishingPhase(Command.CommandPublishingPhase.STARTED);
        commandPublisherProvider.get().start(command);

        Try<Bookmark> result = transactionService.callWithinCurrentTransactionElseCreateNew(
            () -> {
                if (interactionContextPolicy == InteractionContextPolicy.NO_SWITCH) {
                    // short-circuit
                    return doExecuteCommand(dto);
                }
                return sudoService.call(
                        context -> interactionContextPolicy.mapper.apply(context, dto),
                        () -> doExecuteCommand(dto));
            });

        command.updater().setResult(result);

        // we don't need to call the final CommandSubscriber callback, as this is called for us as part of the teardown
        // of the containing Interaction.

        return result;
    }

    private Bookmark doExecuteCommand(final CommandDto dto) {

        if(log.isDebugEnabled()) {
            log.debug("Executing: {} {} {} {}",
                    dto.getMember().getLogicalMemberIdentifier(),
                    dto.getInteractionId(),
                    targetBookmarkStrFor(dto),
                    argStrFor(dto));
        }

        var memberDto = dto.getMember();
        var logicalMemberIdentifier = memberDto.getLogicalMemberIdentifier();

        var oidsDto = CommandDtoUtils.targetsFor(dto);
        var targetOidDtoList = oidsDto.getOid();

        var interactionType = memberDto.getInteractionType();
        if(interactionType == InteractionType.ACTION_INVOCATION) {

            var actionDto = (ActionDto) memberDto;

            // in practice there is only ever one target.
            var targetOidDto = targetOidDtoList.get(0);

            var targetAdapter = valueMarshaller.recoverReferenceFrom(targetOidDto);
            var objectAction = findObjectAction(targetAdapter, logicalMemberIdentifier);

            // we pass 'null' for the mixedInAdapter; if this action _is_ a mixin then
            // it will switch the targetAdapter to be the mixedInAdapter transparently
            var argAdapters = argAdaptersFor(actionDto);

            var interactionHead = objectAction.interactionHead(targetAdapter);

            var resultAdapter = objectAction.execute(interactionHead, argAdapters, InteractionInitiatedBy.FRAMEWORK);

            // flush any PersistenceCommands pending
            // (else might get transient objects for the return value)
            transactionService.flushTransaction();

            //
            // for the result adapter, we could alternatively have used...
            // (priorExecution populated by the push/pop within the interaction object)
            //
            // final Execution priorExecution = backgroundInteraction.getPriorExecution();
            // Object unused = priorExecution.getReturned();
            //

            if(resultAdapter != null) {
                return ManagedObjects.bookmark(resultAdapter)
                        .orElse(null);
            }
        } else {

            var propertyDto = (PropertyDto) memberDto;

            // in practice there is only ever one target.
            var targetOidDto = targetOidDtoList.get(0);

            var targetAdapter = valueMarshaller.recoverReferenceFrom(targetOidDto);

            if(ManagedObjects.isNullOrUnspecifiedOrEmpty(targetAdapter)) {
                throw _Exceptions.unrecoverable("cannot recreate ManagedObject from bookmark %s",
                        Bookmark.forOidDto(targetOidDto));
            }

            var property = findOneToOneAssociation(targetAdapter, logicalMemberIdentifier);
            var newValueAdapter = valueMarshaller.recoverPropertyFrom(propertyDto);
            property.set(targetAdapter, newValueAdapter, InteractionInitiatedBy.FRAMEWORK);

            // there is no return value for property modifications.
        }

        return null;
    }

    private String targetBookmarkStrFor(final CommandDto dto) {
        return dto.getTargets().getOid().stream()
                .map(oidDto -> UtilStr.entityAsStr(Bookmark.forOidDto(oidDto), specificationLoader))
                .collect(Collectors.joining(";"));
    }

    private String argStrFor(final CommandDto dto) {
        var memberDto = dto.getMember();
        if(memberDto instanceof ActionDto) {
            var actionDto = (ActionDto) memberDto;
            return paramNameArgValuesFor(actionDto);
        }
        if(memberDto instanceof PropertyDto) {
            var propertyDto = (PropertyDto) memberDto;
            var proposedValue = valueMarshaller.recoverPropertyFrom(propertyDto);
            return proposedValue.getTitle();
        }
        // shouldn't happen
        return "";
    }

    private static ObjectAction findObjectAction(
            final ManagedObject targetAdapter,
            final String logicalMemberIdentifier) throws RuntimeException {

        var objectSpecification = targetAdapter.getSpecification();

        // we use the local identifier because the fullyQualified version includes the class name.
        // that is a problem for us if the property is inherited, because it will be the class name of the declaring
        // superclass, rather than the concrete class of the target that we are inspecting here.
        var localActionId = localPartOf(logicalMemberIdentifier);

        var objectAction = findActionElseNull(objectSpecification, localActionId);
        if(objectAction == null) {
            throw new RuntimeException(String.format("Unknown action '%s'", localActionId));
        }
        return objectAction;
    }

    private static OneToOneAssociation findOneToOneAssociation(
            final ManagedObject targetAdapter,
            final String logicalMemberIdentifier) throws RuntimeException {

        // we use the local identifier because the fullyQualified version includes the class name.
        // that is a problem for us if the property is inherited, because it will be the class name of the declaring
        // superclass, rather than the concrete class of the target that we are inspecting here.
        var localPropertyId = localPartOf(logicalMemberIdentifier);

        var objectSpecification = targetAdapter.getSpecification();

        var property = findOneToOneAssociationElseNull(objectSpecification, localPropertyId);
        if(property == null) {
            throw new RuntimeException(String.format("Unknown property '%s'", localPropertyId));
        }
        return property;
    }

    private static String localPartOf(final String memberId) {
        var matcher = ID_PARSER.matcher(memberId);
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

    private String paramNameArgValuesFor(final ActionDto actionDto) {
        var actionIdentifier = valueMarshaller.actionIdentifier(actionDto);
        return streamParamDtosFrom(actionDto)
                .map(IndexedFunction.zeroBased((i, paramDto) -> {
                    var argStr = argStr(actionIdentifier, i, paramDto);
                    return paramDto.getName() + "=" + argStr;
                })).collect(Collectors.joining(","));
    }

    private String argStr(final Identifier actionIdentifier, final int i, final ParamDto paramDto) {
        String paramName = paramDto.getName();
        var argValue = valueMarshaller.recoverParameterFrom(actionIdentifier.withParameterIndex(i), paramDto);
        return UtilStr.namedArgStr(paramName, argValue);
    }

//    private static boolean isSensitiveName(String name) {
//        return name.equalsIgnoreCase("password") ||
//               name.equalsIgnoreCase("secret") ||
//               name.equalsIgnoreCase("apikey") ||
//               name.equalsIgnoreCase("token");
//    }

    private Can<ManagedObject> argAdaptersFor(final ActionDto actionDto) {
        var actionIdentifier = valueMarshaller.actionIdentifier(actionDto);
        IndexedFunction<ParamDto, ManagedObject> paramDtoManagedObjectIndexedFunction = (i, paramDto) ->
                valueMarshaller.recoverParameterFrom(actionIdentifier.withParameterIndex(i), paramDto);
        return streamParamDtosFrom(actionDto)
                .map(IndexedFunction.zeroBased(paramDtoManagedObjectIndexedFunction)).collect(Can.toCan());
    }

    private static Stream<ParamDto> streamParamDtosFrom(final ActionDto actionDto) {
        return Optional.ofNullable(actionDto.getParameters())
                .map(ParamsDto::getParameter)
                .map(_NullSafe::stream)
                .orElseGet(Stream::empty);
    }

}
