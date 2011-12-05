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

package org.apache.isis.core.progmodel.facets.object.disabled.method;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.progmodel.facets.MethodFinderUtils;
import org.apache.isis.core.progmodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.progmodel.facets.members.disable.method.DisableForContextFacetViaMethod;

public class DisabledObjectViaDisabledMethodFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String DISABLED_PREFIX = "disabled";

    private static final String[] PREFIXES = { DISABLED_PREFIX, };

    public DisabledObjectViaDisabledMethodFacetFactory() {
        super(FeatureType.OBJECTS_ONLY, PREFIXES);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final Method method =
            MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, DISABLED_PREFIX, String.class, NO_PARAMETERS_TYPES);
        if (method != null) {

            FacetUtil.addFacet(new DisabledObjectFacetViaDisabledMethod(method, facetHolder));

            // DNW
            ObjectSpecification spec = getSpecificationLookup().loadSpecification(cls);
            List<ObjectAssociation> members = spec.getAssociations();
            for (ObjectAssociation member : members) {
                FacetUtil.addFacet(new DisableForContextFacetViaMethod(method, member));
            }

            // Method methods[] = cls.getMethods();
            // addFacetToFacetHolder(methods, facetHolder, method);
            processClassContext.removeMethod(method);
        }
    }

    protected void addFacetToFacetHolder(final Method methods[], final FacetHolder facetHolder, final Method facetMethod) {
        // Original
        FacetUtil.addFacet(new DisabledObjectFacetViaDisabledMethod(facetMethod, facetHolder));

        // Dan's suggestion
        // for (ObjectMember member : objectSpec.getMembers()) {
        // FacetUtil.addFacet(new DisabledObjectFacetViaDisabledMethod(method, member));
        // }

        // Try 3?
        for (Method method : methods) {
            if (!anIsisMethod(method.getName())) {
                // DNW
                // FacetUtil.addFacet(new DisableForContextFacetViaMethod(method, facetHolder));
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
                "modify", "set");

        for (String prefix : names) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
