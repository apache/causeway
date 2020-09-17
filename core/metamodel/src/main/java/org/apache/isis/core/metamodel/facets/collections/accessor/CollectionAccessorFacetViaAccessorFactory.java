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
import java.util.Collection;
import java.util.List;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.commons.MethodUtil;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.core.metamodel.facets.PropertyOrCollectionIdentifyingFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.collparam.semantics.CollectionSemanticsFacetDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class CollectionAccessorFacetViaAccessorFactory
extends PropertyOrCollectionIdentifyingFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.empty();

    public CollectionAccessorFacetViaAccessorFactory() {
        super(FeatureType.COLLECTIONS_ONLY, PREFIXES);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        attachAccessorFacetForAccessorMethod(processMethodContext);
    }

    private void attachAccessorFacetForAccessorMethod(final ProcessMethodContext processMethodContext) {
        final Method accessorMethod = processMethodContext.getMethod();
        processMethodContext.removeMethod(accessorMethod);

        final Class<?> cls = processMethodContext.getCls();
        final ObjectSpecification typeSpec = getSpecificationLoader().loadSpecification(cls);

        final FacetHolder holder = processMethodContext.getFacetHolder();
        super.addFacet(
                new CollectionAccessorFacetViaAccessor(
                        typeSpec, accessorMethod, holder));

        super.addFacet(CollectionSemanticsFacetDefault.forCollection(accessorMethod, holder));
    }


    // ///////////////////////////////////////////////////////////////
    // PropertyOrCollectionIdentifyingFacetFactory impl.
    // ///////////////////////////////////////////////////////////////

    @Override
    public boolean isPropertyOrCollectionAccessorCandidate(final Method method) {
        return method.getName().startsWith(MethodLiteralConstants.GET_PREFIX);
    }

    @Override
    public boolean isCollectionAccessor(final Method method) {
        if (!isPropertyOrCollectionAccessorCandidate(method)) {
            return false;
        }
        final Class<?> methodReturnType = method.getReturnType();
        return isCollectionOrArray(methodReturnType);
    }

    /**
     * The method way well represent a reference property, but this facet
     * factory does not have any opinion on the matter.
     */
    @Override
    public boolean isPropertyAccessor(final Method method) {
        return false;
    }

    @Override
    public void findAndRemoveCollectionAccessors(
            final MethodRemover methodRemover,
            final List<Method> methodListToAppendTo) {

        methodRemover.removeMethods(
                MethodUtil.Predicates.getter(Collection.class),
                methodListToAppendTo::add
                );
    }

    @Override
    public void findAndRemovePropertyAccessors(final MethodRemover methodRemover, final List<Method> methodListToAppendTo) {
        // does nothing
    }

}
