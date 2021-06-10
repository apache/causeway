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

package org.apache.isis.core.metamodel.facets.all.named;

import java.util.Map;
import java.util.Objects;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.NonNull;
import lombok.val;

public abstract class NamedFacetAbstract
extends FacetAbstract
implements NamedFacet {

    private final String value;
    private final boolean escaped;

    public static Class<? extends Facet> type() {
        return NamedFacet.class;
    }

    public NamedFacetAbstract(String value, boolean escaped, FacetHolder holder) {
        super(type(), holder);
        this.value = value;
        this.escaped = escaped;
    }

    public NamedFacetAbstract(String value, boolean escaped, FacetHolder holder, final Facet.Precedence precedence) {
        super(type(), holder, precedence);
        this.value = value;
        this.escaped = escaped;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public boolean escaped() {
        return escaped;
    }

    @Override
    public void appendAttributesTo(Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("value", value);
        attributeMap.put("escaped", escaped);
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        if(! (other instanceof NamedFacetAbstract)) {
            return false;
        }

        val otherNamedFacet =  (NamedFacetAbstract)other;

        return this.escaped() == otherNamedFacet.escaped()
                && Objects.equals(this.value(), otherNamedFacet.value());
    }

}
