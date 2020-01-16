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

package org.apache.isis.core.metamodel.facets.properties.autocomplete.method;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.MinLengthUtil;
import org.apache.isis.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

public class PropertyAutoCompleteFacetMethod extends PropertyAutoCompleteFacetAbstract implements ImperativeFacet {

    private final Method method;
    private final Class<?> choicesClass;
    private final int minLength;

    public PropertyAutoCompleteFacetMethod(
            final Method method,
            final Class<?> choicesClass,
            final FacetHolder holder) {
        super(holder);
        this.method = method;
        this.choicesClass = choicesClass;
        this.minLength = MinLengthUtil.determineMinLength(method);
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
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final Object collectionOrArray = ManagedObject.InvokeUtil.invoke(method, owningAdapter, searchArg);
        if (collectionOrArray == null) {
            return null;
        }

        val collectionAdapter = getObjectManager().adapt(collectionOrArray);
        
        val visiblePojos = ManagedObject.VisibilityUtil
                .visiblePojosAsArray(collectionAdapter, interactionInitiatedBy);
        
        return visiblePojos;
    }

    @Override
    protected String toStringValues() {
        return "method=" + method + ",class=" + choicesClass;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        ImperativeFacet.Util.appendAttributesTo(this, attributeMap);
        attributeMap.put("choicesType", choicesClass);
        attributeMap.put("minLength", minLength);
    }

}
