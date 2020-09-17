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

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.metamodel.commons.ClassUtil;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

public final class ParserUtil {

    private ParserUtil() {
    }

    public static Class<? extends Parser<?>> parserOrNull(final Class<?> candidateClass, final String classCandidateName) {

        final Class<? extends Parser<?>> type = candidateClass != null 
                ? uncheckedCast(ClassUtil.implementingClassOrNull(
                        candidateClass.getName(), Parser.class, FacetHolder.class))
                        : null;

                return type != null 
                        ? type 
                                : uncheckedCast(ClassUtil.implementingClassOrNull(
                                        classCandidateName, Parser.class, FacetHolder.class));
    }

}
