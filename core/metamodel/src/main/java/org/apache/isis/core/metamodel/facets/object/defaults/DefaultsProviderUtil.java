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

package org.apache.isis.core.metamodel.facets.object.defaults;

import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.commons.ClassUtil;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class DefaultsProviderUtil {

    public static final String DEFAULTS_PROVIDER_NAME_KEY_PREFIX = "isis.reflector.java.facets.defaulted.";
    public static final String DEFAULTS_PROVIDER_NAME_KEY_SUFFIX = ".providerName";

    public static String defaultsProviderNameFromConfiguration(
            final IsisConfiguration configuration,
            final Class<?> type) {

        val key = DEFAULTS_PROVIDER_NAME_KEY_PREFIX +
                type.getCanonicalName() +
                DEFAULTS_PROVIDER_NAME_KEY_SUFFIX;

        val defaultsProviderName = configuration
                .getEnvironment()
                .getProperty(key);

        return !_Strings.isNullOrEmpty(defaultsProviderName)
                ? defaultsProviderName
                : null;
    }

    public static Class<?> defaultsProviderOrNull(
            final Class<?> candidateClass,
            final String classCandidateName) {

        val type = candidateClass != null
                ? ClassUtil.implementingClassOrNull(
                        candidateClass.getName(),
                        DefaultsProvider.class,
                        FacetHolder.class)

                        : null;

        return type != null
                ? type
                        : ClassUtil.implementingClassOrNull(
                                classCandidateName,
                                DefaultsProvider.class,
                                FacetHolder.class);
    }

    public static Optional<DefaultsProvider<?>> providerFrom(
            final @Nullable String candidateEncoderDecoderName,
            final @Nullable Class<?> candidateEncoderDecoderClass,
            final @NonNull FacetHolder holder) {

        val defaultsProviderClass = DefaultsProviderUtil
                .defaultsProviderOrNull(candidateEncoderDecoderClass, candidateEncoderDecoderName);

        val defaultsProvider = defaultsProviderClass!=null
                ? (DefaultsProvider<?>) ClassExtensions
                        .newInstance(defaultsProviderClass, FacetHolder.class, holder)
                : null;

        return Optional.ofNullable(defaultsProvider)
                .map(holder.getServiceInjector()::injectServicesInto);

    }

}
