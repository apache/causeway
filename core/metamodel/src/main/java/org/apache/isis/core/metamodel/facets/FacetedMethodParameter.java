/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Method;

import com.google.common.base.Function;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;

public class FacetedMethodParameter extends TypedHolderDefault implements IdentifiedHolder {

    private final Identifier identifier;

    public FacetedMethodParameter(final Class<?> declaringType, final Method method, final Class<?> type) {
        super(FeatureType.ACTION_PARAMETER, type);

        // best we can do...
        this.identifier = FeatureType.ACTION.identifierFor(declaringType, method);
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }


    public static class Functions {
        public static final Function<FacetedMethodParameter, String> GET_NAME = new Function<FacetedMethodParameter, String>() {
            @Override public String apply(final FacetedMethodParameter input) {
                final NamedFacet namedFacet = input.getFacet(NamedFacet.class);
                return namedFacet.value();
            }
        };
        public static final Function<FacetedMethodParameter, Class<?>> GET_TYPE = new Function<FacetedMethodParameter, Class<?>>() {
            @Override public Class<?> apply(final FacetedMethodParameter input) {
                return input.getType();
            }
        };

        private Functions(){}

    }

}
