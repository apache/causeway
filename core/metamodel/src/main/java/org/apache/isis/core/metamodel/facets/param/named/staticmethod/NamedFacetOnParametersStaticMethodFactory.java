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

package org.apache.isis.core.metamodel.facets.param.named.staticmethod;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.commons.lang.MethodExtensions;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.members.named.staticmethod.NamedFacetStaticMethod;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodPrefixConstants;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class NamedFacetOnParametersStaticMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String[] PREFIXES = { MethodPrefixConstants.NAME_PREFIX };

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public NamedFacetOnParametersStaticMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();
        final List<FacetedMethodParameter> holderList = facetedMethod.getParameters();

        attachNamedFacetForParametersIfParameterNamesMethodIsFound(processMethodContext, holderList);
    }

    private static void attachNamedFacetForParametersIfParameterNamesMethodIsFound(final ProcessMethodContext processMethodContext, final List<FacetedMethodParameter> parameters) {

        if (parameters.isEmpty()) {
            return;
        }

        final Method actionMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Method namesMethod = MethodFinderUtils.findMethod(cls, MethodScope.CLASS, MethodPrefixConstants.NAME_PREFIX + capitalizedName, String[].class, new Class[0]);
        if (namesMethod == null) {
            return;
        }
        try {
            final String[] names = invokeNamesMethod(namesMethod, parameters.size());
            for (int i = 0; i < names.length; i++) {
                // add facets directly to parameters, not to actions
                FacetUtil.addFacet(new NamedFacetStaticMethod(names[i], namesMethod, parameters.get(i)));
            }
        } finally {
            processMethodContext.removeMethod(namesMethod);
        }
    }

    private static String[] invokeNamesMethod(final Method namesMethod, final int numElementsRequired) {
        String[] names = null;
        try {
            names = (String[]) MethodExtensions.invokeStatic(namesMethod, new Object[0]);
        } catch (final ClassCastException ex) {
            // ignore
        }
        if (names == null || names.length != numElementsRequired) {
            throw new MetaModelException(namesMethod + " must return an String[] array of same size as number of parameters of action");
        }
        return names;
    }
}
