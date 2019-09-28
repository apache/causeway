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

package org.apache.isis.metamodel.facets.members.hidden.method;

import java.lang.reflect.Method;

import org.apache.isis.metamodel.commons.StringExtensions;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.metamodel.methodutils.MethodScope;

public class HideForContextFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {


    private static final String[] PREFIXES = { MethodLiteralConstants.HIDE_PREFIX };

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public HideForContextFacetViaMethodFactory() {
        super(FeatureType.MEMBERS, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        attachHideFacetIfHideMethodIsFound(processMethodContext);
    }

    private void attachHideFacetIfHideMethodIsFound(final ProcessMethodContext processMethodContext) {

        final Method getMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseNameStripAccessorPrefixIfRequired(getMethod.getName());

        final Class<?> cls = processMethodContext.getCls();
        Method hideMethod = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, MethodLiteralConstants.HIDE_PREFIX + capitalizedName, boolean.class, new Class[] {});
        if (hideMethod == null) {

            boolean noParamsOnly = getConfiguration().getReflector().getValidator().isNoParamsOnly();
            boolean searchExactMatch = !noParamsOnly;
            if(searchExactMatch) {
                hideMethod = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, MethodLiteralConstants.HIDE_PREFIX + capitalizedName, boolean.class, getMethod.getParameterTypes());
            }
        }

        if (hideMethod == null) {
            return;
        }

        processMethodContext.removeMethod(hideMethod);

        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new HideForContextFacetViaMethod(hideMethod, facetedMethod));
    }

}
