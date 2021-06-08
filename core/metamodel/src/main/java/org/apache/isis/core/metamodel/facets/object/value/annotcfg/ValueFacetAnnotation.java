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

package org.apache.isis.core.metamodel.facets.object.value.annotcfg;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderUtil;

import lombok.NonNull;

public class ValueFacetAnnotation extends ValueFacetAbstract {

    public ValueFacetAnnotation(
            final @NonNull Value value,
            final @NonNull IsisConfiguration config,
            final @NonNull Class<?> annotatedClass,
            final @NonNull FacetHolder holder) {

        super(ValueSemanticsProviderUtil
                .valueSemanticsProviderOrNull(
                        value.semanticsProviderClass(),
                        semanticsProviderName(value, config, annotatedClass)),
                AddFacetsIfInvalidStrategy.DO_ADD,
                holder);
    }

    // -- HELPER

    private static String semanticsProviderName(
            final Value value,
            final IsisConfiguration config,
            final Class<?> annotatedClass) {

        final String semanticsProviderName = value.semanticsProviderName();
        if (_Strings.isNotEmpty(semanticsProviderName)) {
            return semanticsProviderName;
        }
        return ValueSemanticsProviderUtil
                .semanticsProviderNameFromConfiguration(config, annotatedClass);
    }

}
