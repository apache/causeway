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

import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacetAbstract;

public class RegExFacetForPropertyAnnotation extends RegExFacetAbstract {

    private final Pattern pattern;

    public static RegExFacet create(
            final Property annotation,
            final Class<?> returnType,
            final FacetHolder holder) {

        if (annotation == null) {
            return null;
        }

        if (!Annotations.isString(returnType)) {
            return null;
        }

        final String pattern = annotation.regexPattern();
        if(Strings.isNullOrEmpty(pattern)) {
            return null;
        }

        final String replacement = annotation.regexPatternReplacement();
        final int patternFlags = annotation.regexPatternFlags();

        return new RegExFacetForPropertyAnnotation(pattern, patternFlags, holder, replacement);
    }

    private RegExFacetForPropertyAnnotation(final String pattern, final int patternFlags, final FacetHolder holder, final String replacement) {
        super(pattern, "", (patternFlags & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE, holder, replacement);
        this.pattern = Pattern.compile(pattern, patternFlags);
    }

    /**
     * Unused (for the TitledFacet)
     */
    @Override
    public String format(String text) {
        return text;
    }

    @Override
    public boolean doesNotMatch(final String text) {
        if (text == null) {
            return true;
        }
        return !pattern.matcher(text).matches();
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("pattern", pattern);
    }

}
