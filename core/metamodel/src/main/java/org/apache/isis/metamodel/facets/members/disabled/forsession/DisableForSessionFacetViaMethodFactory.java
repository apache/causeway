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

package org.apache.isis.metamodel.facets.members.disabled.forsession;

import java.lang.reflect.Method;

import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.facets.MethodPrefixConstants;
import org.apache.isis.metamodel.methodutils.MethodScope;
import org.apache.isis.security.authentication.AuthenticationSessionProvider;

public class DisableForSessionFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String[] PREFIXES = { MethodPrefixConstants.DISABLE_PREFIX };


    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public DisableForSessionFacetViaMethodFactory() {
        super(FeatureType.MEMBERS, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        attachDisableFacetIfDisableMethodForSessionIsFound(processMethodContext, getAuthenticationSessionProvider());
    }

    public static void attachDisableFacetIfDisableMethodForSessionIsFound(
            final ProcessMethodContext processMethodContext,
            final AuthenticationSessionProvider authenticationSessionProvider) {

        final Method method = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseNameStripAccessorPrefixIfRequired(method.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Method disableForSessionMethod = MethodFinderUtils.findMethod(cls, MethodScope.CLASS, MethodPrefixConstants.DISABLE_PREFIX + capitalizedName, String.class, new Class[] { UserMemento.class });

        if (disableForSessionMethod == null) {
            return;
        }

        processMethodContext.removeMethod(disableForSessionMethod);

        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new DisableForSessionFacetViaMethod(disableForSessionMethod, facetedMethod));
    }

}
