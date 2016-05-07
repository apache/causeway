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
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
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
import org.apache.isis.core.metamodel.services.ixn.InteractionDtoServiceInternal;
import org.apache.isis.core.metamodel.services.publishing.PublishingServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.ReflectiveActionException;
import org.apache.isis.core.metamodel.transactions.TransactionState;
import org.apache.isis.core.metamodel.transactions.TransactionStateProvider;
import org.apache.isis.schema.ixn.v1.ActionInvocationDto;

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


    @Override
    public ObjectAdapter invoke(
            final ObjectAction owningAction,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter mixedInAdapter,
            final ObjectAdapter[] argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        // similar code in PropertySetterOrClearFacetFDEA

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();


        final InteractionContext interactionContext = getInteractionContext();
        final Interaction interaction = interactionContext.getInteraction();

        final String actionId = owningAction.getIdentifier().toClassAndNameIdentityString();

        final ObjectAdapter returnedAdapter;
        if( command.getExecutor() == Command.Executor.USER &&
            command.getExecuteIn() == org.apache.isis.applib.annotation.Command.ExecuteIn.BACKGROUND) {

            // deal with background commands

            // persist command so can it can subsequently be invoked in the 'background'
            final CommandService commandService = getCommandService();
            if (!commandService.persistIfPossible(command)) {
                throw new IsisException(String.format(
                        "Unable to persist command for action '%s'; CommandService does not support persistent commands ",
                        actionId));
            }
            returnedAdapter = getAdapterManager().adapterFor(command);

        } else {

            // otherwise, go ahead and execute action in the 'foreground'
            owningAction.setupActionInvocationContext(targetAdapter);

            final Object targetPojo = ObjectAdapter.Util.unwrap(targetAdapter);
            final List<ObjectAdapter> argumentAdapterList = Arrays.asList(argumentAdapters);
            final List<Object> argumentPojos = ObjectAdapter.Util.unwrap(argumentAdapterList);

            final Interaction.ActionInvocation execution =
                    new Interaction.ActionInvocation(interaction, actionId, targetPojo, argumentPojos);
            final Interaction.MemberExecutor<Interaction.ActionInvocation> callable =
                    new Interaction.MemberExecutor<Interaction.ActionInvocation>() {

                @Override
                public Object execute(final Interaction.ActionInvocation currentExecution) {

                    try {

                        // update the current execution with the DTO (memento)
                        final ActionInvocationDto invocationDto =
                                getInteractionDtoServiceInternal().asActionInvocationDto(
                                        owningAction, targetAdapter, argumentAdapterList);
                        currentExecution.setDto(invocationDto);

                        // ... post the executing event
                        final ActionDomainEvent<?> event =
                                domainEventHelper.postEventForAction(
                                        AbstractDomainEvent.Phase.EXECUTING,
                                        eventType, null,
                                        owningAction, owningAction,
                                        targetAdapter, mixedInAdapter, argumentAdapters,
                                        command,
                                        null);

                        // set event onto the execution
                        currentExecution.setEvent(event);

                        // invoke method
                        final Object resultPojo = invokeMethodElseFromCache(targetAdapter, argumentAdapters);

                        final ObjectAdapter resultAdapterPossiblyCloned = cloneIfViewModelCloneable(resultPojo, targetAdapter);


                        // ... post the executed event
                        domainEventHelper.postEventForAction(
                                AbstractDomainEvent.Phase.EXECUTED,
                                eventType, verify(event),
                                owningAction, owningAction, targetAdapter, mixedInAdapter, argumentAdapters,
                                command,
                                resultAdapterPossiblyCloned);

                        return ObjectAdapter.Util.unwrap(resultAdapterPossiblyCloned);

                    } catch (IllegalAccessException ex) {
                        throw new ReflectiveActionException("Illegal access of " + method, ex);
                    } catch (InvocationTargetException ex) {

                        final Throwable targetException = ex.getTargetException();
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
                                Throwable nonRecoverableCause = targetExceptionCause != null
                                        ? targetExceptionCause
                                        : targetException;

                                // trim to first 300 chars
                                final String message = trim(nonRecoverableCause.getMessage(), 300);

                                throw new NonRecoverableException(message, nonRecoverableCause);
                            }
                        }

                        ThrowableExtensions.throwWithinIsisException(ex, "Exception executing " + method);
                        return null; // never executed, previous line throws
                    }
                }
            };

            // sets up startedAt and completedAt on the execution, also manages the execution call graph
            interaction.execute(callable, execution, getClockService(), command);

            // handle any exceptions
            final Interaction.Execution<ActionInvocationDto, ?> priorExecution = interaction.getPriorExecution();

            final Exception executionExceptionIfAny = priorExecution.getThrew();

            // TODO: should also sync DTO's threw here...

            if(executionExceptionIfAny != null) {
                throw executionExceptionIfAny instanceof RuntimeException
                        ? ((RuntimeException)executionExceptionIfAny)
                        : new RuntimeException(executionExceptionIfAny);
            }


            final Object returnedPojo = priorExecution.getReturned();
            returnedAdapter = adapterManager.adapterFor(returnedPojo);

            // sync DTO with result
            getInteractionDtoServiceInternal().updateResult(priorExecution.getDto(), owningAction, returnedPojo);


            // update Command (if required)
            setCommandResultIfEntity(command, returnedAdapter);

            final PublishedActionFacet publishedActionFacet = getIdentified().getFacet(PublishedActionFacet.class);
            if (publishedActionFacet != null) {

                final IdentifiedHolder identifiedHolder = getIdentified();
                final List<ObjectAdapter> parameterAdapters = Arrays.asList(argumentAdapters);

                getPublishingServiceInternal().publishAction(
                        priorExecution,
                        owningAction, identifiedHolder,
                        targetAdapter, parameterAdapters,
                        returnedAdapter);
            }
        }


        return filteredIfRequired(returnedAdapter, interactionInitiatedBy);
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

    protected ObjectAdapter filteredIfRequired(
            final ObjectAdapter resultAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        if (resultAdapter == null) {
            return null;
        }

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

    private PublishingServiceInternal getPublishingServiceInternal() {
        return lookupService(PublishingServiceInternal.class);
    }

    private InteractionDtoServiceInternal getInteractionDtoServiceInternal() {
        return lookupService(InteractionDtoServiceInternal.class);
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
