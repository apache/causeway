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


package org.apache.isis.metamodel.facets.object.ident.plural;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.metamodel.util.InvokeUtils;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.FacetUtil;
import org.apache.isis.metamodel.facets.MethodRemover;
import org.apache.isis.metamodel.java5.MethodPrefixBasedFacetFactoryAbstract;


public class PluralMethodFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String PLURAL_NAME = "pluralName";

    private static final String[] PREFIXES = { PLURAL_NAME, };

    public PluralMethodFacetFactory() {
        super(PREFIXES, ObjectFeatureType.OBJECTS_ONLY);
    }

    @Override
    public boolean process(final Class<?> type, final MethodRemover methodRemover, final FacetHolder facetHolder) {
        final Method method = findMethod(type, CLASS, PLURAL_NAME, String.class, NO_PARAMETERS_TYPES);
        if (method != null) {
            final String name = (String) InvokeUtils.invokeStatic(method);
            methodRemover.removeMethod(method);
            return FacetUtil.addFacet(new PluralFacetViaMethod(name, facetHolder));
        } else {
            return false;
        }
    }
}
