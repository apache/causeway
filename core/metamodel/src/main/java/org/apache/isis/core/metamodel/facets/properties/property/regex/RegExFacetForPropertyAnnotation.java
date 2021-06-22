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

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacetAbstract;

public class RegExFacetForPropertyAnnotation
extends RegExFacetAbstract {

    public static Optional<RegExFacet> create(
            final Optional<Property> propertyIfAny,
            final Class<?> returnType,
            final FacetHolder holder) {

        if (!returnType.equals(String.class)) {
            return null;
        }

        return propertyIfAny
                .filter(property -> _Strings.emptyToNull(property.regexPattern()) != null)
                .map(property -> new RegExFacetForPropertyAnnotation(
                        property.regexPattern(), property.regexPatternFlags(), holder,
                        property.regexPatternReplacement()));

    }

    private RegExFacetForPropertyAnnotation(
            final String pattern,
            final int patternFlags,
            final FacetHolder holder,
            final String replacement) {

        super(pattern, patternFlags, replacement, holder);
    }

}
