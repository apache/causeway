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

package org.apache.isis.metamodel.facets.param.choices.methodnum;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.metamodel.exceptions.MetaModelException;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.facets.MethodPrefixConstants;
import org.apache.isis.metamodel.facets.param.choices.ActionChoicesFacet;
import org.apache.isis.metamodel.methodutils.MethodScope;

public class ActionParameterChoicesFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String[] PREFIXES = {};

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public ActionParameterChoicesFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();
        final List<FacetedMethodParameter> holderList = facetedMethod.getParameters();

        attachChoicesFacetForParametersIfChoicesNumMethodIsFound(processMethodContext, holderList);

    }

    private void attachChoicesFacetForParametersIfChoicesNumMethodIsFound(final ProcessMethodContext processMethodContext, final List<FacetedMethodParameter> parameters) {

        if (parameters.isEmpty()) {
            return;
        }

        final Method actionMethod = processMethodContext.getMethod();
        final Class<?>[] paramTypes = actionMethod.getParameterTypes();

        for (int i = 0; i < paramTypes.length; i++) {

            final Class<?> arrayOfParamType = (Array.newInstance(paramTypes[i], 0)).getClass();

            final Method choicesMethod = findChoicesNumMethodReturning(processMethodContext, i);
            if (choicesMethod == null) {
                continue;
            }

            processMethodContext.removeMethod(choicesMethod);

            final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();
            if (facetedMethod.containsDoOpFacet(ActionChoicesFacet.class)) {
                final Class<?> cls = processMethodContext.getCls();
                throw new MetaModelException(cls + " uses both old and new choices syntax - must use one or other");
            }

            // add facets directly to parameters, not to actions
            final FacetedMethodParameter paramAsHolder = parameters.get(i);
            FacetUtil.addFacet(new ActionParameterChoicesFacetViaMethod(choicesMethod, arrayOfParamType, paramAsHolder));
        }
    }

    /**
     * search successively for the default method, trimming number of param types each loop
     */
    private static Method findChoicesNumMethodReturning(final ProcessMethodContext processMethodContext, final int n) {

        final Method actionMethod = processMethodContext.getMethod();
        final List<Class<?>> paramTypes = ListExtensions.mutableCopy(actionMethod.getParameterTypes());

        final Class<?> arrayOfParamType = (Array.newInstance(paramTypes.get(n), 0)).getClass();

        final int numParamTypes = paramTypes.size();

        for(int i=0; i< numParamTypes+1; i++) {
            Method method;

            method = findChoicesNumMethodReturning(processMethodContext, n, paramTypes.toArray(new Class<?>[]{}), arrayOfParamType);
            if(method != null) {
                return method;
            }
            method = findChoicesNumMethodReturning(processMethodContext, n, paramTypes.toArray(new Class<?>[]{}), Collection.class);
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



    private static Method findChoicesNumMethodReturning(final ProcessMethodContext processMethodContext, final int n, Class<?>[] paramTypes, final Class<?> returnType) {
        final Class<?> cls = processMethodContext.getCls();
        final Method actionMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());
        final String name = MethodPrefixConstants.CHOICES_PREFIX + n + capitalizedName;
        return MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, name, returnType, paramTypes);
    }



}
