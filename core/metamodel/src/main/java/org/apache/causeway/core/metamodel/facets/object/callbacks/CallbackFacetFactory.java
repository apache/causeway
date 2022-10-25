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
package org.apache.causeway.core.metamodel.facets.object.callbacks;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.CallbackMethod;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.methods.MethodFinder;
import org.apache.causeway.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

public class CallbackFacetFactory
extends MethodPrefixBasedFacetFactoryAbstract {

    @Inject
    public CallbackFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY, OrphanValidation.VALIDATE,
                Stream.of(CallbackMethod.values())
                .map(CallbackMethod::getMethodNames)
                .flatMap(Can::stream)
                .collect(Can.toCan()));
    }

    @Override
    public final void process(final ProcessClassContext processClassContext) {
        processCallback(processClassContext, CallbackMethod.CREATED, CreatedCallbackFacetViaMethod::new);
        processCallback(processClassContext, CallbackMethod.LOADED, LoadedCallbackFacetViaMethod::new);
        processCallback(processClassContext, CallbackMethod.PERSISTED, PersistedCallbackFacetViaMethod::new);
        processCallback(processClassContext, CallbackMethod.PERSISTING, PersistingCallbackFacetViaMethod::new);
        processCallback(processClassContext, CallbackMethod.REMOVING, RemovingCallbackFacetViaMethod::new);
        processCallback(processClassContext, CallbackMethod.UPDATED, UpdatedCallbackFacetViaMethod::new);
        processCallback(processClassContext, CallbackMethod.UPDATING, UpdatingCallbackFacetViaMethod::new);
    }

    private void processCallback(
            final ProcessClassContext processClassContext,
            final CallbackMethod callbackMethodEnum,
            final BiFunction<Can<Method>, FacetHolder, CallbackFacet> callbackFacetConstructor) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        val callbackMethods =

        MethodFinder
        .livecycleCallback(
                cls,
                callbackMethodEnum.getMethodNames(),
                processClassContext.getIntrospectionPolicy())
        .withRequiredReturnType(void.class)
        .streamMethodsMatchingSignature(NO_ARG)
        .peek(processClassContext::removeMethod)
        .collect(Can.toCan());

        if(callbackMethods.isNotEmpty()) {
            addFacet(callbackFacetConstructor.apply(callbackMethods, facetHolder));
        }

    }


}
