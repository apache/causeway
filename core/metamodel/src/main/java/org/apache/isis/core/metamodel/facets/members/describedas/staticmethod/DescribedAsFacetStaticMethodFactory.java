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

package org.apache.isis.core.metamodel.facets.members.describedas.staticmethod;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.lang.MethodExtensions;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodPrefixConstants;

/**
 * Sets up a {@link DescribedAsFacet} if a
 * {@value MethodPrefixConstants#DESCRIPTION_PREFIX}-prefixed method is present.
 */
public class DescribedAsFacetStaticMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String[] PREFIXES = { MethodPrefixConstants.DESCRIPTION_PREFIX };

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public DescribedAsFacetStaticMethodFactory() {
        super(FeatureType.MEMBERS, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        attachDescribedAsFacetIfDescriptionMethodIsFound(processMethodContext);
    }

    public static void attachDescribedAsFacetIfDescriptionMethodIsFound(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseNameStripAccessorPrefixIfRequired(method.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Method descriptionMethod = MethodFinderUtils.findMethod(cls, MethodScope.CLASS, MethodPrefixConstants.DESCRIPTION_PREFIX + capitalizedName, String.class, new Class[0]);
        if (descriptionMethod == null) {
            return;
        }

        processMethodContext.removeMethod(descriptionMethod);
        final String description = invokeDescriptionMethod(descriptionMethod);

        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new DescribedAsFacetStaticMethod(description, descriptionMethod, facetedMethod));
    }

    private static String invokeDescriptionMethod(final Method descriptionMethod) {
        String description = null;
        try {
            description = (String) MethodExtensions.invokeStatic(descriptionMethod);
        } catch (final ClassCastException ex) {
            // ignore
        }
        if (description == null) {
            throw new MetaModelException("method " + descriptionMethod + "must return a string");
        }
        return description;
    }

}
