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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.applib.services.wrapper.DisabledException;
import org.apache.causeway.applib.services.wrapper.HiddenException;
import org.apache.causeway.applib.services.wrapper.InvalidException;
import org.apache.causeway.applib.services.wrapper.events.ActionInvocationEvent;
import org.apache.causeway.applib.services.wrapper.events.ActionUsabilityEvent;
import org.apache.causeway.applib.services.wrapper.events.ActionVisibilityEvent;
import org.apache.causeway.applib.services.wrapper.events.InteractionEvent;
import org.apache.causeway.applib.services.wrapper.events.PropertyModifyEvent;
import org.apache.causeway.applib.services.wrapper.events.PropertyUsabilityEvent;
import org.apache.causeway.applib.services.wrapper.events.PropertyVisibilityEvent;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Core.RuntimeServices.CommandExecutorService.InteractionAdvisorPolicy;
import org.apache.causeway.core.metamodel.commons.UtilStr;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
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

import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link CommandExecutorService}.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".CommandExecutorServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Slf4j
public record CommandExecutorServiceDefault(
        BookmarkService bookmarkService,
        SudoService sudoService,
        ClockService clockService,
        TransactionService transactionService,
        InteractionProvider interactionProvider,
        SchemaValueMarshaller valueMarshaller,
        MetaModelService metaModelService,
        Provider<CommandPublisher> commandPublisherProvider,
        SpecificationLoader specificationLoader,
        CausewayConfiguration causewayConfiguration
) implements CommandExecutorService {

    @Inject
    public CommandExecutorServiceDefault {
    }

    private static final Pattern ID_PARSER =
            Pattern.compile("(?<className>[^#]+)#?(?<localId>[^(]+)(?<args>[(][^)]*[)])?");

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

        var interaction = interactionProvider.currentInteractionElseFail();
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
                    if (interactionContextPolicy == InteractionContextPolicy.NO_SWITCH)
                        // short-circuit
                        return doExecuteCommand(dto);
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

        if (log.isDebugEnabled()) {
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
        if (interactionType == InteractionType.ACTION_INVOCATION) {

            var actionDto = (ActionDto) memberDto;

            // in practice there is only ever one target.
            var targetOidDto = targetOidDtoList.get(0);

            var targetAdapter = valueMarshaller.recoverReferenceFrom(targetOidDto);
            var objectAction = findObjectAction(targetAdapter, logicalMemberIdentifier);

            // we pass 'null' for the mixedInAdapter; if this action _is_ a mixin then
            // it will switch the targetAdapter to be the mixedInAdapter transparently
            var argAdapters = argAdaptersFor(actionDto, objectAction);

            var interactionHead = objectAction.interactionHead(targetAdapter);

            applyActionAdvisorPolicy(
                    causewayConfiguration.core().runtimeServices().commandExecutorService().interactionAdvisorPolicy(),
                    objectAction,
                    targetAdapter,
                    interactionHead,
                    argAdapters);

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

            if (resultAdapter != null)
                return ManagedObjects.bookmark(resultAdapter)
                        .orElse(null);
        } else {

            var propertyDto = (PropertyDto) memberDto;

            // in practice there is only ever one target.
            var targetOidDto = targetOidDtoList.get(0);

            var targetAdapter = valueMarshaller.recoverReferenceFrom(targetOidDto);

            if (ManagedObjects.isNullOrUnspecifiedOrEmpty(targetAdapter))
                throw _Exceptions.unrecoverable("cannot recreate ManagedObject from bookmark %s",
                        Bookmark.forOidDto(targetOidDto));

            var property = findOneToOneAssociation(targetAdapter, logicalMemberIdentifier);
            var newValueAdapter = valueMarshaller.recoverPropertyFrom(propertyDto);

            applyPropertyAdvisorPolicy(
                    causewayConfiguration.core().runtimeServices().commandExecutorService().interactionAdvisorPolicy(),
                    property,
                    targetAdapter,
                    newValueAdapter);

            property.set(targetAdapter, newValueAdapter, InteractionInitiatedBy.FRAMEWORK);

            // there is no return value for property modifications.
        }

        return null;
    }

    static void applyActionAdvisorPolicy(
            final InteractionAdvisorPolicy policy,
            final ObjectAction action,
            final ManagedObject target,
            final InteractionHead interactionHead,
            final Can<ManagedObject> arguments) {

        if (policy == InteractionAdvisorPolicy.NO_CHECK) {
            return;
        }

        var visibility = action.isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
        if (policy == InteractionAdvisorPolicy.CHECK && visibility.isVetoed()) {
            throw new HiddenException(advised(
                    new ActionVisibilityEvent(target.getPojo(), action.getFeatureIdentifier()), visibility));
        }

        var usability = action.isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
        if (policy == InteractionAdvisorPolicy.CHECK && usability.isVetoed()) {
            throw new DisabledException(advised(
                    new ActionUsabilityEvent(target.getPojo(), action.getFeatureIdentifier()), usability));
        }

        var validity = action.isArgumentSetValid(interactionHead, arguments, InteractionInitiatedBy.FRAMEWORK);
        if (policy == InteractionAdvisorPolicy.CHECK && validity.isVetoed()) {
            var argumentPojos = arguments.stream()
                    .map(ManagedObject::getPojo)
                    .toArray();
            throw new InvalidException(advised(
                    new ActionInvocationEvent(target.getPojo(), action.getFeatureIdentifier(), argumentPojos), validity));
        }
    }

    static void applyPropertyAdvisorPolicy(
            final InteractionAdvisorPolicy policy,
            final OneToOneAssociation property,
            final ManagedObject target,
            final ManagedObject proposedValue) {

        if (policy == InteractionAdvisorPolicy.NO_CHECK) {
            return;
        }

        var visibility = property.isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
        if (policy == InteractionAdvisorPolicy.CHECK && visibility.isVetoed()) {
            throw new HiddenException(advised(
                    new PropertyVisibilityEvent(target.getPojo(), property.getFeatureIdentifier()), visibility));
        }

        var usability = property.isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
        if (policy == InteractionAdvisorPolicy.CHECK && usability.isVetoed()) {
            throw new DisabledException(advised(
                    new PropertyUsabilityEvent(target.getPojo(), property.getFeatureIdentifier()), usability));
        }

        var validity = property.isAssociationValid(target, proposedValue, InteractionInitiatedBy.FRAMEWORK);
        if (policy == InteractionAdvisorPolicy.CHECK && validity.isVetoed()) {
            throw new InvalidException(advised(
                    new PropertyModifyEvent(
                            target.getPojo(),
                            property.getFeatureIdentifier(),
                            proposedValue.getPojo()),
                    validity));
        }
    }

    private static <T extends InteractionEvent> T advised(final T event, final Consent consent) {
        consent.getReasonAsString()
                .ifPresent(reason -> event.advised(reason, CommandExecutorServiceDefault.class));
        return event;
    }

    private String targetBookmarkStrFor(final CommandDto dto) {
        return dto.getTargets().getOid().stream()
                .map(oidDto -> UtilStr.entityAsStr(Bookmark.forOidDto(oidDto), specificationLoader))
                .collect(Collectors.joining(";"));
    }

    private String argStrFor(final CommandDto dto) {
        var memberDto = dto.getMember();
        if (memberDto instanceof ActionDto actionDto)
            return paramNameArgValuesFor(actionDto);
        if (memberDto instanceof PropertyDto propertyDto) {
            var proposedValue = valueMarshaller.recoverPropertyFrom(propertyDto);
            return proposedValue.getTitle();
        }
        // shouldn't happen
        return "";
    }

    private static ObjectAction findObjectAction(
            final ManagedObject targetAdapter,
            final String logicalMemberIdentifier) throws RuntimeException {

        var objectSpecification = targetAdapter.objSpec();

        // we use the local identifier because the fullyQualified version includes the class name.
        // that is a problem for us if the property is inherited, because it will be the class name of the declaring
        // superclass, rather than the concrete class of the target that we are inspecting here.
        var localActionId = localPartOf(logicalMemberIdentifier);

        var objectAction = findActionElseNull(objectSpecification, localActionId);
        if (objectAction == null)
            throw new RuntimeException(String.format("Unknown action '%s'", localActionId));
        return objectAction;
    }

    private static OneToOneAssociation findOneToOneAssociation(
            final ManagedObject targetAdapter,
            final String logicalMemberIdentifier) throws RuntimeException {

        // we use the local identifier because the fullyQualified version includes the class name.
        // that is a problem for us if the property is inherited, because it will be the class name of the declaring
        // superclass, rather than the concrete class of the target that we are inspecting here.
        var localPropertyId = localPartOf(logicalMemberIdentifier);

        var objectSpecification = targetAdapter.objSpec();

        var property = findOneToOneAssociationElseNull(objectSpecification, localPropertyId);
        if (property == null)
            throw new RuntimeException(String.format("Unknown property '%s'", localPropertyId));
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

    /**
     * Reserved id prefix for a synthetic parented-collection selector ("navigate to one of") action.
     * <p>
     * Must match {@code SyntheticNavigationActionFactory.COLLECTION_ACTION_ID_PREFIX} in core-metamodel (that
     * package-private constant is not on this module's classpath). The prefix is serialized into the command
     * DTO, so a recorded collection-navigation command is recognised here on replay.
     */
    private static final String PARENTED_COLLECTION_NAVIGATION_ACTION_ID_PREFIX = "__causeway_navigate_to_one_of_";

    private Can<ManagedObject> argAdaptersFor(final ActionDto actionDto, final ObjectAction objectAction) {
        if (objectAction.getId().startsWith(PARENTED_COLLECTION_NAVIGATION_ACTION_ID_PREFIX)) {
            return argAdaptersForParentedCollectionNavigation(actionDto, objectAction, valueMarshaller);
        }
        return argAdaptersFor(actionDto);
    }

    private Can<ManagedObject> argAdaptersFor(final ActionDto actionDto) {
        var actionIdentifier = valueMarshaller.actionIdentifier(actionDto);
        IndexedFunction<ParamDto, ManagedObject> paramDtoManagedObjectIndexedFunction = (i, paramDto) ->
                valueMarshaller.recoverParameterFrom(actionIdentifier.withParameterIndex(i), paramDto);
        return streamParamDtosFrom(actionDto)
                .map(IndexedFunction.zeroBased(paramDtoManagedObjectIndexedFunction)).collect(Can.toCan());
    }

    /**
     * Reconstructs replay arguments for a synthetic parented-collection selector action.
     * <p>
     * Such an action's parameters are the collection's optional column filters, which can be added, removed, or
     * reordered between the time a command is recorded and the time it is replayed. Rather than bind positionally
     * (which would silently mis-align on any such change), each current action parameter is matched to the
     * recorded DTO parameter with the same stable id (falling back to friendly name); any current parameter with
     * no corresponding recorded parameter is padded with an empty/unselected value.
     * <p>
     * Package-private and static to allow focused testing.
     */
    static Can<ManagedObject> argAdaptersForParentedCollectionNavigation(
            final ActionDto actionDto,
            final ObjectAction objectAction,
            final SchemaValueMarshaller valueMarshaller) {

        var actionIdentifier = valueMarshaller.actionIdentifier(actionDto);

        var paramDtoByName = new HashMap<String, ParamDto>();
        streamParamDtosFrom(actionDto)
                .filter(paramDto -> paramDto.getName() != null)
                .forEach(paramDto -> paramDtoByName.putIfAbsent(paramDto.getName(), paramDto));

        var actionParameters = objectAction.getParameters();
        var argAdapters = new ArrayList<ManagedObject>(actionParameters.size());
        for (int paramNum = 0; paramNum < actionParameters.size(); paramNum++) {
            var actionParameter = actionParameters.getElseFail(paramNum);
            var paramDto = Optional.ofNullable(paramDtoByName.get(actionParameter.getId()))
                    .orElseGet(() -> paramDtoByName.get(actionParameter.getCanonicalFriendlyName()));
            argAdapters.add(paramDto != null
                    ? valueMarshaller.recoverParameterFrom(actionIdentifier.withParameterIndex(paramNum), paramDto)
                    : ManagedObject.empty(actionParameter.getElementType()));
        }
        return argAdapters.stream().collect(Can.toCan());
    }

    private static Stream<ParamDto> streamParamDtosFrom(final ActionDto actionDto) {
        return Optional.ofNullable(actionDto.getParameters())
                .map(ParamsDto::getParameter)
                .map(_NullSafe::stream)
                .orElseGet(Stream::empty);
    }

}
