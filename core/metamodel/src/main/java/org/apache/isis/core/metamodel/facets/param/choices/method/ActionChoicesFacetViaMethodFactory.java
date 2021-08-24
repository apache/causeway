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

import javax.inject.Inject;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.methods.MethodFinder;
import org.apache.isis.core.metamodel.methods.MethodLiteralConstants;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

public class ActionChoicesFacetViaMethodFactory
extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String PREFIX = MethodLiteralConstants.CHOICES_PREFIX;

    @Inject
    public ActionChoicesFacetViaMethodFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
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

        addFacet(new ActionChoicesFacetViaMethod(choicesMethod, returnType, action));

    }

    protected Method findChoicesMethodReturning(
            final ProcessMethodContext processMethodContext,
            final Class<?> returnType) {

        val cls = processMethodContext.getCls();
        val namingConvention = processMethodContext.memberSupportCandidates(PREFIX);

        Method choicesMethod =
                MethodFinder.findMethod(
                        processMethodContext.getEncapsulationPolicy(),
                        cls, namingConvention, returnType, NO_ARG)
                .findFirst()
                .orElse(null);
        return choicesMethod;
    }


}
