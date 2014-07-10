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

package org.apache.isis.core.metamodel.facets.param.mandatory.staticmethod;

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
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodPrefixConstants;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class MandatoryFacetOnParameterStaticMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String[] PREFIXES = { MethodPrefixConstants.OPTIONAL_PREFIX };

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public MandatoryFacetOnParameterStaticMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();
        final List<FacetedMethodParameter> holderList = facetedMethod.getParameters();

        attachMandatoryFacetForParametersIfOptionalMethodIsFound(processMethodContext, holderList);

    }

    private static void attachMandatoryFacetForParametersIfOptionalMethodIsFound(final ProcessMethodContext processMethodContext, final List<FacetedMethodParameter> parameters) {

        if (parameters.isEmpty()) {
            return;
        }

        final Method actionMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Method optionalMethod = MethodFinderUtils.findMethod(cls, MethodScope.CLASS, MethodPrefixConstants.OPTIONAL_PREFIX + capitalizedName, boolean[].class, new Class[0]);
        if (optionalMethod == null) {
            return;
        }
        try {
            final boolean[] optionals = invokeOptionalsMethod(optionalMethod, parameters.size());

            for (int i = 0; i < optionals.length; i++) {
                if (optionals[i]) {
                    // add facets directly to parameters, not to actions
                    FacetUtil.addFacet(new MandatoryFacetOnParameterStaticMethod(parameters.get(i)));
                }
            }
        } finally {
            processMethodContext.removeMethod(optionalMethod);
        }
    }

    private static boolean[] invokeOptionalsMethod(final Method optionalMethod, final int numElementsRequired) {
        boolean[] optionals = null;
        try {
            optionals = (boolean[]) MethodExtensions.invokeStatic(optionalMethod, new Object[0]);
        } catch (final ClassCastException ex) {
            // ignore, test below
        }
        if (optionals == null || optionals.length != numElementsRequired) {
            throw new MetaModelException(optionalMethod + " must return an boolean[] array of same size as number of parameters of action");
        }
        return optionals;
    }

}
