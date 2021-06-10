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

package org.apache.isis.core.metamodel.facets.members.cssclass;

import java.util.Map;
import java.util.Objects;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;

public abstract class CssClassFacetAbstract
extends FacetAbstract
implements CssClassFacet {

    public static Class<? extends Facet> type() {
        return CssClassFacet.class;
    }

    private final String cssClass;

    public CssClassFacetAbstract(final String cssClass, final FacetHolder holder) {
        super(type(), holder);
        this.cssClass = cssClass;
    }

    @Override
    public String cssClass(final ManagedObject objectAdapter) {
        return cssClass;
    }

    @Override
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("cssClass", cssClass);
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof CssClassFacetAbstract
                ? Objects.equals(this.cssClass, ((CssClassFacetAbstract)other).cssClass)
                : false;
    }
}
