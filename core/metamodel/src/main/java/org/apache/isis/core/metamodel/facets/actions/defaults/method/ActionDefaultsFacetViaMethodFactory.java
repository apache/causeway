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

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal._Constants;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class ActionDefaultsFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String PREFIX = MethodLiteralConstants.DEFAULT_PREFIX;

    public ActionDefaultsFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
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
        super.addFacet(new ActionDefaultsFacetViaMethod(defaultsMethod, facetedMethod));
    }

    private static Method findDefaultsMethodReturning(
            final ProcessMethodContext processMethodContext, 
            final Class<?> returnType) {

        val actionMethod = processMethodContext.getMethod();
        val namingConvention = PREFIX_BASED_NAMING.providerForAction(actionMethod, PREFIX);
        val cls = processMethodContext.getCls();
        return MethodFinderUtils.findMethod(
                cls, namingConvention.get(), returnType, NO_ARG);
    }

}
