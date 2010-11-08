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

package org.apache.isis.metamodel.facets.actions.invoke;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.util.InvokeUtils;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.java5.ImperativeFacet;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.specloader.ReflectiveActionException;
import org.apache.log4j.Logger;

public class ActionInvocationFacetViaMethod extends ActionInvocationFacetAbstract implements ImperativeFacet {

    private final static Logger LOG = Logger.getLogger(ActionInvocationFacetViaMethod.class);

    private final Method method;
    private final int paramCount;
    private final ObjectSpecification onType;
    private final ObjectSpecification returnType;

    private final RuntimeContext runtimeContext;

    public ActionInvocationFacetViaMethod(final Method method, final ObjectSpecification onType,
        final ObjectSpecification returnType, final FacetHolder holder, final RuntimeContext runtimeContext) {
        super(holder);
        this.method = method;
        this.paramCount = method.getParameterTypes().length;
        this.onType = onType;
        this.returnType = returnType;
        this.runtimeContext = runtimeContext;
    }

    /**
     * Returns a singleton list of the {@link Method} provided in the constructor.
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
            final Object result = method.invoke(object, executionParameters);
            LOG.debug(" action result " + result);
            if (result == null) {
                return null;
            }

            final ObjectAdapter adapter = getRuntimeContext().adapterFor(result);
            final TypeOfFacet typeOfFacet = getFacetHolder().getFacet(TypeOfFacet.class);
            adapter.setTypeOfFacet(typeOfFacet);
            return adapter;

        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final InvocationTargetException e) {
            if (e.getTargetException() instanceof IllegalStateException) {
                throw new ReflectiveActionException("IllegalStateException thrown while executing " + method + " "
                    + e.getTargetException().getMessage(), e.getTargetException());
            } else {
                InvokeUtils.invocationException("Exception executing " + method, e);
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

    private RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

}
