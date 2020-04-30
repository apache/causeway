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

package org.apache.isis.core.metamodel.facets.param.defaults.methodnum;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

public class ActionParameterDefaultsFacetViaMethod extends ActionParameterDefaultsFacetAbstract implements ImperativeFacet {

    private final Method method;
    private final int paramNum;
    private final Optional<Constructor<?>> ppmFactory;

    /**
     *
     * @param method
     * @param paramNum - which parameter this facet relates to.
     * @param holder
     * @param adapterManager
     */
    public ActionParameterDefaultsFacetViaMethod(
            final Method method,
            final int paramNum,
            final Optional<Constructor<?>> ppmFactory,
            final FacetHolder holder) {

        super(holder);
        this.method = method;
        this.paramNum = paramNum;
        this.ppmFactory = ppmFactory;
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
    public Intent getIntent(final Method method) {
        return Intent.DEFAULTS;
    }

    @Override
    public Object getDefault(
            final ManagedObject target,
            final Can<ManagedObject> pendingArgs,
            final Integer paramNumUpdated) {

        if(ppmFactory.isPresent()) {
            
            if(_NullSafe.isEmpty(pendingArgs)) {
                return pendingArgs.get(paramNum)
                        .map(ManagedObject::getPojo)
                        .orElse(null) ;
            }
            
            return ManagedObject.InvokeUtil.invokeWithPPM(ppmFactory.get(), method, target, pendingArgs);
        }
        
        // this isn't a dependent defaults situation, so just evaluate the default.
        if (_NullSafe.isEmpty(pendingArgs) || paramNumUpdated == null) {
            return ManagedObject.InvokeUtil.invokeAutofit(method, target, pendingArgs);
        }

        // this could be a dependent defaults situation, but has a previous parameter been updated
        // that this parameter is dependent upon?
        final int numParams = method.getParameterCount();
        if (paramNumUpdated < numParams) {
            // in this case the parameter that was updated is previous
            //
            // eg, suppose the method is default2Foo(int, int), and the second param is updated... we want to re-evaluate
            // so numParams == 2, and paramNumUpdated == 1, and (paramNumUpdated < numParams) is TRUE
            //
            // conversely, if method default2Foo(int), and the second param is updated... we don't want to re-evaluate
            // so numParams == 1, and paramNumUpdated == 1, and (paramNumUpdated < numParams) is FALSE
            //
            return ManagedObject.InvokeUtil.invokeAutofit(method, target, pendingArgs);
        }

        // otherwise, just return the arguments that are already known; we don't want to recompute the default
        // because if we did then this would trample over any pending changes already made by the end-user.
        val argPojo = pendingArgs.stream()
                .skip(paramNum)
                .findFirst()
                .map(ManagedObject::getPojo)
                .orElse(null) ;
                
        return argPojo;
    }

    @Override
    protected String toStringValues() {
        return "method=" + method;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        ImperativeFacet.Util.appendAttributesTo(this, attributeMap);
    }

}
