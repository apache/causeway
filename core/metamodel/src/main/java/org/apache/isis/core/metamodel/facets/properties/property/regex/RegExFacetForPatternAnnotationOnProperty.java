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

package org.apache.isis.core.metamodel.facets.properties.property.regex;

import java.util.Optional;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacetAbstract;

public class RegExFacetForPatternAnnotationOnProperty
extends RegExFacetAbstract {

    public static Optional<RegExFacet> create(
            final Optional<javax.validation.constraints.Pattern> patternIfAny,
            final Class<?> returnType,
            final FacetHolder holder) {

        if (!returnType.equals(String.class)) {
            return null;
        }

        return patternIfAny
                .filter(pattern -> _Strings.emptyToNull(pattern.regexp()) != null)
                .map(pattern ->
                    new RegExFacetForPatternAnnotationOnProperty(
                        pattern.regexp(), pattern.flags(), pattern.message(), holder));
    }

    private RegExFacetForPatternAnnotationOnProperty(
            final String regexp,
            final javax.validation.constraints.Pattern.Flag[] flags,
            final String replacement,
            final FacetHolder holder) {
        super(regexp, asMask(flags), replacement, holder);
    }

}
