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
import java.util.List;

import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.facets.MethodPrefixConstants;
import org.apache.isis.metamodel.methodutils.MethodScope;

public class CreatedCallbackFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String[] PREFIXES = { MethodPrefixConstants.CREATED_PREFIX, };

    public CreatedCallbackFacetFactory() {
        super(FeatureType.OBJECTS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder holder = processClassContext.getFacetHolder();

        final List<Facet> facets = new ArrayList<Facet>();
        final List<Method> methods = new ArrayList<Method>();

        Method method = null;
        method = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, MethodPrefixConstants.CREATED_PREFIX, void.class, NO_PARAMETERS_TYPES);
        if (method != null) {
            methods.add(method);
            facets.add(new CreatedCallbackFacetViaMethod(method, holder));
        }

        processClassContext.removeMethods(methods);
        FacetUtil.addFacets(facets);
    }

}
