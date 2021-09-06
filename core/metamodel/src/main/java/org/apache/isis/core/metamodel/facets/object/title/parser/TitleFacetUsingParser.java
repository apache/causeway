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

package org.apache.isis.core.metamodel.facets.object.title.parser;

import java.util.function.BiConsumer;

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;

public final class TitleFacetUsingParser
extends FacetAbstract
implements TitleFacet {

    private final @NonNull Parser<?> parser;

    public static TitleFacetUsingParser create(final Parser<?> parser, final FacetHolder holder) {
        return new TitleFacetUsingParser(parser, holder);
    }

    private TitleFacetUsingParser(final Parser<?> parser, final FacetHolder holder) {
        super(TitleFacet.class, holder, Precedence.LOW);
        this.parser = parser;
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof TitleFacetUsingParser
                ? this.parser.getClass() == ((TitleFacetUsingParser)other).parser.getClass()
                : false;
    }

    @Override
    public String title(final ManagedObject adapter) {
        if (adapter == null) {
            return null;
        }
        final Object object = adapter.getPojo();
        if (object == null) {
            return null;
        }
        return parser.displayTitleOf(_Casts.uncheckedCast(object));
    }

    /**
     * not API
     */
    public String title(final ManagedObject adapter, final String usingMask) {
        if (adapter == null) {
            return null;
        }
        final Object object = adapter.getPojo();
        if (object == null) {
            return null;
        }
        return parser.displayTitleOf(_Casts.uncheckedCast(object), usingMask);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("parser", parser.toString());
    }


}
