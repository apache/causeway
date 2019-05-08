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

package org.apache.isis.core.metamodel.facets.param.hide.method;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodPrefixConstants;
import org.apache.isis.core.metamodel.facets.param.hide.ActionParameterHiddenFacet;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;

/**
 * Sets up {@link ActionParameterHiddenFacet}.
 */
public class ActionParameterHiddenFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract  {

    private static final String[] PREFIXES = { MethodPrefixConstants.HIDE_PREFIX };

    public ActionParameterHiddenFacetViaMethodFactory() {
        super(FeatureType.PARAMETERS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }


    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {

        final Class<?> cls = processParameterContext.getCls();
        final Method actionMethod = processParameterContext.getMethod();
        final int param = processParameterContext.getParamNum();
        final IdentifiedHolder facetHolder = processParameterContext.getFacetHolder();

        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());
        final List<Class<?>> paramTypes = ListExtensions.mutableCopy(actionMethod.getParameterTypes());
        final MethodScope onClass = MethodScope.scopeFor(actionMethod);

        final String hideName = MethodPrefixConstants.HIDE_PREFIX + param + capitalizedName;

        final int numParamTypes = paramTypes.size();

        for(int i=0; i< numParamTypes+1; i++) {
            final Method hideMethod = MethodFinderUtils.findMethod(
                    cls, onClass,
                    hideName,
                    new Class<?>[]{boolean.class},
                    paramTypes.toArray(new Class<?>[]{}));

            if (hideMethod != null) {
                processParameterContext.removeMethod(hideMethod);

                final Facet facet = new ActionParameterHiddenFacetViaMethod(hideMethod, facetHolder);
                FacetUtil.addFacet(facet);
                return;
            }

            // remove last, and search again
            if(!paramTypes.isEmpty()) {
                paramTypes.remove(paramTypes.size()-1);
            }
        }

    }

    PersistenceSessionServiceInternal adapterManager;

}
