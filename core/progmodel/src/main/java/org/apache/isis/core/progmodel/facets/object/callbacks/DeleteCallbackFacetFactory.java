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


package org.apache.isis.core.progmodel.facets.object.callbacks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingCallbackFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.progmodel.java5.MethodPrefixBasedFacetFactoryAbstract;


public class DeleteCallbackFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String DELETED_PREFIX = "deleted";
    private static final String DELETING_PREFIX = "deleting";

    private static final String[] PREFIXES = { DELETED_PREFIX, DELETING_PREFIX, };

    public DeleteCallbackFacetFactory() {
        super(PREFIXES, ObjectFeatureType.OBJECTS_ONLY);
    }

    @Override
    public boolean process(final Class<?> cls, final MethodRemover remover, final FacetHolder facetHolder) {
        final List<Facet> facets = new ArrayList<Facet>();
        final List<Method> methods = new ArrayList<Method>();

        Method method = null;
        method = findMethod(cls, OBJECT, DELETING_PREFIX, void.class, NO_PARAMETERS_TYPES);
        if (method != null) {
            methods.add(method);
            RemovingCallbackFacet facet = facetHolder.getFacet(RemovingCallbackFacet.class);
            if (facet == null) {
            	facets.add(new RemovingCallbackFacetViaMethod(method, facetHolder));
            } else {
            	facet.addMethod(method);
            }
        }

        method = findMethod(cls, OBJECT, DELETED_PREFIX, void.class, NO_PARAMETERS_TYPES);
        if (method != null) {
            methods.add(method);
            RemovedCallbackFacet facet = facetHolder.getFacet(RemovedCallbackFacet.class);
            if (facet == null) {
            	facets.add(new RemovedCallbackFacetViaMethod(method, facetHolder));
            } else {
            	facet.addMethod(method);
            }
        }

        remover.removeMethods(methods);
        return FacetUtil.addFacets(facets);
    }

}
