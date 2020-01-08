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

package org.apache.isis.metamodel.facets.param.choices.method;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.CollectionUtils;
import org.apache.isis.metamodel.facets.ImperativeFacet;
import org.apache.isis.metamodel.facets.param.choices.ActionChoicesFacetAbstract;
import org.apache.isis.metamodel.spec.DomainModelException;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.val;

public class ActionChoicesFacetViaMethod extends ActionChoicesFacetAbstract implements ImperativeFacet {

    private final Method method;
    private final Class<?> choicesType;

    public ActionChoicesFacetViaMethod(
            final Method method,
            final Class<?> choicesType,
            final FacetHolder holder) {

        super(holder);
        this.method = method;
        this.choicesType = choicesType;
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
    public Object[][] getChoices(
            final ManagedObject owningAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final Object objectOrCollection = ManagedObject.InvokeUtil.invoke(method, owningAdapter);
        if (!(objectOrCollection instanceof Object[])) {
            throw new DomainModelException(String.format(
                    "Expected an array of collections (Object[]) containing choices for all parameters, "
                            + "but got %s instead. Perhaps the parameter number is missing?",
                            objectOrCollection));
        }
        final Object[] options = (Object[]) objectOrCollection;
        final Object[][] results = new Object[options.length][];

        val parameterTypes = method.getParameterTypes();
        
        for (int i = 0; i < results.length; i++) {
            results[i] = handleResults(options[i], parameterTypes[i], interactionInitiatedBy);
        }
        return results;
    }

    private Object[] handleResults(
            final Object collectionOrArray,
            final Class<?> parameterType,
            final InteractionInitiatedBy interactionInitiatedBy) {
        if (collectionOrArray == null) {
            return null;
        }

        final ManagedObject collectionAdapter = getObjectManager().adapt(collectionOrArray);

        final List<ManagedObject> visibleAdapters =
                ManagedObject.VisibilityUtil.visibleAdapters(
                        collectionAdapter,
                        interactionInitiatedBy);
        final List<Object> filteredObjects =
                _Lists.map(visibleAdapters, ManagedObject::unwrapSingle);

        final ObjectSpecification parameterSpec = getSpecification(parameterType);
        return CollectionUtils.getCollectionAsObjectArray(filteredObjects, parameterSpec, getObjectManager());
    }

    @Override
    protected String toStringValues() {
        return "method=" + method + ",type=" + choicesType;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        ImperativeFacet.Util.appendAttributesTo(this, attributeMap);
        attributeMap.put("choicesType", choicesType);
    }
}
