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
    NON_IDEMPOTENT;

    public String getFriendlyName() {
        return Enums.getFriendlyNameOf(this);
    }

    public String getCamelCaseName() {
        return Enums.enumToCamelCase(this);
    }

    /**
     * {@link #SAFE} is idempotent in nature, as well as, obviously, {@link #IDEMPOTENT}.
     */
    public boolean isIdempotentInNature() {
        return this == SAFE || this == IDEMPOTENT;
    }

    public boolean isSafe() {
        return this == SAFE;
    }

    @Deprecated
    public static ActionSemantics.Of from(final SemanticsOf semantics) {
        if(semantics == null) return null;
        if(semantics == SAFE) return ActionSemantics.Of.SAFE;
        if(semantics == IDEMPOTENT) return ActionSemantics.Of.IDEMPOTENT;
        if(semantics == NON_IDEMPOTENT) return ActionSemantics.Of.NON_IDEMPOTENT;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized of: " + semantics);
    }

    @Deprecated
    public static SemanticsOf from(final ActionSemantics.Of semantics) {
        if(semantics == null) return null;
        if(semantics == ActionSemantics.Of.SAFE) return SAFE;
        if(semantics == ActionSemantics.Of.IDEMPOTENT) return IDEMPOTENT;
        if(semantics == ActionSemantics.Of.NON_IDEMPOTENT) return NON_IDEMPOTENT;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized semantics: " + semantics);
    }
}
