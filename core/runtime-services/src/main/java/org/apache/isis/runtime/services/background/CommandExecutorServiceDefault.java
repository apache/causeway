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
package org.apache.isis.runtime.services.background;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.CommandExecutorService;
import org.apache.isis.applib.services.command.CommandWithDto;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.cmd.v1.CommandDto;
import org.apache.isis.schema.cmd.v1.MemberDto;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.cmd.v1.ParamsDto;
import org.apache.isis.schema.cmd.v1.PropertyDto;
import org.apache.isis.schema.common.v1.InteractionType;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.OidsDto;
import org.apache.isis.schema.common.v1.ValueWithTypeDto;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.applib.util.schema.CommonDtoUtils;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isisRuntimeServices.CommandExecutorServiceDefault")
@Log4j2
public class CommandExecutorServiceDefault implements CommandExecutorService {

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
        val interaction = interactionContext.getInteraction();
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
                        final ManagedObject[] argAdapters = argAdaptersFor(actionDto);
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
                            Bookmark resultBookmark = CommandUtil.bookmarkFor(resultAdapter);
                            commandWithDto.internal().setResult(resultBookmark);
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

    }

    protected void afterCommit(CommandWithDto commandWithDto, Exception exceptionIfAny) {

        val interaction = interactionContext.getInteraction();

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
            final String actionId) throws RuntimeException {

        final ObjectSpecification specification = targetAdapter.getSpecification();

        final ObjectAction objectAction = findActionElseNull(specification, actionId);
        if(objectAction == null) {
            throw new RuntimeException(String.format("Unknown action '%s'", actionId));
        }
        return objectAction;
    }

    private static OneToOneAssociation findOneToOneAssociation(
            final ManagedObject targetAdapter,
            final String propertyId) throws RuntimeException {

        final ObjectSpecification specification = targetAdapter.getSpecification();

        final OneToOneAssociation property = findOneToOneAssociationElseNull(specification, propertyId);
        if(property == null) {
            throw new RuntimeException(String.format("Unknown property '%s'", propertyId));
        }
        return property;
    }

    private ManagedObject newValueAdapterFor(final PropertyDto propertyDto) {
        final ValueWithTypeDto newValue = propertyDto.getNewValue();
        final Object arg = CommonDtoUtils.getValue(newValue);
        return adapterFor(arg);
    }

    private static ObjectAction findActionElseNull(
            final ObjectSpecification specification,
            final String actionId) {
        final Stream<ObjectAction> objectActions = specification.streamObjectActions(Contributed.INCLUDED);

        return objectActions
                .filter(objectAction->objectAction.getIdentifier().toClassAndNameIdentityString().equals(actionId))
                .findAny()
                .orElse(null);
    }

    private static OneToOneAssociation findOneToOneAssociationElseNull(
            final ObjectSpecification specification,
            final String propertyId) {

        final Stream<ObjectAssociation> associations = specification.streamAssociations(Contributed.INCLUDED);

        return associations
                .filter(association->
                association.getIdentifier().toClassAndNameIdentityString().equals(propertyId) &&
                association instanceof OneToOneAssociation
                        )
                .findAny()
                .map(association->(OneToOneAssociation) association)
                .orElse(null);

    }

    private ManagedObject[] argAdaptersFor(final ActionDto actionDto) {
        
        val paramDtos = paramDtosFrom(actionDto);
        
        return paramDtos
                .stream()
                .map(CommonDtoUtils::getValue)
                .map(this::adapterFor)
                .collect(_Arrays.toArray(ManagedObject.class, paramDtos.size()));
    }

    private static List<ParamDto> paramDtosFrom(final ActionDto actionDto) {
        final ParamsDto parameters = actionDto.getParameters();
        if (parameters != null) {
            final List<ParamDto> parameterList = parameters.getParameter();
            if (parameterList != null) {
                return parameterList;
            }
        }
        return Collections.emptyList();
    }

    private ManagedObject adapterFor(final Object pojo) {
        return ManagedObject.of(getSpecificationLoader()::loadSpecification, pojo);
        
        //legacy of
        //return ObjectAdapterLegacy.__CommandExecutorServiceDefault.adapterFor(targetObject);
    }

    // //////////////////////////////////////

    protected IsisSessionFactory getIsisSessionFactory() {
        return isisSessionFactory;
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession().orElse(null);
    }

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    // -- DEPENDENCIES

    @Inject BookmarkService bookmarkService;
    @Inject InteractionContext interactionContext;
    @Inject SudoService sudoService;
    @Inject ClockService clockService;
    @Inject TransactionService transactionService;
    @Inject CommandContext commandContext;
    @Inject SpecificationLoader specificationLoader;
    @Inject IsisSessionFactory isisSessionFactory;

}
