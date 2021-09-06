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
package org.apache.isis.core.metamodel.facets.object.defaults.annotcfg;

import java.util.Optional;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.annotation.Defaulted;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultsProviderUtil;

public class DefaultedFacetAnnotation
extends DefaultedFacetAbstract {

    public static Optional<DefaultedFacet> create(
            final IsisConfiguration config,
            final Class<?> annotatedClass,
            final FacetHolder holder) {

        return DefaultsProviderUtil.providerFrom(
                providerName(config, annotatedClass),
                providerClass(annotatedClass),
                holder)
        .map(defaultsProvider->new DefaultedFacetAnnotation(defaultsProvider, holder));
    }

    // -- CONSTRUCTOR

    private DefaultedFacetAnnotation(
            final DefaultsProvider<?> defaultsProvider,
            final FacetHolder holder) {
        super(defaultsProvider, holder);
    }

    // -- HELPER

    private static String providerName(final IsisConfiguration config, final Class<?> annotatedClass) {

        final Defaulted annotation = annotatedClass.getAnnotation(Defaulted.class);
        final String providerName = annotation.defaultsProviderName();
        if (!_Strings.isNullOrEmpty(providerName)) {
            return providerName;
        }
        return DefaultsProviderUtil.defaultsProviderNameFromConfiguration(config, annotatedClass);
    }

    private static Class<?> providerClass(final Class<?> annotatedClass) {
        final Defaulted annotation = annotatedClass.getAnnotation(Defaulted.class);
        return annotation.defaultsProviderClass();
    }

}
