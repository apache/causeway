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

package org.apache.isis.core.metamodel.facets.object.parseable;

import com.google.common.base.Strings;

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.lang.ClassUtil;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public final class ParserUtil {

    private ParserUtil() {
    }

    public static final String PARSER_NAME_KEY_PREFIX = "isis.reflector.java.facets.parser.";
    public static final String PARSER_NAME_KEY_SUFFIX = ".parserName";

    public static String parserNameFromConfiguration(final Class<?> type, final IsisConfiguration configuration) {
        final String key = PARSER_NAME_KEY_PREFIX + type.getCanonicalName() + PARSER_NAME_KEY_SUFFIX;
        final String parserName = configuration.getString(key);
        return !Strings.isNullOrEmpty(parserName) ? parserName : null;
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Parser<?>> parserOrNull(final Class<?> candidateClass, final String classCandidateName) {
        @SuppressWarnings("rawtypes")
        final Class type = candidateClass != null ? ClassUtil.implementingClassOrNull(candidateClass.getName(), Parser.class, FacetHolder.class) : null;
        return type != null ? type : (Class)ClassUtil.implementingClassOrNull(classCandidateName, Parser.class, FacetHolder.class);
    }

}
