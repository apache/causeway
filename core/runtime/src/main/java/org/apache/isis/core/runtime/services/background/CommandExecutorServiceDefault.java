/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.services.background;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
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
import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.ObjectAdapterLegacy;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.cmd.v1.CommandDto;
import org.apache.isis.schema.cmd.v1.MemberDto;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.cmd.v1.ParamsDto;
import org.apache.isis.schema.cmd.v1.PropertyDto;
import org.apache.isis.schema.common.v1.CollectionDto;
import org.apache.isis.schema.common.v1.InteractionType;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.OidsDto;
import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;
import org.apache.isis.schema.common.v1.ValueWithTypeDto;
import org.apache.isis.schema.utils.CommandDtoUtils;
import org.apache.isis.schema.utils.CommonDtoUtils;

@DomainService(nature = NatureOfService.DOMAIN)
public class CommandExecutorServiceDefault implements CommandExecutorService {

    private final static Logger LOG = LoggerFactory.getLogger(CommandExecutorServiceDefault.class);

    @Override
    @Programmatic
    public void executeCommand(
            final CommandExecutorService.SudoPolicy sudoPolicy,
            final CommandWithDto commandWithDto) {

        ensureTransactionInProgressWithContext(commandWithDto);

        switch (sudoPolicy) {
        case NO_SWITCH:
            executeCommand(commandWithDto);
            break;
        case SWITCH:
            final String user = commandWithDto.getUser();
            sudoService.sudo(user, new Runnable() {
                @Override
                public void run() {
                    executeCommand(commandWithDto);
                }
            });
            break;
        default:
            throw new IllegalStateException("Probable framework error, unrecognized sudoPolicy: " + sudoPolicy);
        }

        // double check that we've ended up in the same state
        ensureTransactionInProgress();
    }

    private void ensureTransactionInProgressWithContext(final Command command) {

        ensureTransactionInProgress();

        // check the required command is used as the context.
        // this will ensure that any audit entries also inherit from this existing command.
        if(commandContext.getCommand() != command) {
            transactionService.nextTransaction(command);
        }
    }

    private void ensureTransactionInProgress() {
        final Transaction currentTransaction = transactionService.currentTransaction();
        if(currentTransaction == null) {
            throw new IllegalStateException("No current transaction");
        }
        final TransactionState transactionState = currentTransaction.getTransactionState();
        if(!transactionState.canCommit()) {
            throw new IllegalStateException("Current transaction is not in a state to be committed, is: " + transactionState);
        }
    }

    protected void executeCommand(final CommandWithDto commandWithDto) {

        // setup for us by IsisTransactionManager; will have the transactionId of the backgroundCommand
        final Interaction interaction = interactionContext.getInteraction();

        org.apache.isis.applib.annotation.CommandExecuteIn executeIn = commandWithDto.getExecuteIn();

        LOG.info("Executing: {} {} {} {}", executeIn, commandWithDto.getMemberIdentifier(), commandWithDto.getTimestamp(), commandWithDto.getTransactionId());

        RuntimeException exceptionIfAny = null;

        try {
            commandWithDto.setExecutor(Command.Executor.BACKGROUND);

            // responsibility for setting the Command#startedAt is in the ActionInvocationFacet or
            // PropertySetterFacet, but this is run if the domain object was found.  If the domain object is
            // thrown then we would have a command with only completedAt, which is inconsistent.
            // Therefore instead we copy down from the backgroundInteraction (similar to how we populate the
            // completedAt at the end)
            final Interaction.Execution currentExecution = interaction.getCurrentExecution();

            final Timestamp startedAt = currentExecution != null
                    ? currentExecution.getStartedAt()
                            : clockService.nowAsJavaSqlTimestamp();

                    commandWithDto.setStartedAt(startedAt);

                    final CommandDto dto = commandWithDto.asDto();

                    final MemberDto memberDto = dto.getMember();
                    final String memberId = memberDto.getMemberIdentifier();

                    final OidsDto oidsDto = CommandDtoUtils.targetsFor(dto);
                    final List<OidDto> targetOidDtos = oidsDto.getOid();

                    final InteractionType interactionType = memberDto.getInteractionType();
                    if(interactionType == InteractionType.ACTION_INVOCATION) {

                        final ActionDto actionDto = (ActionDto) memberDto;

                        for (OidDto targetOidDto : targetOidDtos) {

                            final ObjectAdapter targetAdapter = adapterFor(targetOidDto);
                            final ObjectAction objectAction = findObjectAction(targetAdapter, memberId);

                            // we pass 'null' for the mixedInAdapter; if this action _is_ a mixin then
                            // it will switch the targetAdapter to be the mixedInAdapter transparently
                            final ObjectAdapter[] argAdapters = argAdaptersFor(actionDto);
                            final ObjectAdapter resultAdapter = objectAction.execute(
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
                                commandWithDto.setResult(resultBookmark);
                            }
                        }
                    } else {

                        final PropertyDto propertyDto = (PropertyDto) memberDto;

                        for (OidDto targetOidDto : targetOidDtos) {

                            final Bookmark bookmark = Bookmark.from(targetOidDto);
                            final Object targetObject = bookmarkService.lookup(bookmark);

                            final ObjectAdapter targetAdapter = adapterFor(targetObject);

                            final OneToOneAssociation property = findOneToOneAssociation(targetAdapter, memberId);

                            final ObjectAdapter newValueAdapter = newValueAdapterFor(propertyDto);

                            property.set(targetAdapter, newValueAdapter, InteractionInitiatedBy.FRAMEWORK);

                            // there is no return value for property modifications.
                        }
                    }

        } catch (RuntimeException ex) {

            LOG.warn("Exception when executing : {} {}", executeIn, commandWithDto.getMemberIdentifier(), ex);

            exceptionIfAny = ex;
        }

        // committing the xactn might also trigger an exception
        try {
            transactionService.nextTransaction(TransactionService.Policy.ALWAYS);
        } catch(RuntimeException ex) {

            LOG.warn("Exception when committing : {} {}", executeIn, commandWithDto.getMemberIdentifier(), ex);

            if(exceptionIfAny == null) {
                exceptionIfAny = ex;
            }

            // this will set up a new transaction
            transactionService.nextTransaction();
        }

        // it's possible that there is no priorExecution, specifically if there was an exception
        // when performing the action invocation/property edit.  We therefore need to guard that case.
        final Interaction.Execution priorExecution = interaction.getPriorExecution();
        if (commandWithDto.getStartedAt() == null) {
            // if attempting to commit the xactn threw an error, we will (I think?) have lost this info, so need to
            // capture
            commandWithDto.setStartedAt(
                    priorExecution != null
                    ? priorExecution.getStartedAt()
                            : clockService.nowAsJavaSqlTimestamp());
        }

        final Timestamp completedAt =
                priorExecution != null
                ? priorExecution.getCompletedAt()
                        : clockService.nowAsJavaSqlTimestamp();  // close enough...
                commandWithDto.setCompletedAt(completedAt);

                if(exceptionIfAny != null) {
                    commandWithDto.setException(Throwables.getStackTraceAsString(exceptionIfAny));
                }
    }

    // //////////////////////////////////////

    private static ObjectAction findObjectAction(
            final ObjectAdapter targetAdapter,
            final String actionId) throws RuntimeException {

        final ObjectSpecification specification = targetAdapter.getSpecification();

        final ObjectAction objectAction = findActionElseNull(specification, actionId);
        if(objectAction == null) {
            throw new RuntimeException(String.format("Unknown action '%s'", actionId));
        }
        return objectAction;
    }

    private static OneToOneAssociation findOneToOneAssociation(
            final ObjectAdapter targetAdapter,
            final String propertyId) throws RuntimeException {

        final ObjectSpecification specification = targetAdapter.getSpecification();

        final OneToOneAssociation property = findOneToOneAssociationElseNull(specification, propertyId);
        if(property == null) {
            throw new RuntimeException(String.format("Unknown property '%s'", propertyId));
        }
        return property;
    }

    private ObjectAdapter newValueAdapterFor(final PropertyDto propertyDto) {
        final ValueWithTypeDto newValue = propertyDto.getNewValue();
        final Object arg = CommonDtoUtils.getValue(newValue);
        return adapterFor(arg);
    }

    private static ObjectAction findActionElseNull(
            final ObjectSpecification specification,
            final String actionId) {
        final List<ObjectAction> objectActions = specification.getObjectActions(Contributed.INCLUDED);
        for (final ObjectAction objectAction : objectActions) {
            if(objectAction.getIdentifier().toClassAndNameIdentityString().equals(actionId)) {
                return objectAction;
            }
        }
        return null;
    }

    private static OneToOneAssociation findOneToOneAssociationElseNull(
            final ObjectSpecification specification,
            final String propertyId) {
        final List<ObjectAssociation> associations = specification.getAssociations(Contributed.INCLUDED);
        for (final ObjectAssociation association : associations) {
            if( association.getIdentifier().toClassAndNameIdentityString().equals(propertyId) &&
                    association instanceof OneToOneAssociation) {
                return (OneToOneAssociation) association;
            }
        }
        return null;
    }

    private ObjectAdapter[] argAdaptersFor(final ActionDto actionDto) {
        final List<ParamDto> params = paramDtosFrom(actionDto);
        final List<ObjectAdapter> args = Lists.newArrayList(
                params.stream()
                .map(paramDto -> {
                    final Object arg = CommonDtoUtils.getValue(paramDto);
                    return adapterFor(arg);
                })
                .collect(Collectors.toList())
                );
        return args.toArray(new ObjectAdapter[]{});
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

    private ObjectAdapter adapterFor(final Object targetObject) {
        return ObjectAdapterLegacy.__CommandExecutorServiceDefault.adapterFor(targetObject);
    }

    // //////////////////////////////////////

    protected IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    protected PersistenceSession getPersistenceSession() {
        return getIsisSessionFactory().getCurrentSession().getPersistenceSession();
    }

    protected IsisTransactionManager getTransactionManager(PersistenceSession persistenceSession) {
        return persistenceSession.getTransactionManager();
    }

    protected SpecificationLoader getSpecificationLoader() {
        return getIsisSessionFactory().getSpecificationLoader();
    }

    // //////////////////////////////////////

    @javax.inject.Inject
    BookmarkService bookmarkService;

    @javax.inject.Inject
    InteractionContext interactionContext;

    @javax.inject.Inject
    SudoService sudoService;

    @javax.inject.Inject
    ClockService clockService;

    @javax.inject.Inject
    TransactionService transactionService;

    @javax.inject.Inject
    CommandContext commandContext;

}
