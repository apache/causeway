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

import org.apache.isis.applib.annotation.Defaulted;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultsProviderUtil;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class DefaultedFacetAnnotation extends DefaultedFacetAbstract {

    private static String providerName(final Class<?> annotatedClass, final IsisConfiguration configuration) {
        final Defaulted annotation = annotatedClass.getAnnotation(Defaulted.class);
        final String providerName = annotation.defaultsProviderName();
        if (!_Strings.isNullOrEmpty(providerName)) {
            return providerName;
        }
        return DefaultsProviderUtil.defaultsProviderNameFromConfiguration(annotatedClass, configuration);
    }

    private static Class<?> providerClass(final Class<?> annotatedClass) {
        final Defaulted annotation = annotatedClass.getAnnotation(Defaulted.class);
        return annotation.defaultsProviderClass();
    }

    public DefaultedFacetAnnotation(final Class<?> annotatedClass, final IsisConfiguration configuration, final FacetHolder holder, final ServicesInjector dependencyInjector) {
        this(providerName(annotatedClass, configuration), providerClass(annotatedClass), holder, dependencyInjector);
    }

    private DefaultedFacetAnnotation(final String candidateProviderName, final Class<?> candidateProviderClass, final FacetHolder holder, final ServicesInjector dependencyInjector) {
        super(candidateProviderName, candidateProviderClass, holder, dependencyInjector);
    }

}
