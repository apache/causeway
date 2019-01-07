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

package org.apache.isis.core.metamodel.facets.object.layout;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.methodutils.MethodScope;

public class LayoutFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String ICON_NAME_PREFIX = "iconName";

    private static final String[] PREFIXES = { ICON_NAME_PREFIX, };

    public LayoutFacetFactory() {
        super(FeatureType.OBJECTS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final Method method = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, ICON_NAME_PREFIX, String.class, NO_PARAMETERS_TYPES);

        final LayoutFacet facet;
        if (method == null) {
            facet = new LayoutFacetFallback(facetHolder);
        } else {
            processClassContext.removeMethod(method);
            facet = new LayoutFacetMethod(method, facetHolder);
        }
        FacetUtil.addFacet(facet);
    }
}
