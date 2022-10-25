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
package org.apache.causeway.core.metamodel.facets.collections.accessor;

import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.commons.MethodUtil;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover;
import org.apache.causeway.core.metamodel.facets.PropertyOrCollectionIdentifyingFacetFactoryAbstract;

import lombok.val;

public class CollectionAccessorFacetViaAccessorFactory
extends PropertyOrCollectionIdentifyingFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.empty();

    @Inject
    public CollectionAccessorFacetViaAccessorFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.COLLECTIONS_ONLY, PREFIXES);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        attachAccessorFacetForAccessorMethod(processMethodContext);
    }

    private void attachAccessorFacetForAccessorMethod(final ProcessMethodContext processMethodContext) {
        final Method accessorMethod = processMethodContext.getMethod();
        processMethodContext.removeMethod(accessorMethod);

        val cls = processMethodContext.getCls();
        val typeSpec = getSpecificationLoader().loadSpecification(cls);
        val facetHolder = processMethodContext.getFacetHolder();

        addFacet(
                new CollectionAccessorFacetViaAccessor(
                        typeSpec, accessorMethod, facetHolder));
    }


    // ///////////////////////////////////////////////////////////////
    // PropertyOrCollectionIdentifyingFacetFactory impl.
    // ///////////////////////////////////////////////////////////////

    @Override
    public boolean isPropertyOrCollectionGetterCandidate(final Method method) {
        return ProgrammingModelConstants.AccessorPrefix.GET.isPrefixOf(method.getName());
    }

    @Override
    public boolean isCollectionAccessor(final Method method) {
        if (!isPropertyOrCollectionGetterCandidate(method)) {
            return false;
        }
        final Class<?> methodReturnType = method.getReturnType();
        return isNonScalar(methodReturnType);
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
                MethodUtil.Predicates.supportedNonScalarMethodReturnType(),
                methodListToAppendTo::add);
    }

    @Override
    public void findAndRemovePropertyAccessors(
            final MethodRemover methodRemover, final List<Method> methodListToAppendTo) {
        // does nothing
    }

}
