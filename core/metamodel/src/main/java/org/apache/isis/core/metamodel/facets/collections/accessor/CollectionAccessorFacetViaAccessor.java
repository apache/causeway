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

package org.apache.isis.core.metamodel.facets.collections.accessor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.CollectionUtils;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

public class CollectionAccessorFacetViaAccessor
extends PropertyOrCollectionAccessorFacetAbstract
implements ImperativeFacet {

    private final Method method;

    public CollectionAccessorFacetViaAccessor(
            final ObjectSpecification typeSpec,
            final Method method,
            final FacetHolder holder) {
        super(typeSpec, holder);
        this.method = method;
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
        return Intent.ACCESSOR;
    }

    @Override
    public Object getProperty(
            final ManagedObject owningAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final Object collectionOrArray = ManagedObject.InvokeUtil.invoke(method, owningAdapter);
        if(collectionOrArray == null) {
            return null;
        }

        val collectionAdapter = getObjectManager().adapt(collectionOrArray);

        final boolean filterForVisibility = getConfiguration().getCore().getMetaModel().isFilterVisibility();
        if(filterForVisibility) {
            final List<ManagedObject> visibleAdapters =
                    ManagedObject.VisibilityUtil.visibleAdapters(
                            collectionAdapter,
                            interactionInitiatedBy);
            final Object visibleObjects =
                    CollectionUtils.copyOf(
                            _Lists.map(visibleAdapters, ManagedObject::unwrapSingle),
                            method.getReturnType());
            if (visibleObjects != null) {
                return visibleObjects;
            }
            // would be null if unable to take a copy (unrecognized return type)
            // fallback to returning the original adapter, without filtering for visibility
        }

        // either no filtering, or was unable to filter (unable to take copy due to unrecognized type)
        return collectionOrArray;
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
