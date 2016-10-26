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

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command.Executor;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.services.memento.MementoServiceDefault;
import org.apache.isis.core.runtime.sessiontemplate.AbstractIsisSessionTemplate;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosure;
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
 * Intended to be used as a base class for executing queued up {@link Command background action}s.
 * 
 * <p>
 * This implementation uses the {@link #findBackgroundCommandsToExecute() hook method} so that it is
 * independent of the location where the actions have actually been persisted to.
 */
public abstract class BackgroundCommandExecution extends AbstractIsisSessionTemplate {

    private final MementoServiceDefault mementoService;

    public BackgroundCommandExecution() {
        // same as configured by BackgroundServiceDefault
        mementoService = new MementoServiceDefault().withNoEncoding();
    }
    
    // //////////////////////////////////////

    
    protected void doExecute(Object context) {

        final PersistenceSession persistenceSession = getPersistenceSession();
        final IsisTransactionManager transactionManager = getTransactionManager(persistenceSession);
        final List<Command> backgroundCommands = Lists.newArrayList();
        transactionManager.executeWithinTransaction(new TransactionalClosure() {
            @Override
            public void execute() {
                backgroundCommands.addAll(findBackgroundCommandsToExecute());
            }
        });

        for (final Command backgroundCommand : backgroundCommands) {
            execute(transactionManager, backgroundCommand);
        }
    }

    /**
     * Mandatory hook method
     */
    protected abstract List<? extends Command> findBackgroundCommandsToExecute();

    // //////////////////////////////////////

    
    private void execute(
            final IsisTransactionManager transactionManager,
            final Command backgroundCommand) {

        transactionManager.executeWithinTransaction(
                backgroundCommand,
                new TransactionalClosure() {
            @Override
            public void execute() {

                // setup for us by IsisTransactionManager; will have the transactionId of the backgroundCommand
                final Interaction backgroundInteraction = interactionContext.getInteraction();

                final String memento = backgroundCommand.getMemento();

                try {
                    backgroundCommand.setExecutor(Executor.BACKGROUND);

                    final boolean legacy = memento.startsWith("<memento");
                    if(legacy) {

                        final ActionInvocationMemento aim = new ActionInvocationMemento(mementoService, memento);

                        final String actionId = aim.getActionId();

                        final Bookmark targetBookmark = aim.getTarget();
                        final Object targetObject = bookmarkService.lookup(
                                                        targetBookmark, BookmarkService2.FieldResetPolicy.RESET);

                        final ObjectAdapter targetAdapter = adapterFor(targetObject);
                        final ObjectSpecification specification = targetAdapter.getSpecification();

                        final ObjectAction objectAction = findActionElseNull(specification, actionId);
                        if(objectAction == null) {
                            throw new RuntimeException(String.format("Unknown action '%s'", actionId));
                        }

                        // TODO: background commands won't work for mixin actions...
                        // ... we obtain the target from the bookmark service (above), which will
                        // simply fail for a mixin.  Instead we would need to serialize out the mixedInAdapter
                        // and also capture the mixinType within the aim memento.
                        final ObjectAdapter mixedInAdapter = null;

                        final ObjectAdapter[] argAdapters = argAdaptersFor(aim);
                        final ObjectAdapter resultAdapter = objectAction.execute(
                                targetAdapter, mixedInAdapter, argAdapters, InteractionInitiatedBy.FRAMEWORK);

                        if(resultAdapter != null) {
                            Bookmark resultBookmark = CommandUtil.bookmarkFor(resultAdapter);
                            backgroundCommand.setResult(resultBookmark);
                            backgroundInteraction.getCurrentExecution().setReturned(resultAdapter.getObject());
                        }

                    } else {

                        final CommandDto dto = jaxbService.fromXml(CommandDto.class, memento);

                        final MemberDto memberDto = dto.getMember();
                        final String memberId = memberDto.getMemberIdentifier();

                        final OidsDto oidsDto = CommandDtoUtils.targetsFor(dto);
                        final List<OidDto> targetOidDtos = oidsDto.getOid();

                        final InteractionType interactionType = memberDto.getInteractionType();
                        if(interactionType == InteractionType.ACTION_INVOCATION) {

                            final ActionDto actionDto = (ActionDto) memberDto;

                            for (OidDto targetOidDto : targetOidDtos) {

                                final ObjectAdapter targetAdapter = targetAdapterFor(targetOidDto);
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
                                    backgroundCommand.setResult(resultBookmark);
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

                    }

                } catch (RuntimeException e) {
                    // hmmm, this doesn't really make sense if >1 action
                    //
                    // in any case, the capturing of the result of the action invocation should be the
                    // responsibility of the interaction...
                    backgroundCommand.setException(Throwables.getStackTraceAsString(e));

                    // lower down the stack the IsisTransactionManager will have set the transaction to abort
                    // however, we don't want that to occur (because any changes made to the backgroundCommand itself
                    // would also be rolled back, and it would keep getting picked up again by a scheduler for
                    // processing); instead we clear the abort cause and ensure we can continue.
                    transactionManager.getCurrentTransaction().clearAbortCauseAndContinue();
                }

                // it's possible that there is no priorExecution, specifically if there was an exception
                // invoking the action.  We therefore need to guard that case.
                final Interaction.Execution priorExecution = backgroundInteraction.getPriorExecution();
                final Timestamp completedAt =
                        priorExecution != null
                                ? priorExecution.getCompletedAt()
                                : clockService.nowAsJavaSqlTimestamp();  // close enough...
                backgroundCommand.setCompletedAt(completedAt);
            }

            private ObjectAction findObjectAction(
                    final ObjectAdapter targetAdapter,
                    final String actionId) throws RuntimeException {

                final ObjectSpecification specification = targetAdapter.getSpecification();

                final ObjectAction objectAction = findActionElseNull(specification, actionId);
                if(objectAction == null) {
                    throw new RuntimeException(String.format("Unknown action '%s'", actionId));
                }
                return objectAction;
            }

            private OneToOneAssociation findOneToOneAssociation(
                    final ObjectAdapter targetAdapter,
                    final String propertyId) throws RuntimeException {


                final ObjectSpecification specification = targetAdapter.getSpecification();

                final OneToOneAssociation property = findOneToOneAssociationElseNull(specification, propertyId);
                if(property == null) {
                    throw new RuntimeException(String.format("Unknown property '%s'", propertyId));
                }
                return property;
            }
        });
    }

    protected ObjectAdapter newValueAdapterFor(final PropertyDto propertyDto) {
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
            return argAdapterFor(argType, arg);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected ObjectAdapter targetAdapterFor(final OidDto targetOidDto) {

//        // this is the original code, but it can be simplified ...
//        // (moved out to separate method so that, if proven wrong, can override as a patch)

//      final Bookmark bookmark = Bookmark.from(targetOidDto);
//      final Object targetObject = bookmarkService.lookup(bookmark);
//      final ObjectAdapter targetAdapter = adapterFor(targetObject);

        return adapterFor(targetOidDto);
    }

    protected ObjectAdapter argAdapterFor(final Class<?> argType, final Object arg) {

//        // this is the original code, but it can be simplified ...
//        // (moved out to separate method so that, if proven wrong, can override as a patch)

//        if(Bookmark.class != argType) {
//            return adapterFor(arg);
//        } else {
//            final Bookmark argBookmark = (Bookmark)arg;
//            final RootOid rootOid = RootOid.create(argBookmark);
//            return adapterFor(rootOid);
//        }

        return adapterFor(arg);
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
    private BookmarkService2 bookmarkService;

    @javax.inject.Inject
    private JaxbService jaxbService;

    @javax.inject.Inject
    private CommandContext commandContext;

    @javax.inject.Inject
    private InteractionContext interactionContext;

    @javax.inject.Inject
    private ClockService clockService;


}
