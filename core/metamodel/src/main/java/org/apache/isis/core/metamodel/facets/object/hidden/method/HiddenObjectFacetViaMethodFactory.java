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

package org.apache.isis.core.metamodel.facets.object.hidden.method;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.hidden.HiddenObjectFacet;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

/**
 * Installs the {@link HiddenObjectFacetViaMethod} on the
 * {@link ObjectSpecification}, and copies this facet onto each
 * {@link ObjectMember}.
 *
 * <p>
 * This two-pass design is required because, at the time that the
 * {@link #process(org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext)
 * class is being processed}, the {@link ObjectMember member}s for the
 * {@link ObjectSpecification spec} are not known.
 */
public class HiddenObjectFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String HIDDEN_PREFIX = "hidden";

    private static final String[] PREFIXES = { HIDDEN_PREFIX, };

    public HiddenObjectFacetViaMethodFactory() {
        super(FeatureType.EVERYTHING_BUT_PARAMETERS, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        for (final Class<?> returnType : new Class<?>[] { Boolean.class, boolean.class }) {
            if (addFacetIfMethodFound(processClassContext, returnType)) {
                return;
            }
        }
        return;
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final FacetedMethod member = processMethodContext.getFacetHolder();
        final Class<?> owningClass = processMethodContext.getCls();
        final ObjectSpecification owningSpec = getSpecificationLoader().loadSpecification(owningClass);
        final HiddenObjectFacet facet = owningSpec.getFacet(HiddenObjectFacet.class);
        if (facet != null) {
            facet.copyOnto(member);
        }
    }

    private boolean addFacetIfMethodFound(final ProcessClassContext processClassContext, final Class<?> returnType) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final Method method = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, HIDDEN_PREFIX, returnType, NO_PARAMETERS_TYPES);
        if (method == null) {
            return false;
        }
        FacetUtil.addFacet(new HiddenObjectFacetViaMethod(method, facetHolder));
        processClassContext.removeMethod(method);
        return true;
    }

}
