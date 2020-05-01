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

package org.apache.isis.core.metamodel.facets.param.autocomplete.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal._Constants;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacetAbstract;
import org.apache.isis.core.metamodel.facets.param.autocomplete.MinLengthUtil;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

public class ActionParameterAutoCompleteFacetViaMethod 
extends ActionParameterAutoCompleteFacetAbstract 
implements ImperativeFacet {

    private final Method method;
    private final Class<?> choicesType;
    private final int minLength;
    private final Optional<Constructor<?>> ppmFactory;

    public ActionParameterAutoCompleteFacetViaMethod(
            final Method method,
            final Class<?> choicesType,
            final Optional<Constructor<?>> ppmFactory, 
            final FacetHolder holder) {

        super(holder);
        this.method = method;
        this.choicesType = choicesType;
        this.minLength = MinLengthUtil.determineMinLength(method);
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
        return Intent.CHOICES_OR_AUTOCOMPLETE;
    }

    @Override
    public int getMinLength() {
        return minLength;
    }

    @Override
    public Object[] autoComplete(
            final ManagedObject owningAdapter,
            final Can<ManagedObject> pendingArgs,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final Object collectionOrArray = ppmFactory.isPresent()
                ? ManagedObject.InvokeUtil.invokeWithPPM(
                        ppmFactory.get(), method, owningAdapter, pendingArgs, Collections.singletonList(searchArg))
                : ManagedObject.InvokeUtil.invokeAutofit(
                        method, owningAdapter, pendingArgs, Collections.singletonList(searchArg));
        
        if (collectionOrArray == null) {
            return _Constants.emptyObjects;
        }
        val collectionAdapter = getObjectManager().adapt(collectionOrArray);

        val visiblePojos = ManagedObject.VisibilityUtil
                .visiblePojosAsArray(collectionAdapter, interactionInitiatedBy);
        
        return visiblePojos;
    }

    @Override
    protected String toStringValues() {
        return "method=" + method + ",type=" + choicesType;
    }


    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        ImperativeFacet.Util.appendAttributesTo(this, attributeMap);
        attributeMap.put("choicesType", choicesType);
        attributeMap.put("minLength", minLength);
    }

}
