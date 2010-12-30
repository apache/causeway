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

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.progmodel.facets.MethodPrefixBasedFacetFactoryAbstract;


public class UpdateCallbackFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String UPDATED_PREFIX = "updated";
    private static final String UPDATING_PREFIX = "updating";

    private static final String[] PREFIXES = { UPDATED_PREFIX, UPDATING_PREFIX, };

    public UpdateCallbackFacetFactory() {
        super(PREFIXES, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public boolean process(final Class<?> cls, final MethodRemover remover, final FacetHolder facetHolder) {
        final List<Facet> facets = new ArrayList<Facet>();
        final List<Method> methods = new ArrayList<Method>();

        Method method = null;
        method = findMethod(cls, OBJECT, UPDATING_PREFIX, void.class, NO_PARAMETERS_TYPES);
        if (method != null) {
            methods.add(method);
            facets.add(new UpdatingCallbackFacetViaMethod(method, facetHolder));
        }

        method = findMethod(cls, OBJECT, UPDATED_PREFIX, void.class, NO_PARAMETERS_TYPES);
        if (method != null) {
            methods.add(method);
            facets.add(new UpdatedCallbackFacetViaMethod(method, facetHolder));
        }

        remover.removeMethods(methods);
        return FacetUtil.addFacets(facets);
    }

}
