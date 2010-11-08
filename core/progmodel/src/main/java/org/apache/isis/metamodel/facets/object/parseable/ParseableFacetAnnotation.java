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


package org.apache.isis.metamodel.facets.object.parseable;

import org.apache.isis.applib.annotation.Parseable;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;


public class ParseableFacetAnnotation extends ParseableFacetAbstract {

    private static String parserName(final Class<?> annotatedClass, final IsisConfiguration configuration) {
        final Parseable annotation = (Parseable) annotatedClass.getAnnotation(Parseable.class);
        final String parserName = annotation.parserName();
        if (!StringUtils.isEmpty(parserName)) {
            return parserName;
        }
        return ParserUtil.parserNameFromConfiguration(annotatedClass, configuration);
    }

    private static Class<?> parserClass(final Class<?> annotatedClass) {
        final Parseable annotation = (Parseable) annotatedClass.getAnnotation(Parseable.class);
        return annotation.parserClass();
    }

    public ParseableFacetAnnotation(
            final Class<?> annotatedClass,
            final IsisConfiguration configuration,
            final FacetHolder holder, 
            final RuntimeContext runtimeContext) {
        this(parserName(annotatedClass, configuration), parserClass(annotatedClass), holder, runtimeContext);
    }

    private ParseableFacetAnnotation(
    		final String candidateParserName, 
    		final Class<?> candidateParserClass, 
    		final FacetHolder holder, 
    		final RuntimeContext runtimeContext) {
        super(candidateParserName, candidateParserClass, holder, runtimeContext);
    }

}

