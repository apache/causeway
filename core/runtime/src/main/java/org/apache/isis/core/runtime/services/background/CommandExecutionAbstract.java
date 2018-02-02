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
import java.util.concurrent.Callable;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command.Executor;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.sessiontemplate.AbstractIsisSessionTemplate;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosure;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosureWithReturn;
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
import org.apache.isis.schema.utils.CommandDtoUtils;
import org.apache.isis.schema.utils.CommonDtoUtils;

/**
 */
public abstract class CommandExecutionAbstract extends AbstractIsisSessionTemplate {

    private final static Logger LOG = LoggerFactory.getLogger(CommandExecutionAbstract.class);

    public enum SudoPolicy {
        /**
         * For example, regular background commands.
         */
        NO_SWITCH,
        /**
         * For example, replayable commands.
         */
        SWITCH,
    }

    private final SudoPolicy sudoPolicy;

    protected CommandExecutionAbstract(final SudoPolicy sudoPolicy) {
        this.sudoPolicy = sudoPolicy;
    }

    // //////////////////////////////////////

    /**
     * Executes the command within a transaction, and with respect to the specified {@link SudoPolicy}
     * specified in the constructor.
     * 
     * <p>
     *     Intended to be called from an override of {@link #doExecute(Object)},
     *     not from {@link #doExecuteWithTransaction(Object)} (because the error handling is different).
     * </p>
     *
     * <p>
     *     Subclasses can add additional policies (eg adjusting the clock) by overriding {@link #executeCommand(IsisTransactionManager, Command)} and delegating to {@link #doExecuteCommand(IsisTransactionManager, Command)} as required.
     * </p>
     *
     * @return - any exception arising.  We don't throw this exception, because an exception might be what is expected (eg if replaying a command that itself failed to execute).
     */
    protected final Exception execute(
            final IsisTransactionManager transactionManager,
            final Command command) {

        try {
            return executeCommandWithinTran(transactionManager, command);
        } catch (final RuntimeException ex) {

            // attempting to commit the xactn itself could cause an issue
            // so we need extra exception handling here, it seems.

            org.apache.isis.applib.annotation.Command.ExecuteIn executeIn = command.getExecuteIn();
            LOG.warn("Exception when committing for: {} {}", executeIn, command.getMemberIdentifier(), ex);

            //
            // the previous transaction will have been aborted as part of the recovery handling within
            // TransactionManager's executeCommandWithinTran
            //
            // we therefore start a new xactn in order to make sure that this background command is marked as a failure
            // so it won't be attempted again.
            //
            transactionManager.executeWithinTransaction(new TransactionalClosure(){
                @Override
                public void execute() {

                    final Interaction backgroundInteraction = interactionContext.getInteraction();
                    final Interaction.Execution currentExecution = backgroundInteraction.getCurrentExecution();
                    command.setStartedAt(
                            currentExecution != null
                                    ? currentExecution.getStartedAt()
                                    : clockService.nowAsJavaSqlTimestamp());

                    command.setCompletedAt(clockService.nowAsJavaSqlTimestamp());
                    command.setException(Throwables.getStackTraceAsString(ex));
                }
            });

            return ex;
        }
    }

    private Exception executeCommandWithinTran(
            final IsisTransactionManager transactionManager,
            final Command command) {

        return transactionManager.executeWithinTransaction(
                command,
                new TransactionalClosureWithReturn<Exception>() {
            @Override
            public Exception execute() {

                return executeCommandPerSudoPolicy(transactionManager, command);
            }
        });
    }

    private Exception executeCommandPerSudoPolicy(
            final IsisTransactionManager transactionManager,
            final Command command) {

        switch (sudoPolicy) {
        case NO_SWITCH:
            return executeCommand(transactionManager, command);
        case SWITCH:
            final String user = command.getUser();
            return sudoService.sudo(user, new Callable<Exception>() {
                @Override
                public Exception call() {
                    return executeCommand(transactionManager, command);
                }
            });
        default:
            throw new IllegalStateException("Unrecognized sudoPolicy: " + sudoPolicy);
        }
    }

    /**
     * Simply delegates to {@link #doExecuteCommand(IsisTransactionManager, Command)}.
     *
     * Overridable, so execution policy can be adjusted if required,
     * eg when replaying, adjust the clock before executing the command.
     *
     * @return - any exception arising
     */
    protected Exception executeCommand(
            final IsisTransactionManager transactionManager,
            final Command command) {
        return doExecuteCommand(transactionManager, command);
    }

    /**
     * Not overrideable, but intended to be called by
     * {@link #executeCommand(IsisTransactionManager, Command)} (which is).
     *
     * @return - any exception arising.  We don't throw this exception, because an exception might be
     *           what is expected (eg if replaying a command that itself failed to execute).
     */
    protected final Exception doExecuteCommand(
            final IsisTransactionManager transactionManager,
            final Command command) {

        // setup for us by IsisTransactionManager; will have the transactionId of the backgroundCommand
        final Interaction backgroundInteraction = interactionContext.getInteraction();

        final String memento = command.getMemento();

        org.apache.isis.applib.annotation.Command.ExecuteIn executeIn = command.getExecuteIn();

        LOG.info("Executing: {} {}", executeIn, command.getMemberIdentifier());

        RuntimeException exceptionIfAny = null;

        try {
            command.setExecutor(Executor.BACKGROUND);

            // responsibility for setting the Command#startedAt is in the ActionInvocationFacet or
            // PropertySetterFacet, but this is run if the domain object was found.  If the domain object is
            // thrown then we would have a command with only completedAt, which is inconsistent.
            // Therefore instead we copy down from the backgroundInteraction (similar to how we populate the
            // completedAt at the end)
            final Interaction.Execution priorExecution = backgroundInteraction.getPriorExecution();

            final Timestamp startedAt = priorExecution != null
                    ? priorExecution.getStartedAt()
                    : clockService.nowAsJavaSqlTimestamp();
            final Timestamp completedAt =
                    priorExecution != null
                            ? priorExecution.getCompletedAt()
                            : clockService.nowAsJavaSqlTimestamp();  // close enough...

            command.setStartedAt(startedAt);
            command.setCompletedAt(completedAt);

            final CommandDto dto = jaxbService.fromXml(CommandDto.class, memento);

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

                    //
                    // for the result adapter, we could alternatively have used...
                    // (priorExecution populated by the push/pop within the interaction object)
                    //
                    // final Interaction.Execution priorExecution = backgroundInteraction.getPriorExecution();
                    // Object unused = priorExecution.getReturned();
                    //

                    // REVIEW: this doesn't really make sense if >1 action
                    // in any case, the capturing of the action interaction should be the
                    // responsibility of auditing/profiling
                    if(resultAdapter != null) {
                        Bookmark resultBookmark = CommandUtil.bookmarkFor(resultAdapter);
                        command.setResult(resultBookmark);
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

            LOG.warn("Exception for: {} {}", executeIn, command.getMemberIdentifier(), ex);

            // hmmm, this doesn't really make sense if >1 action
            //
            // in any case, the capturing of the result of the action invocation should be the
            // responsibility of the interaction...
            command.setException(Throwables.getStackTraceAsString(ex));

            // lower down the stack the IsisTransactionManager will have set the transaction to abort
            // however, we don't want that to occur (because any changes made to the backgroundCommand itself
            // would also be rolled back, and it would keep getting picked up again by a scheduler for
            // processing); instead we clear the abort cause and ensure we can continue.
            transactionManager.getCurrentTransaction().clearAbortCauseAndContinue();

            // checked at the end
            exceptionIfAny = ex;

        }

        // it's possible that there is no priorExecution, specifically if there was an exception
        // invoking the action.  We therefore need to guard that case.
        final Interaction.Execution priorExecution = backgroundInteraction.getPriorExecution();
        final Timestamp completedAt =
                priorExecution != null
                        ? priorExecution.getCompletedAt()
                        : clockService.nowAsJavaSqlTimestamp();  // close enough...
        command.setCompletedAt(completedAt);

        return exceptionIfAny;
    }

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

    private ObjectAdapter[] argAdaptersFor(final ActionInvocationMemento aim)  {
        final int numArgs = aim.getNumArgs();
        final List<ObjectAdapter> argumentAdapters = Lists.newArrayList();
        for(int i=0; i<numArgs; i++) {
            final ObjectAdapter argAdapter = argAdapterFor(aim, i);
            argumentAdapters.add(argAdapter);
        }
        return argumentAdapters.toArray(new ObjectAdapter[]{});
    }

    private ObjectAdapter argAdapterFor(final ActionInvocationMemento aim, int num) {
        final Class<?> argType;
        try {
            argType = aim.getArgType(num);
            final Object arg = aim.getArg(num, argType);
            if(arg == null) {
                return null;
            }
            return adapterFor(arg);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectAdapter[] argAdaptersFor(final ActionDto actionDto) {
        final List<ParamDto> params = paramDtosFrom(actionDto);
        final List<ObjectAdapter> args = Lists.newArrayList(
                Iterables.transform(params, new Function<ParamDto, ObjectAdapter>() {
                    @Override
                    public ObjectAdapter apply(final ParamDto paramDto) {
                        final Object arg = CommonDtoUtils.getValue(paramDto);
                        return adapterFor(arg);
                    }
                })
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

    // //////////////////////////////////////

    @javax.inject.Inject
    BookmarkService2 bookmarkService;

    @javax.inject.Inject
    JaxbService jaxbService;

    @javax.inject.Inject
    InteractionContext interactionContext;

    @javax.inject.Inject
    SudoService sudoService;

    @javax.inject.Inject
    ClockService clockService;

}
