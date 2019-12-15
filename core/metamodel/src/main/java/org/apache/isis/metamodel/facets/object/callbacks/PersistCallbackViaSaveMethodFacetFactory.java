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
import java.util.ArrayList;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

public class PersistCallbackViaSaveMethodFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.ofCollection(_Lists.of(
            MethodLiteralConstants.SAVED_PREFIX, 
            MethodLiteralConstants.SAVING_PREFIX));

    public PersistCallbackViaSaveMethodFacetFactory() {
        super(FeatureType.OBJECTS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();
        val facets = new ArrayList<Facet>();

        Method method = null;
        method = MethodFinderUtils.findMethod(cls, MethodLiteralConstants.SAVING_PREFIX, void.class, NO_PARAMETERS_TYPES);
        if (method != null) {
            processClassContext.removeMethod(method);
            val facet = facetHolder.getFacet(PersistingCallbackFacet.class);
            if (facet == null) {
                facets.add(new PersistingCallbackFacetViaMethod(method, facetHolder));
            } else {
                facet.addMethod(method);
            }
        }

        method = MethodFinderUtils.findMethod(cls, MethodLiteralConstants.SAVED_PREFIX, void.class, NO_PARAMETERS_TYPES);
        if (method != null) {
            processClassContext.removeMethod(method);
            val facet = facetHolder.getFacet(PersistedCallbackFacet.class);
            if (facet == null) {
                facets.add(new PersistedCallbackFacetViaMethod(method, facetHolder));
            } else {
                facet.addMethod(method);
            }
        }
        
        FacetUtil.addFacets(facets);
    }

}
