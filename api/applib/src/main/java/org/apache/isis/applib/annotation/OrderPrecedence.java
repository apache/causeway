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

import org.springframework.core.Ordered;

import lombok.experimental.UtilityClass;

/**
 * 
 * @since 2.0 {@index}
 */
@UtilityClass
public class OrderPrecedence {

    /**
     * For domain services with the highest precedence value.
     * No framework services use this constant, but some very fundamental services (eg for security)
     * that are not expected to be overridden use a value that is only a little after this first value.
     *
     * @see java.lang.Integer#MIN_VALUE
     * @see Ordered#HIGHEST_PRECEDENCE
     */
    public static final int FIRST = Ordered.HIGHEST_PRECEDENCE;

    /**
     * For framework for services that are unlikely to be overridden by application code.
     */
    public static final int EARLY = FIRST / 2;

    /**
     * For framework for services that could be overridden by application code (though not commonly).
     */
    public static final int MIDPOINT = 0;

    /**
     * For framework services that are expected to be overridden by application code, or that act as a fallback.
     */
    public static final int LATE = OrderPrecedence.LAST / 2;

    /**
     * For domain services that act as a fallback, and which will typically be overridden.
     *
     * @see java.lang.Integer#MAX_VALUE
     * @see Ordered#LOWEST_PRECEDENCE
     */
    public static final int LAST = Ordered.LOWEST_PRECEDENCE;

}

