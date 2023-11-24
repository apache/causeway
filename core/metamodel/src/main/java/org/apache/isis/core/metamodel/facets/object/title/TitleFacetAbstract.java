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
package org.apache.isis.core.metamodel.facets.object.title;

import java.util.Objects;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;

import lombok.NonNull;
import lombok.val;

public abstract class TitleFacetAbstract
extends FacetAbstract
implements TitleFacet {

    private static final Class<? extends Facet> type() {
        return TitleFacet.class;
    }

    public TitleFacetAbstract(final FacetHolder holder) {
        super(type(), holder);
    }

    public TitleFacetAbstract(final FacetHolder holder, final Facet.Precedence precedence) {
        super(type(), holder, precedence);
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {

        // equality by facet-type and java-methods

        if(!this.facetType().equals(other.facetType())) {
            return false;
        }

        val otherFacet = (TitleFacet)other;

        if(Objects.equals(this, otherFacet)) {
            return true;
        }

        if(this instanceof ImperativeFacet
                && otherFacet instanceof ImperativeFacet) {

            return ((ImperativeFacet)this)
                    .getMethods()
                    .equals(((ImperativeFacet)otherFacet).getMethods());
        }

        return false;
    }

}
