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
package org.apache.causeway.core.metamodel.facets;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import org.jspecify.annotations.NonNull;

public abstract class SingleStringValueFacetAbstract
extends FacetAbstract
implements SingleStringValueFacet {

    private final String value;

    public SingleStringValueFacetAbstract(
            final Class<? extends Facet> type, final FacetHolder holder, final String value) {
        super(type, holder);
        this.value = value;
    }

    public SingleStringValueFacetAbstract(
            final Class<? extends Facet> type, final FacetHolder holder, final String value, final Facet.Precedence precedence) {
        super(type, holder, precedence);
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("value", value);
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof SingleStringValueFacet
                ? Objects.equals(this.value(), ((SingleStringValueFacet)other).value())
                : false;
    }

}
