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

package org.apache.isis.core.metamodel.facets.param.parameter.regex;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacetAbstract;

public class RegExFacetForParameterAnnotation extends RegExFacetAbstract {

    private final Pattern pattern;

    public static RegExFacet create(
            final List<Parameter> parameters,
            final Class<?> parameterType,
            final FacetHolder holder) {

        return parameters.stream()
                .filter(parameter -> _Strings.emptyToNull(parameter.regexPattern()) != null)
                .findFirst()
                .map(parameter -> {
                    final String pattern = parameter.regexPattern();
                    final String replacement = parameter.regexPatternReplacement();
                    final int patternFlags = parameter.regexPatternFlags();

                    return new RegExFacetForParameterAnnotation(pattern, patternFlags, replacement, holder);
                })
                .orElse(null);
    }

    private RegExFacetForParameterAnnotation(
            final String pattern,
            final int patternFlags,
            final String replacement,
            final FacetHolder holder) {
        super(pattern, patternFlags, replacement, holder);
        this.pattern = Pattern.compile(pattern, patternFlags);
    }

    @Override
    public boolean doesNotMatch(final String text) {
        return text == null || !pattern.matcher(text).matches();
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("pattern", pattern);
    }

}
