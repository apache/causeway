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
package org.apache.isis.core.metamodel.facets.object.support;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedCallbackFacetViaMethod;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedCallbackFacetViaMethod;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacetViaMethod;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacetViaMethod;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingCallbackFacetViaMethod;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedCallbackFacetViaMethod;
import org.apache.isis.core.metamodel.methods.MethodFinderOptions;
import org.apache.isis.core.metamodel.methods.MethodFinderUtils;
import org.apache.isis.core.metamodel.methods.MethodLiteralConstants.ObjectSupportMethod;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

public class ObjectSupportFacetFactory
extends MethodPrefixBasedFacetFactoryAbstract {

    @Inject
    public ObjectSupportFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY, OrphanValidation.VALIDATE,
                Stream.of(ObjectSupportMethod.values())
                .map(ObjectSupportMethod::getMethodNames)
                .flatMap(Can::stream)
                .collect(Can.toCan()));
    }

    @Override
    public final void process(final ProcessClassContext processClassContext) {
        processObjectSupport(processClassContext, ObjectSupportMethod.HIDDEN, CreatedCallbackFacetViaMethod::new);
        processObjectSupport(processClassContext, ObjectSupportMethod.DISABLED, LoadedCallbackFacetViaMethod::new);
        processObjectSupport(processClassContext, ObjectSupportMethod.TITLE, PersistedCallbackFacetViaMethod::new);
        processObjectSupport(processClassContext, ObjectSupportMethod.LAYOUT, PersistingCallbackFacetViaMethod::new);
        processObjectSupport(processClassContext, ObjectSupportMethod.ICON_NAME, RemovingCallbackFacetViaMethod::new);
        processObjectSupport(processClassContext, ObjectSupportMethod.CSS_CLASS, UpdatedCallbackFacetViaMethod::new);
    }

    private void processObjectSupport(
            final ProcessClassContext processClassContext,
            final ObjectSupportMethod callbackMethodEnum,
            final BiFunction<Can<Method>, FacetHolder, CallbackFacet> ojectSupportFacetConstructor) {
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
            addFacet(ojectSupportFacetConstructor.apply(callbackMethods, facetHolder));
        }
    }


}
