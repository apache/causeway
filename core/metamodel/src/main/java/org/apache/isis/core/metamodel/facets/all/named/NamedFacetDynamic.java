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

import java.util.function.BiConsumer;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.i8n.I8nFacetAbstract;
import org.apache.isis.core.metamodel.facets.all.i8n.NounForms;

import lombok.NonNull;
import lombok.val;

//TODO[1720] just a stub yet
public abstract class NamedFacetDynamic
extends I8nFacetAbstract
implements NamedFacet {

    private static final Class<? extends Facet> type() {
        return NamedFacet.class;
    }

    protected NamedFacetDynamic(
            final FacetHolder holder) {
        super(type(), NounForms.preferredSingular("TODO").build(), holder, Precedence.HIGH);
    }

    @Override
    public boolean escaped() {
        return true; // dynamic names are always escaped
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("escaped", escaped());
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        if(! (other instanceof NamedFacetDynamic)) {
            return false;
        }

        val otherNamedFacet = (NamedFacetDynamic)other;

        return this.escaped() == otherNamedFacet.escaped()
                && super.semanticEquals(otherNamedFacet);
    }

}
