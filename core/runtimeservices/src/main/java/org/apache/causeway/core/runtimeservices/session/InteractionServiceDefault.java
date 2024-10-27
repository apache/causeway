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
package org.apache.causeway.core.runtimeservices.session;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayer;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.util.schema.ChangesDtoUtils;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.util.schema.InteractionDtoUtils;
import org.apache.causeway.applib.util.schema.InteractionsDtoUtils;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.concurrent._ConcurrentContext;
import org.apache.causeway.commons.internal.concurrent._ConcurrentTaskList;
import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.interaction.scope.InteractionScopeBeanFactoryPostProcessor;
import org.apache.causeway.core.interaction.scope.InteractionScopeLifecycleHandler;
import org.apache.causeway.core.interaction.session.CausewayInteraction;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.runtime.events.MetamodelEventService;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * Default implementation of {@link InteractionService}, keeping track of the current {@link Interaction}
 *
 * @implNote holds a reference to the current session using a thread-local
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".InteractionServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class InteractionServiceDefault
implements
    InteractionService,
    InteractionLayerTracker {

    // TODO: reading the javadoc for TransactionSynchronizationManager and looking at the implementations
    //  of TransactionSynchronization (in particular SpringSessionSynchronization), I suspect that this
    //  ThreadLocal would be considered bad practice and instead should be managed using the TransactionSynchronization mechanism.
    final ThreadLocal<Stack<InteractionLayer>> interactionLayerStack = ThreadLocal.withInitial(Stack::new);

    final MetamodelEventService runtimeEventService;
    final SpecificationLoader specificationLoader;
    final ServiceInjector serviceInjector;

    final ClockService clockService;
    final Provider<CommandPublisher> commandPublisherProvider;
    final ConfigurableBeanFactory beanFactory;

    final InteractionScopeLifecycleHandler interactionScopeLifecycleHandler;
    final TransactionServiceSpring transactionServiceSpring;

    final InteractionIdGenerator interactionIdGenerator;

    @Inject
    public InteractionServiceDefault(
            final MetamodelEventService runtimeEventService,
            final SpecificationLoader specificationLoader,
            final ServiceInjector serviceInjector,
            final TransactionServiceSpring transactionServiceSpring,
            final ClockService clockService,
            final Provider<CommandPublisher> commandPublisherProvider,
            final ConfigurableBeanFactory beanFactory,
            final InteractionIdGenerator interactionIdGenerator) {
        this.runtimeEventService = runtimeEventService;
        this.specificationLoader = specificationLoader;
        this.serviceInjector = serviceInjector;
        this.transactionServiceSpring = transactionServiceSpring;
        this.clockService = clockService;
        this.commandPublisherProvider = commandPublisherProvider;
        this.beanFactory = beanFactory;
        this.interactionIdGenerator = interactionIdGenerator;

        this.interactionScopeLifecycleHandler = InteractionScopeBeanFactoryPostProcessor.lookupScope(beanFactory);
    }

    @EventListener
    public void init(final ContextRefreshedEvent event) {

        log.info("Initialising Causeway System");
        log.info("working directory: {}", new File(".").getAbsolutePath());

        runtimeEventService.fireBeforeMetamodelLoading();

        var taskList = _ConcurrentTaskList.named("CausewayInteractionFactoryDefault Init")
                .addRunnable("SpecificationLoader::createMetaModel", specificationLoader::createMetaModel)
                .addRunnable("ChangesDtoUtils::init", ChangesDtoUtils::init)
                .addRunnable("InteractionDtoUtils::init", InteractionDtoUtils::init)
                .addRunnable("InteractionsDtoUtils::init", InteractionsDtoUtils::init)
                .addRunnable("CommandDtoUtils::init", CommandDtoUtils::init)
                ;

        taskList.submit(_ConcurrentContext.forkJoin());
        taskList.await();

        { // log any validation failures, experimental code however, not sure how to best propagate failures
            var validationResult = specificationLoader.getOrAssessValidationResult();
            if(validationResult.getNumberOfFailures()==0) {
                log.info("Validation PASSED");
            } else {
                log.error("### Validation FAILED, failure count: {}", validationResult.getNumberOfFailures());
                validationResult.forEach(failure->{
                    log.error("# " + failure.getMessage());
                });
                //throw _Exceptions.unrecoverable("Validation FAILED");
            }
        }

        runtimeEventService.fireAfterMetamodelLoaded();

    }

    @Override
    public int getInteractionLayerCount() {
        return interactionLayerStack.get().size();
    }

    @Override
    public InteractionLayer openInteraction() {
        return currentInteractionLayer()
                // or else create an anonymous authentication layer
                .orElseGet(()->openInteraction(InteractionContextFactory.anonymous()));
    }

    @Override
    public InteractionLayer openInteraction(
            final @NonNull InteractionContext interactionContextToUse) {

        var causewayInteraction = getOrCreateCausewayInteraction();

        // check whether we should reuse any current interactionLayer,
        // that is, if current authentication and authToUse are equal

        var reuseCurrentLayer = currentInteractionContext()
                .map(currentInteractionContext -> Objects.equals(currentInteractionContext, interactionContextToUse))
                .orElse(false);

        if(reuseCurrentLayer) {
            // we are done, just return the stack's top
            return interactionLayerStack.get().peek();
        }

        var interactionLayer = new InteractionLayer(causewayInteraction, interactionContextToUse);

        interactionLayerStack.get().push(interactionLayer);

        if(isAtTopLevel()) {
            transactionServiceSpring.onOpen(causewayInteraction);
            interactionScopeLifecycleHandler.onTopLevelInteractionOpened();
        }

        if(log.isDebugEnabled()) {
            log.debug("new interaction layer created (interactionId={}, total-layers-on-stack={}, {})",
                    currentInteraction().map(Interaction::getInteractionId).orElse(null),
                    interactionLayerStack.get().size(),
                    _Probe.currentThreadId());
        }

        if(XrayUi.isXrayEnabled()) {
            _Xray.newInteractionLayer(interactionLayerStack.get());
        }

        return interactionLayer;
    }

    private CausewayInteraction getOrCreateCausewayInteraction() {

        final Stack<InteractionLayer> interactionLayers = interactionLayerStack.get();
        return interactionLayers.isEmpty()
    			? new CausewayInteraction(interactionIdGenerator.interactionId())
				: _Casts.uncheckedCast(interactionLayers.firstElement().getInteraction());
    }

    @Override
    public void closeInteractionLayers() {
        log.debug("about to close the interaction stack (interactionId={}, total-layers-on-stack={}, {})",
                currentInteraction().map(Interaction::getInteractionId).orElse(null),
                interactionLayerStack.get().size(),
                _Probe.currentThreadId());

        //
        // TODO: Be aware that this method could theoretically throw an exception, if the flush fails in
        //  preInteractionClosed.  Elsewhere where we make this call it's not clear that this is correct
        //
        closeInteractionLayerStackDownToStackSize(0);
    }

	@Override
    public Optional<InteractionLayer> currentInteractionLayer() {
    	var stack = interactionLayerStack.get();
    	return stack.isEmpty()
    	        ? Optional.empty()
                : Optional.of(stack.lastElement());
    }

    @Override
    public boolean isInInteraction() {
        return !interactionLayerStack.get().isEmpty();
    }

    // -- AUTHENTICATED EXECUTION

    @Override
    @SneakyThrows
    public <R> R call(
            final @NonNull InteractionContext interactionContext,
            final @NonNull Callable<R> callable) {

        final int stackSizeWhenEntering = interactionLayerStack.get().size();
        openInteraction(interactionContext);
        try {
            return callInternal(callable);
        } finally {
            //
            // TODO: this method could theoretically throw an exception, if the flush fails in
            //  preInteractionClosed.  It]m uncertain what to do here.  The callable executed in the try block
            //  may be returning a Try, which could encode a failure that way.  Having this method also possibly
            //  throw an exception seems incorrect.
            //
            closeInteractionLayerStackDownToStackSize(stackSizeWhenEntering);
        }
    }

    @Override
    @SneakyThrows
    public void run(
            final @NonNull InteractionContext interactionContext,
            final @NonNull ThrowingRunnable runnable) {

        final int stackSizeWhenEntering = interactionLayerStack.get().size();
        openInteraction(interactionContext);
        try {
            runInternal(runnable);
        } finally {
            //
            // TODO: this method could theoretically throw an exception, if the flush fails in
            //  preInteractionClosed.  It]m uncertain what to do here.  The callable executed in the try block
            //  may be returning a Try, which could encode a failure that way.  Having this method also possibly
            //  throw an exception seems incorrect.
            //
            closeInteractionLayerStackDownToStackSize(stackSizeWhenEntering);
        }
    }

    // -- ANONYMOUS EXECUTION

    @Override
    @SneakyThrows
    public <R> R callAnonymous(final @NonNull Callable<R> callable) {
        if(isInInteraction()) {
            return callInternal(callable); // participate in existing session
        }
        return call(InteractionContextFactory.anonymous(), callable);
    }

    /**
     * Variant of {@link #callAnonymous(Callable)} that takes a runnable.
     * @param runnable
     */
    @Override
    @SneakyThrows
    public void runAnonymous(final @NonNull ThrowingRunnable runnable) {
        if(isInInteraction()) {
            runInternal(runnable); // participate in existing session
            return;
        }
        run(InteractionContextFactory.anonymous(), runnable);
    }

    // -- INTERACTION ID

    @Override
    public Optional<UUID> getInteractionId() {
        return currentInteraction().map(Interaction::getInteractionId);
    }

    // -- HELPER

    @SneakyThrows
    private <R> R callInternal(final @NonNull Callable<R> callable) {
        serviceInjector.injectServicesInto(callable);
        try {
            return callable.call();
        } catch (Throwable e) {
            requestRollback(e);
            throw e;
        }
    }

    @SneakyThrows
    private void runInternal(final @NonNull ThrowingRunnable runnable) {
        serviceInjector.injectServicesInto(runnable);
        try {
            runnable.run();
        } catch (Throwable e) {
            requestRollback(e);
            throw e;
        }
    }

    private void requestRollback(final Throwable cause) {
        var stack = interactionLayerStack.get();
        if(stack.isEmpty()) {
            // seeing this code-path, when the corresponding runnable/callable
            // by itself causes the interaction stack to be closed
            log.warn("unexpected state: missing interaction (layer) on interaction rollback; "
                    + "rollback was caused by {} -> {}",
                    cause.getClass().getName(),
                    cause.getMessage());
            return;
        }
        var interaction = _Casts.<CausewayInteraction>uncheckedCast(stack.get(0).getInteraction());
        transactionServiceSpring.requestRollback(interaction);
    }

    private boolean isAtTopLevel() {
    	return interactionLayerStack.get().size()==1;
    }

    @SneakyThrows
    private void preInteractionClosed(final CausewayInteraction interaction) {

        Throwable flushException = null;

        // a bit of a hacky guard
        //
        //
        // we check if the transaction is already completed (rolled back/committed).  This isn't meant to be the case,
        // but the suspicion is that if a background command execution encounters a deadlock then (in
        // CommandExecutorServiceDefault) then it might be resulting in top-level xactn will end up being rolled back.
        //
        // The relevant code is in TransactionService#callWithinCurrentTransactionElseCreateNew(...), used by
        // CommandExecutorServiceDefault but also used quite heavily elsewhere.  In normal circumstances I suspect
        // everything works out fine, but if there's a deadlock then perhaps we get this different flow
        //
        // the consequences of an incorrect design can be SEVERE.  In previous versions of the code base we've seen
        // additional changes being made in a new/implicit (?) xactn which furthermore are never committed; we end up
        // with a connection back in Hikari's conn pool with open locks, blocking the entire system as those locks are
        // on CommandLogEntry.  Or, another problem found was seemingly polluting the threadLocals, resulting in an
        // nio-http-exec thread always failing with an error of: "No JDO PersistenceManager bound to thread, and
        // configuration does not allow creation of non-transactional one here" (see PersistenceManagerFactoryUtils).
        // Both of these errors require the app to be restarted.
        //
        // To be clear, it's not yet certain that we've found the underlying issue, but if the following warning is
        // detected in the logs, then that might be a good thing.
        //
        if(transactionServiceSpring.currentTransactionState().isComplete()) {

            // something completed the transaction under our feet; was it a deadlock perhaps?
            log.warn("preInteractionClosed: skipping as a precaution because current transaction has been completed already");

        } else {
            var mustAbort = transactionServiceSpring.currentTransactionState().mustAbort();
            if(!mustAbort) {
                try {
                    transactionServiceSpring.flushTransaction();
                    // publish only when flush was successful
                    completeAndPublishCurrentCommand();
                } catch (Throwable e) {
                    //[CAUSEWAY-3262] if flush fails rethrow later, when interaction was closed ...
                    flushException = e;
                    transactionServiceSpring.requestRollback(interaction);
                }
            }
            // the net effect of this is to call either txManager.rollback(txStatus) or txManager.commit(txStatus) depending upon
            // whether txStatus.setRollbackOnly(...) was ever called.
            // anything has called setRollbackOnly so far.
            transactionServiceSpring.onClose(interaction);
        }

        // cleanup the InteractionScope (Spring scope)
        interactionScopeLifecycleHandler.onTopLevelInteractionPreDestroy();
        interactionScopeLifecycleHandler.onTopLevelInteractionClosed();
        interaction.close(); // do this last

        if(flushException!=null) {
            throw flushException;
        }
    }

    private void closeInteractionLayerStackDownToStackSize(final int downToStackSize) {

        log.debug("about to close interaction stack down to size {} (interactionId={}, total-layers-on-stack={}, {})",
                downToStackSize,
                currentInteraction().map(Interaction::getInteractionId).orElse(null),
                interactionLayerStack.get().size(),
                _Probe.currentThreadId());

        var stack = interactionLayerStack.get();
        try {
            while(stack.size()>downToStackSize) {
                if(isAtTopLevel()) {
                    // keep the stack unmodified yet, to allow for callbacks to properly operate

                    preInteractionClosed(_Casts.uncheckedCast(stack.peek().getInteraction()));
                }
                _Xray.closeInteractionLayer(stack);
                stack.pop();
            }
        } finally {
            // preInteractionClosed above could conceivably throw an exception, so we'll tidy up our threadlocal
            // here to ensure everything is cleaned up
            if(downToStackSize == 0) {
                // cleanup thread-local
                interactionLayerStack.remove();
            }
        }
    }

    private CausewayInteraction getInternalInteractionElseFail() {
        var interaction = currentInteractionElseFail();
        if(interaction instanceof CausewayInteraction) {
            return (CausewayInteraction) interaction;
        }
        throw _Exceptions.unrecoverable("the framework does not recognize "
                + "this implementation of an Interaction: %s", interaction.getClass().getName());
    }

    // -- HELPER - COMMAND COMPLETION

    /**
     * called by {@link TransactionServiceSpring}, but to be moved.
     */
    @Programmatic
    public void completeAndPublishCurrentCommand() {

        var interaction = getInternalInteractionElseFail();
        var command = interaction.getCommand();

        if(command.getStartedAt() != null && command.getCompletedAt() == null) {
            // the guard is in case we're here as the result of a redirect following a previous exception; patch up as best we can.

            var priorInteractionExecution = interaction.getPriorExecution();
            var completedAt =
                    priorInteractionExecution != null
                    ?
                        // copy over from the most recent (which will be the top-level) interaction
                        priorInteractionExecution.getCompletedAt()
                    :
                        // this could arise as the result of calling InteractionService#nextInteraction within an action
                        // the best we can do is to use the current time
                        clockService.getClock().nowAsJavaSqlTimestamp();

            command.updater().setCompletedAt(completedAt);
        }

        command.updater().setPublishingPhase(Command.CommandPublishingPhase.COMPLETED);
        commandPublisherProvider.get().complete(command);

        interaction.clear();
    }

}
