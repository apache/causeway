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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Bulk.InteractionContext.InvokedAs;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.interaction.Interaction;
import org.apache.isis.applib.services.interaction.InteractionContext;
import org.apache.isis.applib.services.interaction.spi.InteractionFactory;
import org.apache.isis.core.commons.lang.ThrowableExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.typeof.ElementSpecificationProviderFromTypeOfFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
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

    public ActionInvocationFacetViaMethod(
            final Method method, 
            final ObjectSpecification onType, 
            final ObjectSpecification returnType, 
            final FacetHolder holder, 
            final AdapterManager adapterManager, 
            final ServicesInjector servicesInjector) {
        super(holder);
        this.method = method;
        this.paramCount = method.getParameterTypes().length;
        this.onType = onType;
        this.returnType = returnType;
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
    public ObjectAdapter invoke(ObjectAction owningAction, ObjectAdapter target, ObjectAdapter[] arguments) {
        if (arguments.length != paramCount) {
            LOG.error(method + " requires " + paramCount + " parameters, not " + arguments.length);
        }

        final Bulk.InteractionContext bulkInteractionContext = getServicesInjector().lookupService(Bulk.InteractionContext.class);
        final InteractionContext interactionContext = getServicesInjector().lookupService(InteractionContext.class);
        final BookmarkService bookmarkService = getServicesInjector().lookupService(BookmarkService.class);

        try {
            final Object[] executionParameters = new Object[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                executionParameters[i] = unwrap(arguments[i]);
            }

            final Object object = unwrap(target);
            
            final BulkFacet bulkFacet = getFacetHolder().getFacet(BulkFacet.class);
            if (bulkFacet != null && 
                bulkInteractionContext != null &&
                bulkInteractionContext.getInvokedAs() == null) {
                bulkInteractionContext.setInvokedAs(InvokedAs.REGULAR);
                bulkInteractionContext.setDomainObjects(Collections.singletonList(object));
            }
            
            if(interactionContext != null) {
                Interaction interaction = interactionContext.getInteraction();
                if(owningAction != null) {

                    final String actionIdentifier = owningAction.getIdentifier().toClassAndNameIdentityString();
                    interaction.setActionIdentifier(actionIdentifier);
                    
                    String targetTitle = target.titleString(null);
                    String actionName = owningAction.getName();
                    interaction.setTargetClass(targetTitle);
                    interaction.setTargetAction(actionName);
                    
                    final StringBuilder argsBuf = new StringBuilder();
                    List<ObjectActionParameter> parameters = owningAction.getParameters();
                    if(parameters.size() == arguments.length) {
                        // should be the case
                        int i=0;
                        for (ObjectActionParameter param : parameters) {
                            appendParamArg(argsBuf, param, arguments[i++]);
                        }
                    }
                    interaction.setArguments(argsBuf.toString());
                }
                if(bookmarkService != null) {
                    final Bookmark bookmark = bookmarkService.bookmarkFor(target.getObject());
                    interaction.setTarget(bookmark);
                }
            }
            
            final Object result = method.invoke(object, executionParameters);

            if (LOG.isDebugEnabled()) {
                LOG.debug(" action result " + result);
            }
            if (result == null) {
                return null;
            }

            final ObjectAdapter resultAdapter = getAdapterManager().adapterFor(result);
            final TypeOfFacet typeOfFacet = getFacetHolder().getFacet(TypeOfFacet.class);
            resultAdapter.setElementSpecificationProvider(ElementSpecificationProviderFromTypeOfFacet.createFrom(typeOfFacet));
            
            PublishedActionFacet publishedActionFacet = getIdentified().getFacet(PublishedActionFacet.class);
            ActionInvocationFacet.currentInvocation.set(
                    publishedActionFacet != null
                        ? new CurrentInvocation(target, getIdentified(), arguments, resultAdapter)
                        :null);
            
            return resultAdapter;

        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final InvocationTargetException e) {
            if (e.getTargetException() instanceof IllegalStateException) {
                throw new ReflectiveActionException("IllegalStateException thrown while executing " + method + " " + e.getTargetException().getMessage(), e.getTargetException());
            } else {
                ThrowableExtensions.throwWithinIsisException(e, "Exception executing " + method);
                return null;
            }
        } catch (final IllegalAccessException e) {
            throw new ReflectiveActionException("Illegal access of " + method, e);
        }
    }

    private void appendParamArg(final StringBuilder buf, ObjectActionParameter param, ObjectAdapter objectAdapter) {
        String titleOf = objectAdapter != null? objectAdapter.titleString(null): "null";
        buf.append(param.getName()).append(": ").append(titleOf).append("\n");
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
