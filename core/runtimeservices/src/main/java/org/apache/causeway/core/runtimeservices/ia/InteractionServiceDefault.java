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
package org.apache.causeway.core.runtimeservices.ia;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.jspecify.annotations.NonNull;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactnlayer.InteractionCarrier;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayer;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerStack;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration.ObservationProvider;
import org.apache.causeway.core.interaction.scope.InteractionScopeBeanFactoryPostProcessor;
import org.apache.causeway.core.interaction.scope.InteractionScopeLifecycleHandler;
import org.apache.causeway.core.metamodel.execution.ExecutionContext;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class InteractionServiceDefault
implements
    InteractionService,
    InteractionLayerTracker {

    private final InteractionLayerStack layerStack = new InteractionLayerStack();

    private final ObservationProvider observationProvider;
    private final ServiceInjector serviceInjector;
    private final Provider<CommandPublisher> commandPublisherProvider;
    private final InteractionScopeLifecycleHandler interactionScopeLifecycleHandler;
    private final TransactionServiceSpring transactionServiceSpring;
    private final ExecutionContext executionContext;

    @SuppressWarnings("exports")
    @Inject
    public InteractionServiceDefault(
    		final ConfigurableBeanFactory beanFactory,
    		final ServiceInjector serviceInjector,
            final TransactionServiceSpring transactionServiceSpring,
            final ClockService clockService,
            final Provider<CommandPublisher> commandPublisherProvider,
            final ExecutionContext executionContext,
            final CausewayObservationIntegration observationIntegration) {
    	this.interactionScopeLifecycleHandler = InteractionScopeBeanFactoryPostProcessor.lookupScope(beanFactory);
        this.serviceInjector = serviceInjector;
        this.transactionServiceSpring = transactionServiceSpring;
        this.commandPublisherProvider = commandPublisherProvider;
        this.executionContext = executionContext;
        this.observationProvider = observationIntegration.provider(getClass(),
        		CausewayObservationIntegration.withModuleName(CausewayModuleCoreRuntimeServices.NAMESPACE));
    }

    @Override
    public int getInteractionLayerCount() {
        return layerStack.size();
    }

    @Override
    public InteractionLayer openInteraction() {
        return currentInteractionLayer()
                // or else create an anonymous authentication layer
                .orElseGet(()->openInteraction(InteractionContextFactory.anonymous()));
    }

    @Override
    public InteractionLayer openInteraction(final @NonNull InteractionContext interactionContextToUse) {

        // check whether we should reuse any current interactionLayer,
        // that is, if current authentication and authToUse are equal
        var reuseCurrentLayer = currentInteractionContext()
                .map(currentInteractionContext -> Objects.equals(currentInteractionContext, interactionContextToUse))
                .orElse(false);
        if(reuseCurrentLayer) {
			// we are done, just return the stack's top
            return currentInteractionLayerElseFail();
		}

        var interactionCarrier = currentInteractionLayer()
            .map(InteractionLayer::interactionCarrier)
            .orElseGet(()->new org.apache.causeway.core.metamodel.execution.InteractionCarrierDefault(executionContext));

        final int depth = getInteractionLayerCount();

        var obs = observationProvider.get(depth == 0
                ? "Causeway Root Interaction"
                : "Causeway Nested Interaction");
        var newInteractionLayer = layerStack.push(interactionCarrier, interactionContextToUse, obs);

        _Observation.addTags(obs, interactionContextToUse, depth);

        if(depth == 0) {
            transactionServiceSpring.onOpen(interactionCarrier);
            interactionScopeLifecycleHandler.onTopLevelInteractionOpened();
        }

        if(log.isDebugEnabled()) {
            log.debug("new interaction layer created (interactionId={}, total-layers-on-stack={}, {})",
                    currentInteraction().map(Interaction::getInteractionId).orElse(null),
                    getInteractionLayerCount(),
                    _Probe.currentThreadId());
        }

        if(XrayUi.isXrayEnabled()) {
            _Xray.newInteractionLayer(newInteractionLayer);
        }

        return newInteractionLayer;
    }

    @Override
    public void closeInteractionLayers() {
        log.debug("about to close the interaction stack (interactionId={}, total-layers-on-stack={}, {})",
                currentInteraction().map(Interaction::getInteractionId).orElse(null),
                layerStack.size(),
                _Probe.currentThreadId());

        //
        // TODO: Be aware that this method could theoretically throw an exception, if the flush fails in
        //  preInteractionClosed.  Elsewhere where we make this call it's not clear that this is correct
        //
        closeInteractionLayerStackDownToStackSize(0);
    }

	@Override
    public Optional<InteractionLayer> currentInteractionLayer() {
	    return layerStack.currentLayer();
    }

    @Override
    public boolean isInInteraction() {
        return !layerStack.isEmpty();
    }

    // -- AUTHENTICATED EXECUTION

    @Override
    @SneakyThrows
    public <R> R call(
            final @NonNull InteractionContext interactionContext,
            final @NonNull Callable<R> callable) {

        final int stackSizeWhenEntering = layerStack.size();
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

        final int stackSizeWhenEntering = layerStack.size();
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
        if(layerStack.isEmpty()) {
            // seeing this code-path, when the corresponding runnable/callable
            // by itself causes the interaction stack to be closed
            log.warn("unexpected state: missing interaction (layer) on interaction rollback; "
                    + "rollback was caused by {} -> {}",
                    cause.getClass().getName(),
                    cause.getMessage());
            return;
        }
        var interactionCarrier = layerStack.peek().rootLayer().interactionCarrier();
        transactionServiceSpring.requestRollback(interactionCarrier);
    }

    private boolean isAtRootLevel() {
    	return layerStack.size()==1;
    }

    @SneakyThrows
    private void preInteractionClosed(final InteractionCarrier interactionCarrier) {

        Throwable flushException = null;

        // a bit of a hacky guard
        //
        //
        // we check if the transaction is already completed (rolled back/committed).  This isn't meant to be the case,
        // but the suspicion is that if a background command execution encounters a deadlock then (in
        // CommandExecutorServiceDefault) then it might be resulting in a top-level xactn that ends up being rolled back.
        //
        // The relevant code is in TransactionService#callWithinCurrentTransactionElseCreateNew(...), used by
        // CommandExecutorServiceDefault but also used quite heavily elsewhere.  In normal circumstances I suspect
        // everything works out fine, but if there's a deadlock then perhaps we get this different flow.
        //
        // The consequences of an incorrect design can be SEVERE.  In previous versions of the code base we've seen
        // additional changes being made in a new/implicit (?) xactn which furthermore is never committed; we end up
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
                    transactionServiceSpring.requestRollback(interactionCarrier);
                }
            }
            // the net effect of this is to call either txManager.rollback(txStatus) or txManager.commit(txStatus) depending upon
            // whether txStatus.setRollbackOnly(...) was ever called.
            // anything has called setRollbackOnly so far.
            transactionServiceSpring.onClose(interactionCarrier);
        }

        // cleanup the InteractionScope (Spring scope)
        interactionScopeLifecycleHandler.onTopLevelInteractionPreDestroy();
        interactionScopeLifecycleHandler.onTopLevelInteractionClosed();
        //interactionCarrier.close(); // do this last

        if(flushException!=null) {
            throw flushException;
        }
    }

    private void closeInteractionLayerStackDownToStackSize(final int downToStackSize) {
        if(layerStack.isEmpty()) {
			return;
		}
        if(downToStackSize<0) {
			throw new IllegalArgumentException("required non-negative");
		}

        log.debug("about to close interaction stack down to size {} (interactionId={}, total-layers-on-stack={}, {})",
                downToStackSize,
                currentInteraction().map(Interaction::getInteractionId).orElse(null),
                layerStack.size(),
                _Probe.currentThreadId());

        try {
            layerStack.popWhile(currentLayer->{
                if(!(layerStack.size()>downToStackSize)) {
					return false;
				}
                if(isAtRootLevel()) {
                    // keep the stack unmodified yet, to allow for callbacks to properly operate
                    preInteractionClosed(currentLayer.interactionCarrier());
                }
                _Xray.closeInteractionLayer(currentLayer);
                return true;
            });
        } finally {
            // preInteractionClosed above could conceivably throw an exception, so we'll tidy up our threadlocal
            // here to ensure everything is cleaned up
            if(downToStackSize == 0) {
                // cleanup thread-local
                layerStack.clear();
            }
        }
    }

    // -- HELPER - COMMAND COMPLETION

    /**
     * called by {@link TransactionServiceSpring}, but to be moved.
     */
    @Programmatic
    public void completeAndPublishCurrentCommand() {

    	var interactionCarrier = currentInteractionCarrierElseFail();
        var interaction = interactionCarrier.interaction();
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
                        executionContext.clockService().getClock().nowAsJavaSqlTimestamp();

            command.updater().setCompletedAt(completedAt);
        }

        command.updater().setPublishingPhase(Command.CommandPublishingPhase.COMPLETED);
        commandPublisherProvider.get().complete(command);
    }

}
