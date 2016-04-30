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

package org.apache.isis.core.metamodel.facets.actions.action.invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.metamodel.MetaModelService2;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.commons.lang.ThrowableExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.CollectionUtils;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.ElementSpecificationProviderFromTypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.ReflectiveActionException;
import org.apache.isis.core.metamodel.transactions.TransactionState;
import org.apache.isis.core.metamodel.transactions.TransactionStateProvider;

public abstract class ActionInvocationFacetForDomainEventAbstract
        extends ActionInvocationFacetAbstract
        implements ImperativeFacet {

    private final static Logger LOG = LoggerFactory.getLogger(ActionInvocationFacetForDomainEventAbstract.class);

    private final Method method;
    private final ObjectSpecification onType;
    private final ObjectSpecification returnType;

    private final AdapterManager adapterManager;
    private final DeploymentCategory deploymentCategory;
    private final AuthenticationSessionProvider authenticationSessionProvider;

    private final ServicesInjector servicesInjector;
    private final IsisConfiguration configuration;
    private final TransactionStateProvider transactionStateProvider;
    private final Class<? extends ActionDomainEvent<?>> eventType;
    private final DomainEventHelper domainEventHelper;

    public ActionInvocationFacetForDomainEventAbstract(
            final Class<? extends ActionDomainEvent<?>> eventType,
            final Method method,
            final ObjectSpecification onType,
            final ObjectSpecification returnType,
            final FacetHolder holder,
            final DeploymentCategory deploymentCategory,
            final IsisConfiguration configuration,
            final ServicesInjector servicesInjector,
            final AuthenticationSessionProvider authenticationSessionProvider,
            final AdapterManager adapterManager,
            final TransactionStateProvider transactionStateProvider) {
        super(holder);
        this.eventType = eventType;
        this.method = method;
        this.onType = onType;
        this.returnType = returnType;
        this.deploymentCategory = deploymentCategory;
        this.authenticationSessionProvider = authenticationSessionProvider;
        this.adapterManager = adapterManager;
        this.servicesInjector = servicesInjector;
        this.configuration = configuration;
        this.transactionStateProvider = transactionStateProvider;
        this.domainEventHelper = new DomainEventHelper(this.servicesInjector);

    }

    /**
     * Returns a singleton list of the {@link java.lang.reflect.Method} provided in the
     * constructor.
     */
    @Override
    public List<Method> getMethods() {
        return Collections.singletonList(method);
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.EXECUTE;
    }

    @Override
    public ObjectSpecification getReturnType() {
        return returnType;
    }

    @Override
    public ObjectSpecification getOnType() {
        return onType;
    }


    /**
     * Introduced to disambiguate the meaning of <tt>null</tt> as a return value of
     * {@link ActionInvocationFacet#invoke(ObjectAction, ObjectAdapter, ObjectAdapter[], InteractionInitiatedBy)}
     */
    public static class InvocationResult {

        public static InvocationResult forActionThatReturned(final ObjectAdapter resultAdapter) {
            return new InvocationResult(true, resultAdapter);
        }

        public static InvocationResult forActionNotInvoked() {
            return new InvocationResult(false, null);
        }

        private final boolean whetherInvoked;
        private final ObjectAdapter adapter;

        private InvocationResult(final boolean whetherInvoked, final ObjectAdapter result) {
            this.whetherInvoked = whetherInvoked;
            this.adapter = result;
        }

        public boolean getWhetherInvoked() {
            return whetherInvoked;
        }

        /**
         * Returns the result, or null if either the action invocation returned null or
         * if the action was never invoked in the first place.
         *
         * <p>
         * Use {@link #getWhetherInvoked()} to distinguish between these two cases.
         */
        public ObjectAdapter getAdapter() {
            return adapter;
        }
    }

    @Override
    public ObjectAdapter invoke(
            final ObjectAction owningAction,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] arguments,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        // ... post the executing event
        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.EXECUTING,
                        eventType, null,
                        owningAction, targetAdapter, arguments,
                        command,
                        null);

        final InvocationResult invocationResult = invoke(owningAction, targetAdapter, arguments);

        final ObjectAdapter invocationResultAdapter = invocationResult.getAdapter();

        // ... post the executed event
        if (invocationResult.getWhetherInvoked()) {
            // perhaps the Action was not properly invoked (i.e. an exception was raised).
            // If invoked ok, then post to the event bus
            domainEventHelper.postEventForAction(
                    AbstractDomainEvent.Phase.EXECUTED,
                    eventType, verify(event),
                    owningAction, targetAdapter, arguments,
                    command,
                    invocationResultAdapter);
        }

        if (invocationResultAdapter == null) {
            return null;
        }

        return filteredIfRequired(invocationResultAdapter, interactionInitiatedBy);

    }

    protected InvocationResult invoke(
            final ObjectAction owningAction,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] arguments) {

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        try {
            setupActionInvocationContext(owningAction, targetAdapter);

            owningAction.setupCommand(targetAdapter, arguments);

            ObjectAdapter resultAdapter = invokeThruCommand(owningAction, targetAdapter, arguments);

            return InvocationResult.forActionThatReturned(resultAdapter);

        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            if (targetException instanceof IllegalStateException) {
                throw new ReflectiveActionException( String.format(
                        "IllegalStateException thrown while executing %s %s",
                        method, targetException.getMessage()), targetException);
            }
            if(targetException instanceof RecoverableException) {
                if (!getTransactionState().canCommit()) {
                    // something severe has happened to the underlying transaction;
                    // so escalate this exception to be non-recoverable
                    final Throwable targetExceptionCause = targetException.getCause();
                    Throwable nonRecoverableCause = targetExceptionCause != null? targetExceptionCause: targetException;

                    // trim to first 300 chars
                    final String message = trim(nonRecoverableCause.getMessage(), 300);

                    throw new NonRecoverableException(message, nonRecoverableCause);
                }
            }

            ThrowableExtensions.throwWithinIsisException(e, "Exception executing " + method);

            // Action was not invoked (an Exception was thrown)
            return InvocationResult.forActionNotInvoked();

        } catch (final IllegalAccessException e) {
            throw new ReflectiveActionException("Illegal access of " + method, e);
        }
    }

    private static String trim(String message, final int maxLen) {
        if(!Strings.isNullOrEmpty(message)) {
            message = message.substring(0, Math.min(message.length(), maxLen));
            if(message.length() == maxLen) {
                message += " ...";
            }
        }
        return message;
    }

    protected void setupActionInvocationContext(
            final ObjectAction owningAction,
            final ObjectAdapter targetAdapter) {

        owningAction.setupActionInvocationContext(targetAdapter);
    }

    protected ObjectAdapter invokeThruCommand(
            final ObjectAction owningAction,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] argumentAdapters)
            throws IllegalAccessException, InvocationTargetException {

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        final InteractionContext interactionContext = getInteractionContext();
        final Interaction interaction = interactionContext.getInteraction();


        final ObjectAdapter resultAdapter;
        if( command.getExecutor() == Command.Executor.USER &&
                command.getExecuteIn() == org.apache.isis.applib.annotation.Command.ExecuteIn.BACKGROUND) {

            // deal with background commands

            // persist command so can be this command can be in the 'background'
            final CommandService commandService = getCommandService();
            if (!commandService.persistIfPossible(command)) {
                throw new IsisException(
                        "Unable to schedule action '"
                                + owningAction.getIdentifier().toClassAndNameIdentityString() + "' to run in background: "
                                + "CommandService does not support persistent commands " );
            }
            resultAdapter = getAdapterManager().adapterFor(command);

        } else {

            // otherwise, go ahead and execute action in the 'foreground'

            final Object target = ObjectAdapter.Util.unwrap(targetAdapter);
            final List<Object> arguments = ObjectAdapter.Util.unwrap(Arrays.asList(argumentAdapters));


            final Interaction.ActionArgs actionArgs = new Interaction.ActionArgs(command, target, arguments);
            final Interaction.MemberCallable callable = new Interaction.MemberCallable<Interaction.ActionArgs>() {
                @Override
                public Object call(final Interaction.ActionArgs actionArgs) {

                    try {
                        final Object resultPojo = invokeMethodElseFromCache(targetAdapter, argumentAdapters);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug(" action result " + resultPojo);
                        }

                        ObjectAdapter resultAdapter = cloneIfViewModelCloneable(resultPojo, targetAdapter);

                        return resultAdapter != null ? resultAdapter.getObject() : null;

                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            };


            interaction.execute(callable, actionArgs, getClockService());

            final Interaction.Execution priorExecution = interaction.getPriorExecution();

            final RuntimeException executionExceptionIfAny = priorExecution.getException();
            if(executionExceptionIfAny != null) {
                throw executionExceptionIfAny;
            }

            resultAdapter = getAdapterManager().adapterFor(priorExecution.getResult());
            setCommandResultIfEntity(command, resultAdapter);

            // TODO: use InteractionContext instead
            captureCurrentInvocationForPublishing(owningAction, targetAdapter, argumentAdapters, command, resultAdapter);

        }
        return resultAdapter;
    }


    protected Object invokeMethodElseFromCache(
            final ObjectAdapter targetAdapter, final ObjectAdapter[] arguments)
            throws IllegalAccessException, InvocationTargetException {

        final Object[] executionParameters = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            executionParameters[i] = unwrap(arguments[i]);
        }

        final Object targetPojo = unwrap(targetAdapter);

        final ActionSemanticsFacet semanticsFacet = getFacetHolder().getFacet(ActionSemanticsFacet.class);
        final boolean cacheable = semanticsFacet != null && semanticsFacet.value().isSafeAndRequestCacheable();
        if(cacheable) {
            final QueryResultsCache queryResultsCache = getQueryResultsCache();
            final Object[] targetPojoPlusExecutionParameters = ArrayExtensions.appendT(executionParameters, targetPojo);
            return queryResultsCache.execute(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return method.invoke(targetPojo, executionParameters);
                }
            }, targetPojo.getClass(), method.getName(), targetPojoPlusExecutionParameters);

        } else {
            return method.invoke(targetPojo, executionParameters);
        }
    }

    protected ObjectAdapter cloneIfViewModelCloneable(
            final Object resultPojo,
            final ObjectAdapter targetAdapter) {

        // to remove boilerplate from the domain, we automatically clone the returned object if it is a view model.

        if (resultPojo != null) {
            final ObjectAdapter resultAdapter = getAdapterManager().adapterFor(resultPojo);
            return cloneIfViewModelElse(resultAdapter, resultAdapter);
        } else {
            // if void or null, attempt to clone the original target, else return null.
            return cloneIfViewModelElse(targetAdapter, null);
        }
    }

    private ObjectAdapter cloneIfViewModelElse(final ObjectAdapter adapter, final ObjectAdapter dfltAdapter) {

        if (!adapter.getSpecification().isViewModelCloneable(adapter)) {
            return  dfltAdapter;
        }

        final ViewModelFacet viewModelFacet = adapter.getSpecification().getFacet(ViewModelFacet.class);
        final Object clone = viewModelFacet.clone(adapter.getObject());

        final ObjectAdapter clonedAdapter = getAdapterManager().adapterFor(clone);

        // copy over TypeOfFacet if required
        final TypeOfFacet typeOfFacet = getFacetHolder().getFacet(TypeOfFacet.class);
        clonedAdapter.setElementSpecificationProvider(ElementSpecificationProviderFromTypeOfFacet.createFrom(typeOfFacet));

        return clonedAdapter;
    }


    protected void setCommandResultIfEntity(final Command command, final ObjectAdapter resultAdapter) {
        if(command.getResult() != null) {
            // don't trample over any existing result, eg subsequent mixins.
            return;
        }
        if (resultAdapter == null) {
            return;
        }

        final Class<?> domainType = resultAdapter.getSpecification().getCorrespondingClass();
        final MetaModelService2.Sort sort = getMetaModelService().sortOf(domainType);
        switch (sort) {
        case JDO_ENTITY:
            final Object domainObject = resultAdapter.getObject();
            // ensure that any still-to-be-persisted adapters get persisted to DB.
            if(!getRepositoryService().isPersistent(domainObject)) {
                getTransactionService().flushTransaction();
            }
            if(getRepositoryService().isPersistent(domainObject)) {
                BookmarkService bookmarkService = getBookmarkService();
                Bookmark bookmark = bookmarkService.bookmarkFor(domainObject);
               command.setResult(bookmark);
            }
            break;
        default:
            // ignore all other sorts of objects
            break;
        }
    }

    private MetaModelService2 getMetaModelService() {
        return lookupServiceIfAny(MetaModelService2.class);
    }

    private TransactionService getTransactionService() {
        return lookupServiceIfAny(TransactionService.class);
    }

    private BookmarkService getBookmarkService() {
        return lookupServiceIfAny(BookmarkService.class);
    }

    private RepositoryService getRepositoryService() {
        return lookupServiceIfAny(RepositoryService.class);
    }

    protected void captureCurrentInvocationForPublishing(
            final ObjectAction owningAction,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] arguments,
            final Command command,
            final ObjectAdapter resultAdapter) {

        // TODO: should instead be using the top-level ActionInvocationMemento associated with command.

        final PublishedActionFacet publishedActionFacet = getIdentified().getFacet(PublishedActionFacet.class);
        if (publishedActionFacet != null && currentInvocation.get() == null) {
            final CurrentInvocation currentInvocation = new CurrentInvocation(
                    targetAdapter, owningAction, getIdentified(),
                    arguments, resultAdapter, command);
            ActionInvocationFacet.currentInvocation.set(currentInvocation);
        }
    }

    protected ObjectAdapter filteredIfRequired(
            final ObjectAdapter resultAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final boolean filterForVisibility = getConfiguration().getBoolean("isis.reflector.facet.filterVisibility", true);
        if (!filterForVisibility) {
            return resultAdapter;
        }

        final Object result = resultAdapter.getObject();

        if(result instanceof Collection || result.getClass().isArray()) {
            final CollectionFacet facet = CollectionFacet.Utils.getCollectionFacetFromSpec(resultAdapter);

            final Iterable<ObjectAdapter> adapterList = facet.iterable(resultAdapter);
            final List<ObjectAdapter> visibleAdapters =
                    ObjectAdapter.Util.visibleAdapters(
                            adapterList,
                            interactionInitiatedBy);
            final Object visibleObjects =
                    CollectionUtils.copyOf(
                            Lists.transform(visibleAdapters, ObjectAdapter.Functions.getObject()),
                            method.getReturnType());
            if (visibleObjects != null) {
                return getAdapterManager().adapterFor(visibleObjects);
            }

            // would be null if unable to take a copy (unrecognized return type)
            // fallback to returning the original adapter, without filtering for visibility

            return resultAdapter;

        } else {
            boolean visible = ObjectAdapter.Util.isVisible(resultAdapter, interactionInitiatedBy);
            return visible ? resultAdapter : null;
        }
    }


    /**
     * Optional hook to allow the facet implementation for the deprecated {@link org.apache.isis.applib.annotation.PostsActionInvokedEvent} annotation
     * to discard the event if the domain event is of a different type (specifically if was installed by virtue of a no
     * @{@link org.apache.isis.applib.annotation.Action} or @{@link org.apache.isis.applib.annotation.ActionInteraction} annotations.
     */
    protected ActionDomainEvent<?> verify(final ActionDomainEvent<?> event) {
        return event;
    }

    /**
     * For testing only.
     */
    public Class<? extends ActionDomainEvent<?>> getEventType() {
        return eventType;
    }

    private static Object unwrap(final ObjectAdapter adapter) {
        return adapter == null ? null : adapter.getObject();
    }

    @Override
    protected String toStringValues() {
        return "method=" + method;
    }



    // /////////////////////////////////////////////////////////
    // Dependencies (looked up)
    // /////////////////////////////////////////////////////////

    private CommandContext getCommandContext() {
        return lookupService(CommandContext.class);
    }
    private InteractionContext getInteractionContext() {
        return lookupService(InteractionContext.class);
    }

    private QueryResultsCache getQueryResultsCache() {
        return lookupService(QueryResultsCache.class);
    }

    private CommandService getCommandService() {
        return lookupService(CommandService.class);
    }

    private ClockService getClockService() {
        return lookupService(ClockService.class);
    }

    private <T> T lookupService(final Class<T> serviceClass) {
        T service = lookupServiceIfAny(serviceClass);
        if(service == null) {
            throw new IllegalStateException("The '" + serviceClass.getName() + "' service is not registered!");
        }
        return service;
    }

    private <T> T lookupServiceIfAny(final Class<T> serviceClass) {
        return getServicesInjector().lookupService(serviceClass);
    }

    // /////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // /////////////////////////////////////////////////////////


    private AdapterManager getAdapterManager() {
        return adapterManager;
    }

    private ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    public DeploymentCategory getDeploymentCategory() {
        return deploymentCategory;
    }

    public AuthenticationSession getAuthenticationSession() {
        return authenticationSessionProvider.getAuthenticationSession();
    }

    public TransactionState getTransactionState() {
        return transactionStateProvider.getTransactionState();
    }
}
