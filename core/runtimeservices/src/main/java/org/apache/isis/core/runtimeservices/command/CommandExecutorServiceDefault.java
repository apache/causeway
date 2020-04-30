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
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandExecutorService;
import org.apache.isis.applib.services.command.CommandWithDto;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.iactn.IsisInteraction;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.core.runtime.iactn.IsisInteractionTracker;
import org.apache.isis.schema.cmd.v2.ActionDto;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.MemberDto;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.cmd.v2.ParamsDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidDto;
import org.apache.isis.schema.common.v2.OidsDto;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isisRuntimeServices.CommandExecutorServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class CommandExecutorServiceDefault implements CommandExecutorService {

    private static final Pattern ID_PARSER =
            Pattern.compile("(?<className>[^#]+)#?(?<localId>[^(]+)(?<args>[(][^)]*[)])?");

    @Inject private BookmarkService bookmarkService;
    @Inject private SudoService sudoService;
    @Inject private ClockService clockService;
    @Inject private TransactionService transactionService;
    @Inject private IsisInteractionTracker isisInteractionTracker;
    @Inject private javax.inject.Provider<InteractionContext> interactionContextProvider;
    
    @Inject @Getter private IsisInteractionFactory isisInteractionFactory;
    @Inject @Getter private SpecificationLoader specificationLoader;
    
    @Override
    public void executeCommand(
            final CommandExecutorService.SudoPolicy sudoPolicy,
            final CommandWithDto commandWithDto) {

        final Runnable commandRunnable = ()->executeCommand(commandWithDto);
        final Runnable topLevelRunnable;

        switch (sudoPolicy) {
        case NO_SWITCH:
            topLevelRunnable = commandRunnable;
            break;
        case SWITCH:
            val user = commandWithDto.getUser();
            topLevelRunnable = ()->sudoService.sudo(user, commandRunnable);
            break;
        default:
            throw new IllegalStateException("Probable framework error, unrecognized sudoPolicy: " + sudoPolicy);
        }

        try {

            transactionService.executeWithinTransaction(topLevelRunnable);

            afterCommit(commandWithDto, /*exception*/null);

        } catch (Exception e) {

            val executeIn = commandWithDto.getExecuteIn();

            log.warn("Exception when executing : {} {}", executeIn, commandWithDto.getMemberIdentifier(), e);
            afterCommit(commandWithDto, e);
        }

    }

    protected void executeCommand(final CommandWithDto commandWithDto) {

        // setup for us by IsisTransactionManager; will have the transactionId of the backgroundCommand
        val interaction = interactionContextProvider.get().getInteraction();
        val executeIn = commandWithDto.getExecuteIn();

        log.info("Executing: {} {} {} {}", executeIn, commandWithDto.getMemberIdentifier(), commandWithDto.getTimestamp(), commandWithDto.getUniqueId());

        commandWithDto.internal().setExecutor(Command.Executor.BACKGROUND);

        // responsibility for setting the Command#startedAt is in the ActionInvocationFacet or
        // PropertySetterFacet, but this is run if the domain object was found.  If the domain object is
        // thrown then we would have a command with only completedAt, which is inconsistent.
        // Therefore instead we copy down from the backgroundInteraction (similar to how we populate the
        // completedAt at the end)
        val currentExecution = interaction.getCurrentExecution();

        val startedAt = currentExecution != null
                ? currentExecution.getStartedAt()
                        : clockService.nowAsJavaSqlTimestamp();

        commandWithDto.internal().setStartedAt(startedAt);

        final CommandDto dto = commandWithDto.asDto();

        Bookmark resultBookmark = executeCommand(dto);
        commandWithDto.internal().setResult(resultBookmark);
    }

    @Override
    public Bookmark executeCommand(CommandDto dto) {

        final MemberDto memberDto = dto.getMember();
        final String memberId = memberDto.getMemberIdentifier();

        final OidsDto oidsDto = CommandDtoUtils.targetsFor(dto);
        final List<OidDto> targetOidDtos = oidsDto.getOid();

        final InteractionType interactionType = memberDto.getInteractionType();
        if(interactionType == InteractionType.ACTION_INVOCATION) {

            final ActionDto actionDto = (ActionDto) memberDto;

            for (OidDto targetOidDto : targetOidDtos) {

                val targetAdapter = adapterFor(targetOidDto);
                final ObjectAction objectAction = findObjectAction(targetAdapter, memberId);

                // we pass 'null' for the mixedInAdapter; if this action _is_ a mixin then
                // it will switch the targetAdapter to be the mixedInAdapter transparently
                val argAdapters = argAdaptersFor(actionDto);

                val resultAdapter = objectAction.execute(
                        targetAdapter, null, argAdapters, InteractionInitiatedBy.FRAMEWORK);

                // flush any Isis PersistenceCommands pending
                // (else might get transient objects for the return value)
                transactionService.flushTransaction();

                //
                // for the result adapter, we could alternatively have used...
                // (priorExecution populated by the push/pop within the interaction object)
                //
                // final Interaction.Execution priorExecution = backgroundInteraction.getPriorExecution();
                // Object unused = priorExecution.getReturned();
                //

                // REVIEW: this doesn't really make sense if >1 action
                if(resultAdapter != null) {
                    return CommandUtil.bookmarkFor(resultAdapter);
                }
            }
        } else {

            final PropertyDto propertyDto = (PropertyDto) memberDto;

            for (OidDto targetOidDto : targetOidDtos) {

                final Bookmark bookmark = Bookmark.from(targetOidDto);
                final Object targetObject = bookmarkService.lookup(bookmark);

                val targetAdapter = adapterFor(targetObject);

                final OneToOneAssociation property = findOneToOneAssociation(targetAdapter, memberId);

                val newValueAdapter = newValueAdapterFor(propertyDto);

                property.set(targetAdapter, newValueAdapter, InteractionInitiatedBy.FRAMEWORK);

                // there is no return value for property modifications.
            }
        }
        return null;
    }

    protected void afterCommit(CommandWithDto commandWithDto, Exception exceptionIfAny) {

        val interaction = interactionContextProvider.get().getInteraction();

        // it's possible that there is no priorExecution, specifically if there was an exception
        // when performing the action invocation/property edit.  We therefore need to guard that case.
        final Interaction.Execution<?, ?> priorExecution = interaction.getPriorExecution();
        if (commandWithDto.getStartedAt() == null) {
            // if attempting to commit the xactn threw an error, we will (I think?) have lost this info, so need to
            // capture
            commandWithDto.internal().setStartedAt(
                    priorExecution != null
                    ? priorExecution.getStartedAt()
                            : clockService.nowAsJavaSqlTimestamp());
        }

        final Timestamp completedAt =
                priorExecution != null
                ? priorExecution.getCompletedAt()
                        : clockService.nowAsJavaSqlTimestamp();  // close enough...
                commandWithDto.internal().setCompletedAt(completedAt);

                if(exceptionIfAny != null) {
                    commandWithDto.internal().setException(_Exceptions.
                            streamStacktraceLines(exceptionIfAny, 500)
                            .collect(Collectors.joining("\n")));
                }

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

    private static String localPartOf(String memberId) {
        val matcher = ID_PARSER.matcher(memberId);
        return matcher.matches()
                ? matcher.group("localId")
                : "";
    }

    private ManagedObject newValueAdapterFor(final PropertyDto propertyDto) {
        final ValueWithTypeDto newValue = propertyDto.getNewValue();
        final Object arg = CommonDtoUtils.getValue(newValue);
        return adapterFor(arg);
    }

    private static ObjectAction findActionElseNull(
            final ObjectSpecification specification,
            final String localActionId) {
        final Stream<ObjectAction> objectActions = specification.streamObjectActions(Contributed.INCLUDED);

        return objectActions
                .filter(objectAction->
                        Objects.equals(objectAction.getId(), localActionId))
                .findAny()
                .orElse(null);
    }

    private static OneToOneAssociation findOneToOneAssociationElseNull(
            final ObjectSpecification specification,
            final String localPropertyId) {

        final Stream<ObjectAssociation> associations = specification.streamAssociations(Contributed.INCLUDED);

        return associations
                .filter(association->
                            Objects.equals(association.getId(), localPropertyId) &&
                            association instanceof OneToOneAssociation
                        )
                .findAny()
                .map(association->(OneToOneAssociation) association)
                .orElse(null);

    }

    private Can<ManagedObject> argAdaptersFor(final ActionDto actionDto) {
        return streamParamDtosFrom(actionDto)
                .map(CommonDtoUtils::getValue)
                .map(this::adapterFor)
                .collect(Can.toCan());
    }

    private static Stream<ParamDto> streamParamDtosFrom(final ActionDto actionDto) {
        return Optional.ofNullable(actionDto.getParameters())
                .map(ParamsDto::getParameter)
                .map(_NullSafe::stream)
                .orElseGet(Stream::empty);
    }

    private ManagedObject adapterFor(final Object pojo) {
        if(pojo==null) {
            return ManagedObject.unspecified();
        }
        if(pojo instanceof OidDto) {
            return adapterFor((OidDto)pojo);
        }
        if(pojo instanceof RootOid) {
            return adapterFor((RootOid) pojo);
        }
        // value type
        return ManagedObject.of(getSpecificationLoader()::loadSpecification, pojo);
    }

    private ManagedObject adapterFor(final OidDto oid) {
        val oidStr = Oid.marshaller().joinAsOid(oid.getType(), oid.getId());
        val rootOid = Oid.unmarshaller().unmarshal(oidStr, RootOid.class);
        return adapterFor(rootOid);
    }

    private ManagedObject adapterFor(final RootOid oid) {
        val objectSpec = specificationLoader.loadSpecification(oid.getObjectSpecId());
        val loadRequest = ObjectLoader.Request.of(objectSpec, oid.getIdentifier());

        Optional<IsisInteraction> isisInteraction = isisInteractionTracker.currentInteraction();
        return isisInteraction
                .map(x -> x.getObjectManager().loadObject(loadRequest))
                .orElse(null);
    }


}
