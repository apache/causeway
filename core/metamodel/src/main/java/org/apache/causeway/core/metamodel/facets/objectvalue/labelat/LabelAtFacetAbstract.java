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
package org.apache.causeway.core.metamodel.facets.objectvalue.labelat;

import java.util.function.BiConsumer;

import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import org.jspecify.annotations.NonNull;

public abstract class LabelAtFacetAbstract
extends FacetAbstract
implements LabelAtFacet {

    public static final Class<LabelAtFacet> type() {
        return LabelAtFacet.class;
    }

    private final LabelPosition value;

    public LabelAtFacetAbstract(final LabelPosition value, final FacetHolder holder) {
        this(value, holder, Precedence.DEFAULT);
    }

    public LabelAtFacetAbstract(final LabelPosition value, final FacetHolder holder, final Precedence precedence) {
        super(type(), holder, precedence);
        this.value = value;
    }

    @Override
    public LabelPosition label() {
        return value;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("labelPosition", value);
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof LabelAtFacetAbstract
                && this.label() == ((LabelAtFacetAbstract) other).label();
    }

}
