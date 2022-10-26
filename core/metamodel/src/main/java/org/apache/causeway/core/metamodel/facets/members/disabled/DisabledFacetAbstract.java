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
package org.apache.causeway.core.metamodel.facets.members.disabled;

import java.util.function.BiConsumer;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.WhereValueFacetAbstract;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.Getter;
import lombok.NonNull;

public abstract class DisabledFacetAbstract
extends WhereValueFacetAbstract
implements DisabledFacet {

    private static final Class<? extends Facet> type() {
        return DisabledFacet.class;
    }

    @Getter(onMethod_ = {@Override}) private final @NonNull Semantics semantics;

    /**
     * @apiNote this field is used for reporting purposes only (eg. MM export),
     * when either sub-classes override {@link #disabledReason(ManagedObject)}
     * or the semantics is inverted (ENABLED)
     */
    private final @Nullable String reason;

    protected DisabledFacetAbstract(
            final Where where,
            final String reason,
            final FacetHolder holder) {
        this(where, reason, holder, Semantics.DISABLED, Precedence.DEFAULT);
    }

    protected DisabledFacetAbstract(
            final Where where,
            final String reason,
            final FacetHolder holder,
            final Semantics semantics,
            final Precedence precedence) {
        super(type(), holder, where, precedence);
        this.reason = reason;
        this.semantics = semantics;
    }

    @Override
    public String disabledReason(final ManagedObject targetAdapter) {
        if(getSemantics().isEnabled()) {
            return null;
        }
        return _Strings.isNotEmpty(reason)
                ? reason
                : ALWAYS_DISABLED_REASON;
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
    public final void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("semantics", semantics);
        if(_Strings.isNotEmpty(reason)) {
            visitor.accept("reason", reason);
        }
    }

}
