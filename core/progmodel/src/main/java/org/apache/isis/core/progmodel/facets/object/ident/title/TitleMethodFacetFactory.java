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


package org.apache.isis.core.progmodel.facets.object.ident.title;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.lang.JavaClassUtils;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.progmodel.java5.FallbackFacetFactory;
import org.apache.isis.core.progmodel.java5.MethodPrefixBasedFacetFactoryAbstract;


public class TitleMethodFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String TO_STRING = "toString";
    private static final String TITLE = "title";

    private static final String[] PREFIXES = { TO_STRING, TITLE, };

    public TitleMethodFacetFactory() {
        super(PREFIXES, ObjectFeatureType.OBJECTS_ONLY);
    }

    /**
     * If no title or toString can be used then will use Facets provided by {@link FallbackFacetFactory}
     * instead.
     */
    @Override
    public boolean process(final Class<?> type, final MethodRemover methodRemover, final FacetHolder facetHolder) {

        Method method = findMethod(type, OBJECT, TITLE, String.class, null);
        if (method != null) {
            methodRemover.removeMethod(method);
            return FacetUtil.addFacet(new TitleFacetViaTitleMethod(method, facetHolder));
        }

        try {
            method = findMethod(type, OBJECT, TO_STRING, String.class, null);
            if (method == null) {
                return false;
            }
            if (JavaClassUtils.isJavaClass(method.getDeclaringClass())) {
                return false;
            }
            methodRemover.removeMethod(method);
            FacetUtil.addFacet(new TitleFacetViaToStringMethod(method, facetHolder));
            return false;

        } catch (final Exception e) {
            return false;
        }
    }
}
