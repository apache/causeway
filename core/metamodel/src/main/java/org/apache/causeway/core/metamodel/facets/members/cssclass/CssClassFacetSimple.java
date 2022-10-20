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
package org.apache.causeway.core.metamodel.facets.members.cssclass;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.NonNull;

public abstract class CssClassFacetSimple
extends CssClassFacetAbstract
implements CssClassFacet {

    private final String cssClass;

    protected CssClassFacetSimple(final String cssClass, final FacetHolder holder) {
        this(cssClass, holder, Precedence.DEFAULT);
    }

    protected CssClassFacetSimple(final String cssClass, final FacetHolder holder, final Precedence precedence) {
        super(holder, precedence);
        this.cssClass = cssClass;
    }

    @Override
    public String cssClass(final ManagedObject objectAdapter) {
        return cssClass;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("cssClass", cssClass);
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof CssClassFacetSimple
                ? Objects.equals(this.cssClass, ((CssClassFacetSimple)other).cssClass)
                : false;
    }
}
