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

package org.apache.isis.metamodel.facets.param.autocomplete.method;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.metamodel.commons.StringExtensions;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;

public class ActionParameterAutoCompleteFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.ofSingleton(MethodLiteralConstants.AUTO_COMPLETE_PREFIX);

    public ActionParameterAutoCompleteFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();
        final List<FacetedMethodParameter> holderList = facetedMethod.getParameters();

        attachAutoCompleteFacetForParametersIfAutoCompleteNumMethodIsFound(processMethodContext, holderList);

    }

    private void attachAutoCompleteFacetForParametersIfAutoCompleteNumMethodIsFound(final ProcessMethodContext processMethodContext, final List<FacetedMethodParameter> parameters) {

        if (parameters.isEmpty()) {
            return;
        }

        final Method actionMethod = processMethodContext.getMethod();
        final Class<?>[] params = actionMethod.getParameterTypes();

        for (int i = 0; i < params.length; i++) {

            final Class<?> paramType = params[i];
            final Class<?> arrayOfParamType = (Array.newInstance(paramType, 0)).getClass();

            @SuppressWarnings("rawtypes")
            final Class[] returnTypes = { arrayOfParamType, List.class, Set.class, Collection.class };
            Method autoCompleteMethod = findAutoCompleteNumMethodReturning(processMethodContext, i, returnTypes);
            if (autoCompleteMethod == null) {
                continue;
            }
            processMethodContext.removeMethod(autoCompleteMethod);

            // add facets directly to parameters, not to actions
            final FacetedMethodParameter paramAsHolder = parameters.get(i);
            super.addFacet(
                    new ActionParameterAutoCompleteFacetViaMethod(
                            autoCompleteMethod, paramType, paramAsHolder));
        }
    }

    private Method findAutoCompleteNumMethodReturning(
            final ProcessMethodContext processMethodContext,
            final int paramNum,
            final Class<?>[] returnTypes) {

        final Class<?> cls = processMethodContext.getCls();
        final Method actionMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());
        final String name = MethodLiteralConstants.AUTO_COMPLETE_PREFIX + paramNum + capitalizedName;
        return MethodFinderUtils.findMethod_returningAnyOf(
                returnTypes,
                cls,
                name,
                new Class[]{String.class});
    }

}
