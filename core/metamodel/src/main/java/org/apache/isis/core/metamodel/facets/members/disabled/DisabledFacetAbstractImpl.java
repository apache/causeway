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
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public abstract class DisabledFacetAbstractImpl
extends DisabledFacetAbstract {

    private final String reason;

    public DisabledFacetAbstractImpl(final Where where, final FacetHolder holder) {
        this(where, null, holder);
    }

    public DisabledFacetAbstractImpl(
            final Where where,
            final FacetHolder holder,
            final Semantics semantics) {
        this(where, null, holder, semantics);
    }

    public DisabledFacetAbstractImpl(
            final Where where,
            final String reason,
            final FacetHolder holder) {
        super(where, holder);
        this.reason = reason;
    }

    public DisabledFacetAbstractImpl(
            final Where where,
            final String reason,
            final FacetHolder holder,
            final Semantics semantics) {
        super(where, holder, semantics);
        this.reason = reason;
    }

    @Override
    public String disabledReason(final ManagedObject targetAdapter) {
        return disabledReasonElse(ALWAYS_DISABLED_REASON);

    }

    private String disabledReasonElse(final String defaultReason) {
        return !_Strings.isNullOrEmpty(reason)
                ? reason
                : defaultReason;
    }

    /**
     * Not API... the reason as defined in subclass
     */
    public String getReason() {
        return reason;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("reason", reason);
    }
}
