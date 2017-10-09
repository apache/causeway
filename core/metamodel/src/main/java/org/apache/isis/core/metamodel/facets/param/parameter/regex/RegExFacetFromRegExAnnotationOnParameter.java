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

import java.util.regex.Pattern;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacetAbstract;

public class RegExFacetFromRegExAnnotationOnParameter extends RegExFacetAbstract {

    private final Pattern pattern;

    public static RegExFacet create(final javax.validation.constraints.Pattern annotation, final Class<?> parameterType, final FacetHolder holder) {
        if (annotation == null) {
            return null;
        }
        if(!Annotations.isString(parameterType)) {
            return null;
        }

        final String regexp = annotation.regexp();
        final javax.validation.constraints.Pattern.Flag[] flags = annotation.flags();
        final String message = annotation.message();

        return new RegExFacetFromRegExAnnotationOnParameter(regexp, flags, message, holder);
    }

    private RegExFacetFromRegExAnnotationOnParameter(
            final String regexp,
            final javax.validation.constraints.Pattern.Flag[] flags,
            final String message, final FacetHolder holder) {
        super(regexp, flags, message, holder);
        pattern = Pattern.compile(regexp(), patternFlags());
    }

    @Override
    public boolean doesNotMatch(final String text) {
        return text == null || !pattern.matcher(text).matches();
    }


}
