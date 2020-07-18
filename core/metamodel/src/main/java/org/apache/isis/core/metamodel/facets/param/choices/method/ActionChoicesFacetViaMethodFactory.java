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

package org.apache.isis.core.metamodel.facets.param.choices.method;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.MethodFinder2;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

public class ActionChoicesFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String PREFIX = MethodLiteralConstants.CHOICES_PREFIX;

    public ActionChoicesFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        attachActionChoicesFacetIfParameterChoicesMethodIsFound(processMethodContext);
    }

    private void attachActionChoicesFacetIfParameterChoicesMethodIsFound(final ProcessMethodContext processMethodContext) {

        val actionMethod = processMethodContext.getMethod();

        if (actionMethod.getParameterCount() <= 0) {
            return;
        }
        
        Method choicesMethod = null;
        if (choicesMethod == null) {
            choicesMethod = findChoicesMethodReturning(processMethodContext, Object[][].class);
        }
        if (choicesMethod == null) {
            choicesMethod = findChoicesMethodReturning(processMethodContext, Object[].class);
        }
        if (choicesMethod == null) {
            choicesMethod = findChoicesMethodReturning(processMethodContext, Collection.class);
        }
        if (choicesMethod == null) {
            return;
        }
        processMethodContext.removeMethod(choicesMethod);

        final Class<?> returnType = actionMethod.getReturnType();
        final FacetHolder action = processMethodContext.getFacetHolder();
        
        super.addFacet(new ActionChoicesFacetViaMethod(choicesMethod, returnType, action));
        
    }

    protected Method findChoicesMethodReturning(
            final ProcessMethodContext processMethodContext, 
            final Class<?> returnType) {

        val cls = processMethodContext.getCls();
        val actionMethod = processMethodContext.getMethod();
        val namingConvention = PREFIX_BASED_NAMING.map(naming->naming.providerForAction(actionMethod, PREFIX));
        
        Method choicesMethod = 
                MethodFinder2.findMethod(cls, namingConvention.map(x->x.get()), returnType, NO_ARG)
                .findFirst()
                .orElse(null); 
        return choicesMethod;
    }


}
