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

package org.apache.isis.core.progmodel.facets.param.choices.methodnum;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.commons.lang.NameUtils;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.actions.choices.ActionChoicesFacet;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.progmodel.facets.MethodFinderUtils;
import org.apache.isis.core.progmodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.progmodel.facets.MethodPrefixConstants;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class ActionParameterChoicesFacetFactory extends MethodPrefixBasedFacetFactoryAbstract implements AdapterManagerAware {

    private static final String[] PREFIXES = {};

    private AdapterManager adapterManager;

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public ActionParameterChoicesFacetFactory() {
        super(FeatureType.ACTIONS_ONLY, PREFIXES);
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
        final Class<?>[] params = actionMethod.getParameterTypes();

        for (int i = 0; i < params.length; i++) {

            final Class<?> arrayOfParamType = (Array.newInstance(params[i], 0)).getClass();

            Method choicesMethod = findChoicesNumMethodReturning(processMethodContext, i, arrayOfParamType);
            if (choicesMethod == null) {
                choicesMethod = findChoicesNumMethodReturning(processMethodContext, i, List.class);
            }
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
            FacetUtil.addFacet(new ActionParameterChoicesFacetViaMethod(choicesMethod, arrayOfParamType, paramAsHolder, getSpecificationLoader(), getAdapterManager()));
        }
    }

    private Method findChoicesNumMethodReturning(final ProcessMethodContext processMethodContext, final int i, final Class<?> arrayOfParamType) {

        final Class<?> cls = processMethodContext.getCls();
        final Method actionMethod = processMethodContext.getMethod();
        final String capitalizedName = NameUtils.capitalizeName(actionMethod.getName());
        final String name = MethodPrefixConstants.CHOICES_PREFIX + i + capitalizedName;
        return MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, name, arrayOfParamType, new Class[0]);
    }

    // ///////////////////////////////////////////////////////////////
    // Dependencies
    // ///////////////////////////////////////////////////////////////

    @Override
    public void setAdapterManager(final AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    private AdapterManager getAdapterManager() {
        return adapterManager;
    }

}
