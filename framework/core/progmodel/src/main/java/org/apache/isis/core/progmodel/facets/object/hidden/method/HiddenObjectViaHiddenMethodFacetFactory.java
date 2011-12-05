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

package org.apache.isis.core.progmodel.facets.object.hidden.method;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.progmodel.facets.MethodFinderUtils;
import org.apache.isis.core.progmodel.facets.MethodPrefixBasedFacetFactoryAbstract;

public class HiddenObjectViaHiddenMethodFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String VALIDATE_PREFIX = "hidden";

    private static final String[] PREFIXES = { VALIDATE_PREFIX, };

    public HiddenObjectViaHiddenMethodFacetFactory() {
        super(FeatureType.OBJECTS_ONLY, PREFIXES);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        attachHideFacetIfHideMethodIsFound(processClassContext);
    }

    public void attachHideFacetIfHideMethodIsFound(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final Method methods[] = cls.getMethods();

        final Method uBooleanMethod =
            MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, VALIDATE_PREFIX, Boolean.class, NO_PARAMETERS_TYPES);
        if (uBooleanMethod != null) {
            addFacetToFacetHolder(methods, facetHolder, uBooleanMethod);
            processClassContext.removeMethod(uBooleanMethod);
            return;
        }
        final Method lBooleanMethod =
            MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, VALIDATE_PREFIX, boolean.class, NO_PARAMETERS_TYPES);
        if (lBooleanMethod != null) {
            addFacetToFacetHolder(methods, facetHolder, lBooleanMethod);
            processClassContext.removeMethod(lBooleanMethod);
        }
    }

    protected void addFacetToFacetHolder(final Method methods[], final FacetHolder facetHolder, final Method facetMethod) {
        // Original
        FacetUtil.addFacet(new HiddenObjectFacetViaHiddenMethod(facetMethod, HiddenObjectFacetViaHiddenMethod.class,
            facetHolder, false));

        // Dan's suggestion
        // for (ObjectMember member : objectSpec.getMembers()) {
        // FacetUtil.addFacet(new DisabledObjectFacetViaDisabledMethod(method, member));
        // }

        // Try 3?
        for (Method method : methods) {
            if (!anIsisMethod(method.getName())) {
                // FacetUtil.addFacet(new HideForSessionFacetViaMethod(method, facetHolder));

                // DNW
                // FacetUtil.addFacet(new HideForContextFacetViaMethod(method, facetHolder));

                // DNW
                // FacetUtil.addFacet(new HiddenObjectFacetViaHiddenMethod(method,
                // HiddenObjectFacetViaHiddenMethod.class,
                // facetHolder, false));
            }
        }

    }

    /**
     * Check if method name starts with any of the Isis prefixes...
     * 
     * @param name
     * @return
     */
    private boolean anIsisMethod(String name) {
        List<String> names =
            Arrays.asList("choices", "clear", "created", "default", "hide", "validate", "disable", "iconName",
                "modify", "hidden", "set");

        for (String prefix : names) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}
