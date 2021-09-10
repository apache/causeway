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

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.util.Enums;

/**
 * @since 1.x {@index}
 * @see <a href="https://isis.apache.org/guides/rgant/rgant.html#_rgant-Action_semantics">Reference Guide</a>
 */
@Value(logicalTypeName = IsisModuleApplib.NAMESPACE + ".annotation.SemanticsOf")
public enum SemanticsOf {

    /**
     * Safe, with no side effects, and caching the returned value when invoked multiple times in the same request.
     * <ul>
     * <li>Changes state: <b>No</b></li>
     * <li>HTTP verb: <b>GET</b></li>
     * <li>Effect of multiple calls: Will <b>always return the same result</b> each time invoked
     * (within a given request scope).</li>
     * </ul>
     */
    SAFE_AND_REQUEST_CACHEABLE,

    /**
     * Safe, with no side-effects. In other words, a query-only action.
     * <ul>
     * <li>Changes state: <b>No</b></li>
     * <li>HTTP verb: <b>GET</b></li>
     * <li>Effect of multiple calls: Might result in <b>different results each invocation</b>
     * (within a given request scope).</li>
     * </ul>
     */
    SAFE,

    /**
     * Post-conditions are always the same, irrespective as to how many times called.
     * <ul>
     * <li>Changes state: <b>Yes</b></li>
     * <li>HTTP verb: <b>PUT</b></li>
     * <li>Effect of multiple calls: Will make <b>no further changes</b> if called multiple times
     * (eg sets a property or adds of same item to a Set).</li>
     * </ul>
     */
    IDEMPOTENT,

    /**
     * Neither safe nor idempotent; every invocation is likely to change the state of the object.
     * <ul>
     * <li>Changes state: <b>Yes</b></li>
     * <li>HTTP verb: <b>POST</b></li>
     * <li>Effect of multiple calls: Might <b>change the state</b> of the system each time called
     * (eg increments a counter or adds to a List).</li>
     * <li>Example: Increasing the quantity of a line item in an Order by 1.</li>
     * </ul>
     */
    NON_IDEMPOTENT,

    /**
     * Post-conditions are always the same, irrespective as to how many times called.
     * <p>
     * If supported the UI viewer will show a confirmation dialog before executing the action.
     * <ul>
     * <li>Changes state: <b>Yes</b></li>
     * <li>HTTP verb: <b>PUT</b></li>
     * <li>Effect of multiple calls: Will make <b>no further changes</b> if called multiple times
     * (eg sets a property or adds of same item to a Set).</li>
     * </ul>
     */
    IDEMPOTENT_ARE_YOU_SURE,

    /**
     * Neither safe nor idempotent; every invocation is likely to change the state of the object.
     * <p>
     * If supported the UI viewer will show a confirmation dialog before executing the action.
     * <ul>
     * <li>Changes state: <b>Yes</b></li>
     * <li>HTTP verb: <b>POST</b></li>
     * <li>Effect of multiple calls: Might <b>change the state</b> of the system each time called
     * (eg increments a counter or adds to a List).</li>
     * <li>Example: Increasing the quantity of a line item in an Order by 1.</li>
     */
    NON_IDEMPOTENT_ARE_YOU_SURE,

    /**
     * Ignore the value provided by this annotation (meaning that the framework will keep searching, in meta
     * annotations or superclasses/interfaces).
     */
    NOT_SPECIFIED

    ;

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

    public boolean isSafeAndRequestCacheable() {
        return this == SAFE_AND_REQUEST_CACHEABLE;
    }

    public boolean isAreYouSure() {
        return this == IDEMPOTENT_ARE_YOU_SURE || this == NON_IDEMPOTENT_ARE_YOU_SURE;
    }


}
