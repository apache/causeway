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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Bulk.InteractionContext;
import org.apache.isis.applib.annotation.Bulk.InteractionContext.InvokedAs;
import org.apache.isis.core.commons.lang.ThrowableExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.typeof.ElementSpecificationProviderFromTypeOfFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.ReflectiveActionException;
import org.apache.isis.core.progmodel.facets.actions.bulk.BulkFacet;

public class ActionInvocationFacetViaMethod extends ActionInvocationFacetAbstract implements ImperativeFacet {

    private final static Logger LOG = LoggerFactory.getLogger(ActionInvocationFacetViaMethod.class);

    private final Method method;
    private final int paramCount;
    private final ObjectSpecification onType;
    private final ObjectSpecification returnType;

    private final AdapterManager adapterManager;

    public ActionInvocationFacetViaMethod(final Method method, final ObjectSpecification onType, final ObjectSpecification returnType, final FacetHolder holder, final AdapterManager adapterManager) {
        super(holder);
        this.method = method;
        this.paramCount = method.getParameterTypes().length;
        this.onType = onType;
        this.returnType = returnType;
        this.adapterManager = adapterManager;
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
    public ObjectAdapter invoke(final ObjectAdapter inObject, final ObjectAdapter[] parameters) {
        if (parameters.length != paramCount) {
            LOG.error(method + " requires " + paramCount + " parameters, not " + parameters.length);
        }

        try {
            final Object[] executionParameters = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                executionParameters[i] = unwrap(parameters[i]);
            }

            final Object object = unwrap(inObject);
            
            final Object result;
            final BulkFacet bulkFacet = getFacetHolder().getFacet(BulkFacet.class);
            if(bulkFacet != null) {
                Bulk.InteractionContext bulkInteractionContext = Bulk.InteractionContext.current.get();
                if (bulkInteractionContext != null && bulkInteractionContext.getInvokedAs() == null) {
                    bulkInteractionContext.setInvokedAs(InvokedAs.REGULAR);
                    bulkInteractionContext.setDomainObjects(Collections.singletonList(object));
                }
            }
            result = method.invoke(object, executionParameters);
            
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
                        ? new CurrentInvocation(inObject, getIdentified(), parameters, resultAdapter)
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

}
