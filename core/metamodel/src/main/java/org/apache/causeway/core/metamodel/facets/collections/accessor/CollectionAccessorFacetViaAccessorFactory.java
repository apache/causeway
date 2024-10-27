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

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.semantics.AccessorSemantics;
import org.apache.causeway.core.metamodel.commons.MethodUtil;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover;
import org.apache.causeway.core.metamodel.facets.PropertyOrCollectionIdentifyingFacetFactoryAbstract;

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
        var accessorMethod = processMethodContext.getMethod().asMethodElseFail(); // no-arg method, should have a regular facade
        processMethodContext.removeMethod(accessorMethod);

        var cls = processMethodContext.getCls();
        var typeSpec = getSpecificationLoader().loadSpecification(cls);
        var facetHolder = processMethodContext.getFacetHolder();

        addFacet(
                new CollectionAccessorFacetViaAccessor(
                        typeSpec, accessorMethod, facetHolder));
    }

    // ///////////////////////////////////////////////////////////////
    // PropertyOrCollectionIdentifyingFacetFactory impl.
    // ///////////////////////////////////////////////////////////////

    @Override
    public boolean isPropertyOrCollectionGetterCandidate(final ResolvedMethod method) {
        return AccessorSemantics.GET.isPrefixOf(method.name());
    }

    @Override
    public boolean isCollectionAccessor(final ResolvedMethod method) {
        if (!isPropertyOrCollectionGetterCandidate(method)) {
            return false;
        }
        final Class<?> methodReturnType = method.returnType();
        return isNonScalar(methodReturnType);
    }

    /**
     * The method way well represent a reference property, but this facet
     * factory does not have any opinion on the matter.
     */
    @Override
    public boolean isPropertyAccessor(final ResolvedMethod method) {
        return false;
    }

    @Override
    public void findAndRemoveCollectionAccessors(
            final MethodRemover methodRemover,
            final List<ResolvedMethod> methodListToAppendTo) {
        methodRemover.removeMethods(
                MethodUtil.Predicates.supportedNonScalarMethodReturnType(),
                methodListToAppendTo::add);
    }

    @Override
    public void findAndRemovePropertyAccessors(
            final MethodRemover methodRemover, final List<ResolvedMethod> methodListToAppendTo) {
        // does nothing
    }

}
