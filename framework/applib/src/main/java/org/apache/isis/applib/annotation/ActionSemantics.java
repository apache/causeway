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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.util.Enums;

/**
 * Indicates that an instance cannot be persisted by a user, but only
 * programmatically.
 */
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionSemantics {

    public enum Of {
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


    }

    Of value() default Of.NON_IDEMPOTENT;
    
}
