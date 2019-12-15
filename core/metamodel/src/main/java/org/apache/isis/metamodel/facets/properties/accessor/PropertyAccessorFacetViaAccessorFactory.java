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

package org.apache.isis.metamodel.facets.properties.accessor;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.commons.CanBeVoid;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MethodRemover;
import org.apache.isis.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.metamodel.facets.PropertyOrCollectionIdentifyingFacetFactoryAbstract;
import org.apache.isis.metamodel.spec.ObjectSpecification;

public class PropertyAccessorFacetViaAccessorFactory extends PropertyOrCollectionIdentifyingFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.ofCollection(_Lists.of(
            MethodLiteralConstants.GET_PREFIX, 
            MethodLiteralConstants.IS_PREFIX));

    public PropertyAccessorFacetViaAccessorFactory() {
        super(FeatureType.PROPERTIES_ONLY, PREFIXES);
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
                new PropertyAccessorFacetViaAccessor(
                        typeSpec, accessorMethod, property));
    }

    // ///////////////////////////////////////////////////////
    // PropertyOrCollectionIdentifying
    // ///////////////////////////////////////////////////////

    @Override
    public boolean isPropertyOrCollectionAccessorCandidate(final Method method) {
        final String methodName = method.getName();
        if (methodName.startsWith(MethodLiteralConstants.GET_PREFIX)) {
            return true;
        }
        if (methodName.startsWith(MethodLiteralConstants.IS_PREFIX) && method.getReturnType() == boolean.class) {
            return true;
        }
        return false;
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
        if (!isPropertyOrCollectionAccessorCandidate(method)) {
            return false;
        }
        final Class<?> methodReturnType = method.getReturnType();
        return isCollectionOrArray(methodReturnType);
    }

    @Override
    public void findAndRemovePropertyAccessors(final MethodRemover methodRemover, final List<Method> methodListToAppendTo) {
        appendMatchingMethods(methodRemover, MethodLiteralConstants.IS_PREFIX, boolean.class, methodListToAppendTo);
        appendMatchingMethods(methodRemover, MethodLiteralConstants.GET_PREFIX, Object.class, methodListToAppendTo);
    }

    private static void appendMatchingMethods(final MethodRemover methodRemover, final String prefix, final Class<?> returnType, final List<Method> methodListToAppendTo) {
        methodRemover.removeMethods(prefix, returnType, CanBeVoid.FALSE, 0, methodListToAppendTo::add);
    }

    @Override
    public void findAndRemoveCollectionAccessors(final MethodRemover methodRemover, final List<Method> methodListToAppendTo) {
        // does nothing
    }

}
