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
package org.apache.isis.core.metamodel.facets.object.callbacks;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.methods.MethodFinderOptions;
import org.apache.isis.core.metamodel.methods.MethodFinderUtils;
import org.apache.isis.core.metamodel.methods.MethodLiteralConstants;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import lombok.NonNull;
import lombok.val;

abstract class CallbackFacetFactoryAbstract
extends MethodPrefixBasedFacetFactoryAbstract {

    private final MethodLiteralConstants.CallbackMethod callbackMethodEnum;
    private final BiFunction<Can<Method>, FacetHolder, CallbackFacet> callbackFacetConstructor;

    protected CallbackFacetFactoryAbstract(
            final @NonNull MetaModelContext mmc,
            final @NonNull MethodLiteralConstants.CallbackMethod callbackMethodEnum,
            final @NonNull BiFunction<Can<Method>, FacetHolder, CallbackFacet> callbackFacetConstructor) {

        super(mmc, FeatureType.OBJECTS_ONLY, OrphanValidation.VALIDATE, callbackMethodEnum.getMethodNames());
        this.callbackMethodEnum = callbackMethodEnum;
        this.callbackFacetConstructor = callbackFacetConstructor;
    }

    @Override
    public final void process(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        val callbackMethods = callbackMethodEnum
                .getMethodNames()
                .map(callbackMethodName->MethodFinderUtils.findMethod(
                        MethodFinderOptions
                        .livecycleCallback(processClassContext.getIntrospectionPolicy()),
                        cls, callbackMethodName, void.class, NO_ARG));

        if(callbackMethods.isNotEmpty()) {
            callbackMethods.forEach(processClassContext::removeMethod);
            addFacet(callbackFacetConstructor.apply(callbackMethods, facetHolder));
        }
    }

}
