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

package org.apache.isis.core.metamodel.facets.collections.layout;

import java.util.Properties;
import com.google.common.base.Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetAbstract;

public class NamedFacetOnCollectionFromLayoutProperties extends NamedFacetAbstract {

    public static NamedFacet create(Properties properties, FacetHolder holder) {
        final String named = named(properties);
        boolean namedEscaped = namedEscaped(properties);
        return named != null? new NamedFacetOnCollectionFromLayoutProperties(named, namedEscaped, holder): null;
    }

    private NamedFacetOnCollectionFromLayoutProperties(
        final String named,
        final boolean escaped,
        final FacetHolder holder) {

        super(named, escaped, holder);
    }

    private static String named(Properties properties) {
        if(properties == null) {
            return null;
        }
        String named = Strings.emptyToNull(properties.getProperty("named"));
        if(named == null) {
            // alternate key
            named = Strings.emptyToNull(properties.getProperty("name"));
        }
        return named;
    }

    private static boolean namedEscaped(final Properties properties) {
        boolean escaped = true;
        if(properties != null) {
            String namedEscapedValue = Strings.emptyToNull(properties.getProperty("namedEscaped"));
            if("false".equalsIgnoreCase(namedEscapedValue)) {
                escaped = false;
            }
        }
        return escaped;
    }
}
