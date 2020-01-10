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

package org.apache.isis.metamodel.facets.object.callbacks;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;

import static org.apache.isis.metamodel.facets.MethodLiteralConstants.LOADED_PREFIX;
import static org.apache.isis.metamodel.facets.MethodLiteralConstants.LOADING_PREFIX;

import lombok.val;

public class LoadCallbackFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.ofCollection(_Lists.of(
            LOADED_PREFIX, 
            LOADING_PREFIX));

    public LoadCallbackFacetFactory() {
        super(FeatureType.OBJECTS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        Method method = null;
        method = MethodFinderUtils.findMethod(cls, LOADING_PREFIX, void.class, NO_ARG);
        if (method != null) {
            processClassContext.removeMethod(method);
            super.addFacet(new LoadingCallbackFacetViaMethod(method, facetHolder));
        }

        method = MethodFinderUtils.findMethod(cls, LOADED_PREFIX, void.class, NO_ARG);
        if (method != null) {
            processClassContext.removeMethod(method);
            super.addFacet(new LoadedCallbackFacetViaMethod(method, facetHolder));
        }

    }

}
