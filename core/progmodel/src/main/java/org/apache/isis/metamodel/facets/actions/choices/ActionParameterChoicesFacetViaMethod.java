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


package org.apache.isis.metamodel.facets.actions.choices;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.commons.lang.ArrayUtils;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.java5.ImperativeFacet;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.util.ObjectAdapterUtils;
import org.apache.isis.metamodel.util.ObjectInvokeUtils;



public class ActionParameterChoicesFacetViaMethod extends ActionParameterChoicesFacetAbstract implements ImperativeFacet {

    private final Method method;
    private final Class<?> choicesType;
	private final SpecificationLoader specificationLoader;
	private final RuntimeContext runtimeContext;

    public ActionParameterChoicesFacetViaMethod(
    		final Method method, 
    		final Class<?> choicesType, 
    		final FacetHolder holder, 
    		final SpecificationLoader specificationLoader, 
    		final RuntimeContext runtimeContext) {
        super(holder);
        this.method = method;
        this.choicesType = choicesType;
        this.specificationLoader = specificationLoader;
        this.runtimeContext = runtimeContext;
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
	

    public Object[] getChoices(final ObjectAdapter owningAdapter) {
        final Object options = ObjectInvokeUtils.invoke(method, owningAdapter);
        if (options == null) {
            return new Object[0];
        }
        if (options.getClass().isArray()) {
            return ArrayUtils.getObjectAsObjectArray(options);
        }
        else {
            final ObjectSpecification specification = getSpecificationLoader().loadSpecification(choicesType);
            return ObjectAdapterUtils.getCollectionAsObjectArray(options, specification, getRuntimeContext());
        }
    }

	@Override
    protected String toStringValues() {
        return "method=" + method + ",type=" + choicesType;
    }


	
	////////////////////////////////////////////////////////////////////
	// Dependencies (from constructor)
	////////////////////////////////////////////////////////////////////

    private RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}

	private SpecificationLoader getSpecificationLoader() {
		return specificationLoader;
	}

}



