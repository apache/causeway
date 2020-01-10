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

package org.apache.isis.metamodel.facets.param.defaults.methodnum;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.collections._Arrays;
import org.apache.isis.metamodel.commons.StringExtensions;
import org.apache.isis.metamodel.exceptions.MetaModelException;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.facets.actions.defaults.ActionDefaultsFacet;

import lombok.val;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class ActionParameterDefaultsFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.empty();

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
        final int paramCount = actionMethod.getParameterCount();

        for (int i = 0; i < paramCount; i++) {

            // attempt to match method...
            Method defaultMethod = findDefaultNumMethod(processMethodContext, i);
            if (defaultMethod == null) {
                continue;
            }

            processMethodContext.removeMethod(defaultMethod);

            final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();
            if (facetedMethod.containsNonFallbackFacet(ActionDefaultsFacet.class)) {
                final Class<?> cls2 = processMethodContext.getCls();
                throw new MetaModelException(cls2 + " uses both old and new default syntax for " + actionMethod.getName() + "(...) - must use one or other");
            }

            // add facets directly to parameters, not to actions
            final FacetedMethodParameter paramAsHolder = parameters.get(i);
            super.addFacet(new ActionParameterDefaultsFacetViaMethod(defaultMethod, i, paramAsHolder));
        }
    }
    
    /**
     * search successively for the default method, trimming number of param types each loop
     */
    private static Method findDefaultNumMethod(ProcessMethodContext processMethodContext, int n) {
        
        val cls = processMethodContext.getCls();
        val actionMethod = processMethodContext.getMethod();
        Class<?>[] paramTypes = actionMethod.getParameterTypes();
        val returnType = paramTypes[n];
        val capitalizedName =
                MethodLiteralConstants.DEFAULT_PREFIX + n +
                StringExtensions.asCapitalizedName(actionMethod.getName());

        for(;;) {
            val method = MethodFinderUtils.findMethod(
                    cls,
                    capitalizedName,
                    returnType, 
                    paramTypes);
            
            if(method != null) {
                return method;
            }
            
            if(paramTypes.length==0) {
                break;
            }
            
            // remove last, and search again
            paramTypes = _Arrays.removeByIndex(paramTypes, paramTypes.length-1);
        }
        
        return null;
    }

}
