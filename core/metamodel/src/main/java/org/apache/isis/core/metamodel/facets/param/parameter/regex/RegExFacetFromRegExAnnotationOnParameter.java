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

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacetAbstract;

/**
 * @deprecated
 */
@Deprecated
public class RegExFacetFromRegExAnnotationOnParameter extends RegExFacetAbstract {

    private final Pattern pattern;

    public static RegExFacet create(final RegEx annotation, final Class<?> parameterType, final FacetHolder holder) {
        if (annotation == null) {
            return null;
        }
        if(!Annotations.isString(parameterType)) {
            return null;
        }

        final String validationExpression = annotation.validation();
        final boolean caseSensitive = annotation.caseSensitive();
        final String formatExpression = annotation.format();
        final String replacement = "Doesn't match pattern";

        return new RegExFacetFromRegExAnnotationOnParameter(validationExpression, formatExpression, caseSensitive, holder, replacement);
    }

    private RegExFacetFromRegExAnnotationOnParameter(final String validation, final String format, final boolean caseSensitive, final FacetHolder holder, final String replacement) {
        super(validation, format, caseSensitive, holder, replacement);
        pattern = Pattern.compile(validation(), patternFlags());
    }

    @Override
    public String format(final String text) {
        if (text == null) {
            return "<not a string>";
        }
        if (format() == null || format().length() == 0) {
            return text;
        }
        return pattern.matcher(text).replaceAll(format());
    }

    @Override
    public boolean doesNotMatch(final String text) {
        if (text == null) {
            return true;
        }
        return !pattern.matcher(text).matches();
    }

    private int patternFlags() {
        return !caseSensitive() ? Pattern.CASE_INSENSITIVE : 0;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("pattern", pattern);
    }

}
