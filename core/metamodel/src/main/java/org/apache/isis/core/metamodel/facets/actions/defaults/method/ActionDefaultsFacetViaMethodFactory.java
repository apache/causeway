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
package org.apache.isis.core.metamodel.facets.actions.defaults.method;

import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.methods.MethodFinder;
import org.apache.isis.core.metamodel.methods.MethodFinderOptions;
import org.apache.isis.core.metamodel.methods.MethodLiteralConstants;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class ActionDefaultsFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String PREFIX = MethodLiteralConstants.DEFAULT_PREFIX;

    @Inject
    public ActionDefaultsFacetViaMethodFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        attachActionDefaultsFacetIfParameterDefaultsMethodIsFound(processMethodContext);
    }

    private void attachActionDefaultsFacetIfParameterDefaultsMethodIsFound(
            final ProcessMethodContext processMethodContext) {

        Method defaultsMethod = findDefaultsMethodReturning(processMethodContext, Object[].class);
        if (defaultsMethod == null) {
            defaultsMethod = findDefaultsMethodReturning(processMethodContext, List.class);
        }
        if (defaultsMethod == null) {
            return;
        }

        processMethodContext.removeMethod(defaultsMethod);

        val facetedMethod = processMethodContext.getFacetHolder();
        addFacet(new ActionDefaultsFacetViaMethod(defaultsMethod, facetedMethod));
    }

    private static Method findDefaultsMethodReturning(
            final ProcessMethodContext processMethodContext,
            final Class<?> returnType) {

        val cls = processMethodContext.getCls();
        val methodNameCandidates = processMethodContext.memberSupportCandidates(PREFIX);
        return MethodFinder.findMethod(
                MethodFinderOptions
                .memberSupport(processMethodContext.getIntrospectionPolicy()),
                cls, methodNameCandidates, returnType, NO_ARG)
                .findFirst()
                .orElse(null);
    }

}
