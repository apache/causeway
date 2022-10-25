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
package org.apache.causeway.core.metamodel.facets.properties.accessor;

import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.commons.MethodUtil;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover;
import org.apache.causeway.core.metamodel.facets.PropertyOrCollectionIdentifyingFacetFactoryAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

public class PropertyAccessorFacetViaAccessorFactory
extends PropertyOrCollectionIdentifyingFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.empty();

    @Inject
    public PropertyAccessorFacetViaAccessorFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY, PREFIXES);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        attachPropertyAccessFacetForAccessorMethod(processMethodContext);
    }

    private void attachPropertyAccessFacetForAccessorMethod(final ProcessMethodContext processMethodContext) {
        final Method accessorMethod = processMethodContext.getMethod();
        processMethodContext.removeMethod(accessorMethod);

        final Class<?> cls = processMethodContext.getCls();
        final ObjectSpecification typeSpec = getSpecificationLoader().loadSpecification(cls);

        final FacetHolder property = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(
                new PropertyAccessorFacetViaAccessor(typeSpec, accessorMethod, property));
    }

    // ///////////////////////////////////////////////////////
    // PropertyOrCollectionIdentifying
    // ///////////////////////////////////////////////////////

    @Override
    public boolean isPropertyOrCollectionGetterCandidate(final Method method) {
        return ProgrammingModelConstants.AccessorPrefix.isGetter(method);
    }

    /**
     * The method way well represent a collection, but this facet factory does
     * not have any opinion on the matter.
     */
    @Override
    public boolean isCollectionAccessor(final Method method) {
        return false;
    }

    @Override
    public boolean isPropertyAccessor(final Method method) {
        if (!isPropertyOrCollectionGetterCandidate(method)) {
            return false;
        }
        return isNonScalar(method.getReturnType());
    }

    @Override
    public void findAndRemovePropertyAccessors(final MethodRemover methodRemover, final List<Method> methodListToAppendTo) {
        methodRemover.removeMethods(MethodUtil.Predicates.booleanGetter(), methodListToAppendTo::add);
        methodRemover.removeMethods(MethodUtil.Predicates.nonBooleanGetter(Object.class), methodListToAppendTo::add);
    }

    @Override
    public void findAndRemoveCollectionAccessors(final MethodRemover methodRemover, final List<Method> methodListToAppendTo) {
        // does nothing
    }

}
