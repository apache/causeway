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

package org.apache.isis.core.metamodel.facets.members.disabled;

import java.util.function.BiConsumer;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.WhereValueFacetAbstract;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Getter;
import lombok.NonNull;

public abstract class DisabledFacetAbstract
extends WhereValueFacetAbstract
implements DisabledFacet {

    private static final Class<? extends Facet> type() {
        return DisabledFacet.class;
    }

    @Getter(onMethod_ = {@Override}) private final @NonNull Semantics semantics;

    protected DisabledFacetAbstract(Where where, final FacetHolder holder) {
        this(where, holder, Semantics.DISABLED, Precedence.DEFAULT);
    }

    protected DisabledFacetAbstract(
            final Where where,
            final FacetHolder holder,
            final Semantics semantics,
            Precedence precedence) {
        super(type(), holder, where, precedence);
        this.semantics = semantics;
    }

    @Override
    public String disables(final UsabilityContext ic) {
        if(getSemantics().isEnabled()) {
            return null;
        }
        final ManagedObject target = ic.getTarget();
        final String disabledReason = disabledReason(target);
        return disabledReason;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("semantics", semantics);
    }

}
