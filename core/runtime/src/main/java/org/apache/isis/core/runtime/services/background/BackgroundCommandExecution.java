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

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command.Executor;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.services.memento.MementoServiceDefault;
import org.apache.isis.core.runtime.sessiontemplate.AbstractIsisSessionTemplate;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosure;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.cmd.v1.CommandMementoDto;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.utils.CommandMementoDtoUtils;

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

    
    private void execute(final IsisTransactionManager transactionManager, final Command backgroundCommand) {
        transactionManager.executeWithinTransaction(
                backgroundCommand,
                new TransactionalClosure() {
            @Override
            public void execute() {

                final String memento = backgroundCommand.getMemento();

                try {
                    backgroundCommand.setExecutor(Executor.BACKGROUND);

                    final boolean legacy = memento.startsWith("<memento");
                    if(legacy) {

                        final ActionInvocationMemento aim = new ActionInvocationMemento(mementoService, memento);

                        final String actionId = aim.getActionId();

                        final Bookmark targetBookmark = aim.getTarget();
                        final Object targetObject = bookmarkService.lookup(targetBookmark);

                        final ObjectAdapter targetAdapter = adapterFor(targetObject);
                        final ObjectSpecification specification = targetAdapter.getSpecification();

                        final ObjectAction objectAction = findAction(specification, actionId);
                        if(objectAction == null) {
                            throw new Exception(String.format("Unknown action '%s'", actionId));
                        }

                        final ObjectAdapter[] argAdapters = argAdaptersFor(aim);
                        final ObjectAdapter resultAdapter = objectAction.execute(
                                targetAdapter, argAdapters, InteractionInitiatedBy.FRAMEWORK);
                        if(resultAdapter != null) {
                            Bookmark resultBookmark = CommandUtil.bookmarkFor(resultAdapter);
                            backgroundCommand.setResult(resultBookmark);
                        }

                    } else {

                        final CommandMementoDto dto = jaxbService.fromXml(CommandMementoDto.class, memento);
                        final ActionDto actionDto = dto.getAction();
                        final String actionId = actionDto.getActionIdentifier();

                        final List<OidDto> targetOidDtos = dto.getTargets();
                        for (OidDto targetOidDto : targetOidDtos) {

                            final Bookmark bookmark = Bookmark.from(targetOidDto);
                            final Object targetObject = bookmarkService.lookup(bookmark);

                            final ObjectAdapter targetAdapter = adapterFor(targetObject);

                            final ObjectAction objectAction =
                                    findObjectAction(targetAdapter, actionId);

                            final ObjectAdapter[] argAdapters = argAdaptersFor(dto);
                            final ObjectAdapter resultAdapter = objectAction.execute(
                                    targetAdapter, argAdapters, InteractionInitiatedBy.FRAMEWORK);

                            // this doesn't really make sense if >1 action
                            // in any case, the capturing of the action interaction should be the
                            // responsibiity of auditing/profiling
                            if(resultAdapter != null) {
                                Bookmark resultBookmark = CommandUtil.bookmarkFor(resultAdapter);
                                backgroundCommand.setResult(resultBookmark);
                            }
                        }
                    }

                } catch (Exception e) {
                    // this doesn't really make sense if >1 action
                    // in any case, the capturing of the action interaction should be the
                    // responsibiity of auditing/profiling
                    backgroundCommand.setException(Throwables.getStackTraceAsString(e));
                } finally {
                    // decided to keep this, even though really this
                    // should be the responsibility of auditing/profiling
                    backgroundCommand.setCompletedAt(clockService.nowAsJavaSqlTimestamp());
                }
            }

            private ObjectAction findObjectAction(
                    final ObjectAdapter targetAdapter,
                    final String actionId) throws Exception {

                final ObjectAction objectAction;

                final ObjectSpecification specification = targetAdapter.getSpecification();

                objectAction = findAction(specification, actionId);
                if(objectAction == null) {
                    throw new Exception("Unknown action '" + actionId + "'");
                }
                return objectAction;
            }
        });
    }

    private ObjectAction findAction(final ObjectSpecification specification, final String actionId) {
        final List<ObjectAction> objectActions = specification.getObjectActions(Contributed.INCLUDED);
        for (final ObjectAction objectAction : objectActions) {
            if(objectAction.getIdentifier().toClassAndNameIdentityString().equals(actionId)) {
                return objectAction;
            }
        }
        return null;
    }

    private ObjectAdapter[] argAdaptersFor(final ActionInvocationMemento aim) throws ClassNotFoundException {
        final int numArgs = aim.getNumArgs();
        final List<ObjectAdapter> argumentAdapters = Lists.newArrayList();
        for(int i=0; i<numArgs; i++) {
            final ObjectAdapter argAdapter = argAdapterFor(aim, i);
            argumentAdapters.add(argAdapter);
        }
        return argumentAdapters.toArray(new ObjectAdapter[]{});
    }

    private ObjectAdapter argAdapterFor(final ActionInvocationMemento aim, int num) throws ClassNotFoundException {
        final Class<?> argType = aim.getArgType(num);
        final Object arg = aim.getArg(num, argType);
        if(arg == null) {
            return null;
        }
        if(Bookmark.class != argType) {
            return adapterFor(arg);
        } else {
            final Bookmark argBookmark = (Bookmark)arg;
            final RootOid rootOid = RootOid.create(argBookmark);
            return adapterFor(rootOid);
        }
    }

    private ObjectAdapter[] argAdaptersFor(final CommandMementoDto dto) {
        final List<ParamDto> params = dto.getAction().getParameters();
        final List<ObjectAdapter> args = Lists.newArrayList(
                Iterables.transform(params, new Function<ParamDto, ObjectAdapter>() {
                    @Override
                    public ObjectAdapter apply(final ParamDto paramDto) {
                        final Object arg = CommandMementoDtoUtils.paramArgOf(paramDto);
                        return adapterFor(arg);
                    }
                })
        );
        return args.toArray(new ObjectAdapter[]{});
    }



    // //////////////////////////////////////

    @javax.inject.Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private JaxbService jaxbService;

    @javax.inject.Inject
    private CommandContext commandContext;

    @javax.inject.Inject
    private ClockService clockService;
}
