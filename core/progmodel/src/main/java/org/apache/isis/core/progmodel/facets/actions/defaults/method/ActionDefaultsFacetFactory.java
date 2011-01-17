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

package org.apache.isis.core.progmodel.facets.actions.defaults.method;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.commons.lang.NameUtils;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.adapter.map.AdapterMapAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.progmodel.facets.MethodFinderUtils;
import org.apache.isis.core.progmodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.progmodel.facets.MethodPrefixConstants;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class ActionDefaultsFacetFactory extends MethodPrefixBasedFacetFactoryAbstract implements AdapterMapAware {


    private static final String[] PREFIXES = { MethodPrefixConstants.DEFAULT_PREFIX };

    private AdapterMap adapterMap;

    /**
     * Note that the {@link Facet}s registered are the generic ones from noa-architecture (where they exist)
     */
    public ActionDefaultsFacetFactory() {
        super(FeatureType.ACTIONS_ONLY, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(ProcessMethodContext processMethodContext) {

        attachActionDefaultsFacetIfParameterDefaultsMethodIsFound(processMethodContext);
    }


    private static void attachActionDefaultsFacetIfParameterDefaultsMethodIsFound(
        final ProcessMethodContext processMethodContext) {

        Method defaultsMethod =
                findDefaultsMethodReturning(processMethodContext, Object[].class);
        if (defaultsMethod == null) {
            defaultsMethod =
                findDefaultsMethodReturning(processMethodContext, List.class);
        }
        if (defaultsMethod == null) {
            return;
        }
        
        processMethodContext.removeMethod(defaultsMethod);

        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        final ActionDefaultsFacetViaMethod facet = new ActionDefaultsFacetViaMethod(defaultsMethod, facetedMethod);
        FacetUtil.addFacet(facet);
    }

    private static Method findDefaultsMethodReturning(final ProcessMethodContext processMethodContext, final Class<?> returnType) {

        final Method actionMethod = processMethodContext.getMethod();
        final String capitalizedName = NameUtils.capitalizeName(actionMethod.getName());
        String name = MethodPrefixConstants.DEFAULT_PREFIX + capitalizedName;

        Class<?> cls = processMethodContext.getCls();
        return MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, name, returnType, new Class[0]);
    }

    // ///////////////////////////////////////////////////////////////
    // Dependencies
    // ///////////////////////////////////////////////////////////////

    @Override
    public void setAdapterMap(AdapterMap adapterMap) {
        this.adapterMap = adapterMap;
    }

    private AdapterMap getAdapterMap() {
        return adapterMap;
    }

}
