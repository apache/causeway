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
package org.apache.isis.applib.annotation;

import org.apache.isis.applib.util.Enums;

public enum SemanticsOf {

    /**
     * Safe, with no side effects, and caching the returned value when invoked multiple times in the same request.
     */
    SAFE_AND_REQUEST_CACHEABLE,
    /**
     * Safe, with no side-effects.
     *
     * <p>
     * In other words, a query-only action.  By definition, is also idempotent.
     */
    SAFE,
    /**
     * Post-conditions are always the same, irrespective as to how many times called.
     *
     * <p>
     * An example might be <tt>placeOrder()</tt>, that is a no-op if the order has already been placed.
     */
    IDEMPOTENT,
    /**
     * Neither safe nor idempotent; every invocation is likely to change the state of the object.
     *
     * <p>
     * An example is increasing the quantity of a line item in an Order by 1.
     */
    NON_IDEMPOTENT,
    /**
     * Post-conditions are always the same, irrespective as to how many times called.
     *
     * <p>
     * If supported the UI viewer will show a confirmation dialog before executing the action.
     *
     * <p>
     * An example might be <tt>placeOrder()</tt>, that is a no-op if the order has already been placed.
     */
    IDEMPOTENT_ARE_YOU_SURE,
    /**
     * Neither safe nor idempotent; every invocation is likely to change the state of the object.
     *
     * <p>
     * If supported the UI viewer will show a confirmation dialog before executing the action.
     *
     * <p>
     * An example is increasing the quantity of a line item in an Order by 1.
     */
    NON_IDEMPOTENT_ARE_YOU_SURE;

    public String getFriendlyName() {
        return Enums.getFriendlyNameOf(this);
    }

    public String getCamelCaseName() {
        return Enums.enumToCamelCase(this);
    }

    /**
     * Any of {@link #SAFE}, {@link #SAFE_AND_REQUEST_CACHEABLE} or (obviously) {@link #IDEMPOTENT}.
     */
    public boolean isIdempotentInNature() {
        return isSafeInNature() || this == IDEMPOTENT || this == IDEMPOTENT_ARE_YOU_SURE;
    }

    /**
     * Either of {@link #SAFE} or {@link #SAFE_AND_REQUEST_CACHEABLE}.
     */
    public boolean isSafeInNature() {
        return isSafeAndRequestCacheable() || this == SAFE;
    }

    /**
     * @deprecated - use {@link #isSafeInNature()} instead (avoid any ambiguity).
     */
    @Deprecated
    public boolean isSafe() {
        return isSafeInNature();
    }

    public boolean isSafeAndRequestCacheable() {
        return this == SAFE_AND_REQUEST_CACHEABLE;
    }

    public boolean isAreYouSure() {
        return this == IDEMPOTENT_ARE_YOU_SURE || this == NON_IDEMPOTENT_ARE_YOU_SURE;
    }

    @Deprecated
    public static ActionSemantics.Of from(final SemanticsOf semantics) {
        if(semantics == null) return null;
        if(semantics == SAFE_AND_REQUEST_CACHEABLE) return ActionSemantics.Of.SAFE_AND_REQUEST_CACHEABLE;
        if(semantics == SAFE) return ActionSemantics.Of.SAFE;
        if(semantics == IDEMPOTENT) return ActionSemantics.Of.IDEMPOTENT;
        if(semantics == IDEMPOTENT_ARE_YOU_SURE) return ActionSemantics.Of.IDEMPOTENT_ARE_YOU_SURE;
        if(semantics == NON_IDEMPOTENT) return ActionSemantics.Of.NON_IDEMPOTENT;
        if(semantics == NON_IDEMPOTENT_ARE_YOU_SURE) return ActionSemantics.Of.NON_IDEMPOTENT_ARE_YOU_SURE;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized of: " + semantics);
    }

    @Deprecated
    public static SemanticsOf from(final ActionSemantics.Of semantics) {
        if(semantics == null) return null;
        if(semantics == ActionSemantics.Of.SAFE_AND_REQUEST_CACHEABLE) return SAFE_AND_REQUEST_CACHEABLE;
        if(semantics == ActionSemantics.Of.SAFE) return SAFE;
        if(semantics == ActionSemantics.Of.IDEMPOTENT) return IDEMPOTENT;
        if(semantics == ActionSemantics.Of.IDEMPOTENT_ARE_YOU_SURE) return IDEMPOTENT_ARE_YOU_SURE;
        if(semantics == ActionSemantics.Of.NON_IDEMPOTENT) return NON_IDEMPOTENT;
        if(semantics == ActionSemantics.Of.NON_IDEMPOTENT_ARE_YOU_SURE) return NON_IDEMPOTENT_ARE_YOU_SURE;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized semantics: " + semantics);
    }
}
