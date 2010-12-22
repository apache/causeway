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


package org.apache.isis.core.progmodel.facets.actions.defaults;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.AdapterInvokeUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.progmodel.facets.actions.invoke.ActionInvocationFacetViaMethod;


public class ActionDefaultsFacetViaMethod extends ActionDefaultsFacetAbstract implements ImperativeFacet {

    private final Method method;
    
    @SuppressWarnings("unused")
	private final Method actionMethod;

    public ActionDefaultsFacetViaMethod(
    		final Method method, 
    		final FacetHolder holder) {
        super(holder, false);
        this.method = method;
        final Facet actionInvocationFacet = holder.getFacet(ActionInvocationFacet.class);
        if (actionInvocationFacet instanceof ActionInvocationFacetViaMethod) {
            final ActionInvocationFacetViaMethod facetViaMethod = (ActionInvocationFacetViaMethod) actionInvocationFacet;
            actionMethod = facetViaMethod.getMethods().get(0);
        } else {
            actionMethod = null;
        }
    }

    /**
     * Returns a singleton list of the {@link Method} provided in the constructor. 
     */
    public List<Method> getMethods() {
    	return Collections.singletonList(method);
    }

    public boolean impliesResolve() {
    	return true;
    }
    
    public boolean impliesObjectChanged() {
    	return false;
    }
    

    public Object[] getDefaults(final ObjectAdapter owningAdapter) {
        final Object[] defaults = (Object[]) AdapterInvokeUtils.invoke(method, owningAdapter);
        return defaults;

        // TODO fix the setting up actionMethod so that we can check the types.

        /*
         * ObjectAdapter[] array = new ObjectAdapter[defaults.length]; // if (actionMethod != null) {
         * ObjectReflector reflector = IsisContext.getReflector(); // Class[] parameterTypes =
         * actionMethod.getParameterTypes(); for (int i = 0; i < array.length; i++) { // Class parameterType =
         * parameterTypes[i]; // ObjectSpecification paramSpecification =
         * reflector.loadSpecification(parameterType); // if (paramSpecification.isObject()) { array[i] =
         * PersistorUtil.createAdapter(defaults[i]); / } else { throw new
         * UnknownTypeException(paramSpecification.getFullName()); } / } //} return (ObjectAdapter[]) array;
         */
    }

    @Override
    protected String toStringValues() {
        return "method=" + method;
    }


}

