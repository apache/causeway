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

package org.apache.isis.core.progmodel.facets.actions.invoke;

import java.awt.Desktop.Action;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Bulk.InteractionContext.InvokedAs;
import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.background.BackgroundService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.memento.MementoService;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ThrowableExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.typeof.ElementSpecificationProviderFromTypeOfFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.ReflectiveActionException;
import org.apache.isis.core.progmodel.facets.actions.bulk.BulkFacet;

public class ActionInvocationFacetViaMethod extends ActionInvocationFacetAbstract implements ImperativeFacet {

    private final static Logger LOG = LoggerFactory.getLogger(ActionInvocationFacetViaMethod.class);

    private final Method method;
    private final int paramCount;
    private final ObjectSpecification onType;
    private final ObjectSpecification returnType;

    private final AdapterManager adapterManager;
    private final ServicesInjector servicesInjector;
    private final RuntimeContext runtimeContext;
    
    public ActionInvocationFacetViaMethod(
            final Method method, 
            final ObjectSpecification onType, 
            final ObjectSpecification returnType, 
            final FacetHolder holder, 
            final RuntimeContext runtimeContext, 
            final AdapterManager adapterManager, 
            final ServicesInjector servicesInjector) {
        super(holder);
        this.method = method;
        this.paramCount = method.getParameterTypes().length;
        this.onType = onType;
        this.returnType = returnType;
        this.runtimeContext = runtimeContext;
        this.adapterManager = adapterManager;
        this.servicesInjector = servicesInjector;
    }

    /**
     * Returns a singleton list of the {@link Method} provided in the
     * constructor.
     */
    @Override
    public List<Method> getMethods() {
        return Collections.singletonList(method);
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
    public ObjectAdapter invoke(final ObjectAdapter target, final ObjectAdapter[] parameters) {
        return invoke(null, target, parameters);
    }

    @Override
    public ObjectAdapter invoke(ObjectAction owningAction, ObjectAdapter targetAdapter, ObjectAdapter[] arguments) {
        if (arguments.length != paramCount) {
            LOG.error(method + " requires " + paramCount + " parameters, not " + arguments.length);
        }

        final Bulk.InteractionContext bulkInteractionContext = getServicesInjector().lookupService(Bulk.InteractionContext.class);
        final CommandContext commandContext = getServicesInjector().lookupService(CommandContext.class);
        final Command command = commandContext != null ? commandContext.getCommand() : null;
        
        try {
            final Object[] executionParameters = new Object[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                executionParameters[i] = unwrap(arguments[i]);
            }

            final Object object = unwrap(targetAdapter);
            
            final BulkFacet bulkFacet = getFacetHolder().getFacet(BulkFacet.class);
            if (bulkFacet != null && 
                bulkInteractionContext != null &&
                bulkInteractionContext.getInvokedAs() == null) {
                
                bulkInteractionContext.setInvokedAs(InvokedAs.REGULAR);
                bulkInteractionContext.setDomainObjects(Collections.singletonList(object));
            }


            if(command != null && command.getNature() == Command.Nature.USER_INITIATED && owningAction != null) {

                command.setStartedAt(command.getTimestamp());
                command.setActionIdentifier(CommandUtil.actionIdentifierFor(owningAction));
                command.setTargetClass(CommandUtil.targetClassNameFor(targetAdapter));
                command.setTargetAction(CommandUtil.targetActionNameFor(owningAction));
                command.setArguments(CommandUtil.argDescriptionFor(owningAction, arguments));
                
                final Bookmark targetBookmark = CommandUtil.bookmarkFor(targetAdapter);
                command.setTarget(targetBookmark);
                
                
                final BackgroundService backgroundService = getServicesInjector().lookupService(BackgroundService.class);
                if(backgroundService != null) {
                    final Object targetObject = unwrap(targetAdapter);
                    final Object[] args = CommandUtil.objectsFor(arguments);
                    ActionInvocationMemento aim = backgroundService.asActionInvocationMemento(method, targetObject, args);

                    if(aim != null) {
                        command.setMemento(aim.asMementoString());
                    } else {
                        throw new IsisException("Unable to build memento for action " + owningAction.getIdentifier().toClassAndNameIdentityString());
                    }
                }

                final boolean hasCommandFacet = getFacetHolder().containsDoOpFacet(CommandFacet.class);
                command.setPersistHint(hasCommandFacet);
            }
            
            final Object result = method.invoke(object, executionParameters);

            if (LOG.isDebugEnabled()) {
                LOG.debug(" action result " + result);
            }
            if (result == null) {
                return null;
            }

            final ObjectAdapter resultAdapter = getAdapterManager().adapterFor(result);

            // copy over TypeOfFacet if required
            final TypeOfFacet typeOfFacet = getFacetHolder().getFacet(TypeOfFacet.class);
            resultAdapter.setElementSpecificationProvider(ElementSpecificationProviderFromTypeOfFacet.createFrom(typeOfFacet));

            
            if(command != null) {
                if(!resultAdapter.getSpecification().containsDoOpFacet(ViewModelFacet.class)) {
                    final Bookmark bookmark = CommandUtil.bookmarkFor(resultAdapter);
                    command.setResult(bookmark);
                }
            }
            
            PublishedActionFacet publishedActionFacet = getIdentified().getFacet(PublishedActionFacet.class);
            ActionInvocationFacet.currentInvocation.set(
                    publishedActionFacet != null
                        ? new CurrentInvocation(targetAdapter, getIdentified(), arguments, resultAdapter)
                        :null);
            
            return resultAdapter;

        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            if (targetException instanceof IllegalStateException) {
                throw new ReflectiveActionException("IllegalStateException thrown while executing " + method + " " + targetException.getMessage(), targetException);
            } 
            if(targetException instanceof RecoverableException) {
                if (!runtimeContext.getTransactionState().canCommit()) {
                    // something severe has happened to the underlying transaction;
                    // so escalate this exception to be non-recoverable
                    final Throwable targetExceptionCause = targetException.getCause();
                    Throwable nonRecoverableCause = targetExceptionCause != null? targetExceptionCause: targetException;
                    throw new NonRecoverableException(nonRecoverableCause);
                }
            }

            ThrowableExtensions.throwWithinIsisException(e, "Exception executing " + method);
            return null;
        } catch (final IllegalAccessException e) {
            throw new ReflectiveActionException("Illegal access of " + method, e);
        }
    }

    private static Object unwrap(final ObjectAdapter adapter) {
        return adapter == null ? null : adapter.getObject();
    }

    @Override
    public boolean impliesResolve() {
        return true;
    }

    @Override
    public boolean impliesObjectChanged() {
        return false;
    }

    @Override
    protected String toStringValues() {
        return "method=" + method;
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

    
}
