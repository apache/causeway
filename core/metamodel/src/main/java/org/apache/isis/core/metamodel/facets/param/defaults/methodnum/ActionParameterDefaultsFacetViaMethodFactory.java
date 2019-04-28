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

package org.apache.isis.core.metamodel.facets.param.defaults.methodnum;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodPrefixConstants;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.services.ServicesInjector;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class ActionParameterDefaultsFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String[] PREFIXES = {};

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public ActionParameterDefaultsFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();
        final List<FacetedMethodParameter> holderList = facetedMethod.getParameters();

        attachDefaultFacetForParametersIfDefaultsNumMethodIsFound(processMethodContext, holderList);
    }

    private void attachDefaultFacetForParametersIfDefaultsNumMethodIsFound(final ProcessMethodContext processMethodContext, final List<FacetedMethodParameter> parameters) {

        if (parameters.isEmpty()) {
            return;
        }

        final Method actionMethod = processMethodContext.getMethod();
        final Class<?>[] paramTypes = actionMethod.getParameterTypes();

        for (int i = 0; i < paramTypes.length; i++) {

            // attempt to match method...
            Method defaultMethod = findDefaultNumMethod(processMethodContext, i);
            if (defaultMethod == null) {
                continue;
            }

            processMethodContext.removeMethod(defaultMethod);

            final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();
            if (facetedMethod.containsDoOpFacet(ActionDefaultsFacet.class)) {
                final Class<?> cls2 = processMethodContext.getCls();
                throw new MetaModelException(cls2 + " uses both old and new default syntax for " + actionMethod.getName() + "(...) - must use one or other");
            }

            // add facets directly to parameters, not to actions
            final FacetedMethodParameter paramAsHolder = parameters.get(i);
            FacetUtil.addFacet(new ActionParameterDefaultsFacetViaMethod(defaultMethod, i, paramAsHolder, adapterProvider));
        }
    }

    /**
     * search successively for the default method, trimming number of param types each loop
     */
    private static Method findDefaultNumMethod(ProcessMethodContext processMethodContext, int n) {

        final Method actionMethod = processMethodContext.getMethod();
        final List<Class<?>> paramTypes = ListExtensions.mutableCopy(actionMethod.getParameterTypes());

        final int numParamTypes = paramTypes.size();

        for(int i=0; i< numParamTypes+1; i++) {
            final Method method = findDefaultNumMethod(processMethodContext, n, paramTypes.toArray(new Class<?>[]{}));
            if(method != null) {
                return method;
            }
            // remove last, and search again
            if(!paramTypes.isEmpty()) {
                paramTypes.remove(paramTypes.size()-1);
            }
        }

        return null;
    }

    private static Method findDefaultNumMethod(final ProcessMethodContext processMethodContext, int n, Class<?>[] paramTypes) {
        final Class<?> cls = processMethodContext.getCls();
        final Method actionMethod = processMethodContext.getMethod();
        final Class<?> returnType = actionMethod.getParameterTypes()[n];
        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());
        return MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, MethodPrefixConstants.DEFAULT_PREFIX + n + capitalizedName, returnType, paramTypes);
    }

    // ///////////////////////////////////////////////////////////////
    // Dependencies
    // ///////////////////////////////////////////////////////////////


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        adapterProvider = servicesInjector.getPersistenceSessionServiceInternal();
    }

    ObjectAdapterProvider adapterProvider;

}
