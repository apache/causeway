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

package org.apache.isis.core.metamodel.facets.object.dirty.method;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;

public class DirtyMethodsFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String MARK_DIRTY_PREFIX = "markDirty";
    private static final String CLEAR_DIRTY_PREFIX = "clearDirty";
    private static final String IS_DIRTY_PREFIX = "isDirty";

    private static final String[] PREFIXES = { MARK_DIRTY_PREFIX, CLEAR_DIRTY_PREFIX, IS_DIRTY_PREFIX, };

    public DirtyMethodsFacetFactory() {
        super(FeatureType.OBJECTS_ONLY, OrphanValidation.VALIDATE, PREFIXES);

    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        final Class<?> cls = processClassContext.getCls();

        final List<Facet> facets = new ArrayList<Facet>();

        Method method = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, IS_DIRTY_PREFIX, boolean.class, NO_PARAMETERS_TYPES);
        if (method != null) {
            processClassContext.removeMethod(method);
            facets.add(new IsDirtyObjectFacetViaMethod(method, facetHolder));
        }

        method = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, CLEAR_DIRTY_PREFIX, void.class, NO_PARAMETERS_TYPES);
        if (method != null) {
            processClassContext.removeMethod(method);
            facets.add(new ClearDirtyObjectFacetViaMethod(method, facetHolder));
        }

        method = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, MARK_DIRTY_PREFIX, void.class, NO_PARAMETERS_TYPES);
        if (method != null) {
            processClassContext.removeMethod(method);
            facets.add(new MarkDirtyObjectFacetViaMethod(method, facetHolder));
        }

        FacetUtil.addFacets(facets);
    }

}
