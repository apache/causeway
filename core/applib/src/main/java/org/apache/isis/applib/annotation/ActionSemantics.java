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

/**
 * @deprecated - see {@link Action#semantics()}.
 */
@Deprecated
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionSemantics {

    /**
     * @deprecated - see {@link SemanticsOf}
     */
    @Deprecated
    public enum Of {
        /**
         * @deprecated - see {@link SemanticsOf#SAFE_AND_REQUEST_CACHEABLE}
         */
        @Deprecated
        SAFE_AND_REQUEST_CACHEABLE,
        /**
         * @deprecated - see {@link SemanticsOf#SAFE}
         */
        @Deprecated
        SAFE,
        /**
         * @deprecated - see {@link SemanticsOf#IDEMPOTENT}
         */
        @Deprecated
        IDEMPOTENT,
        /**
         * @deprecated - see {@link SemanticsOf#NON_IDEMPOTENT}
         */
        @Deprecated
        NON_IDEMPOTENT,
        /**
         * @deprecated - see {@link SemanticsOf#IDEMPOTENT_ARE_YOU_SURE}
         */
        @Deprecated
        IDEMPOTENT_ARE_YOU_SURE,
        /**
         * @deprecated - see {@link SemanticsOf#NON_IDEMPOTENT_ARE_YOU_SURE}
         */
        @Deprecated
        NON_IDEMPOTENT_ARE_YOU_SURE;

        /**
         * @deprecated - see {@link SemanticsOf#getFriendlyName()}
         */
        @Deprecated
        public String getFriendlyName() {
            return SemanticsOf.from(this).getFriendlyName();
            //return Enums.getFriendlyNameOf(this);
        }

        /**
         * @deprecated - see {@link SemanticsOf#getCamelCaseName()}
         */
        @Deprecated
        public String getCamelCaseName() {
            return SemanticsOf.from(this).getCamelCaseName();
        }

        /**
         * Any of {@link #SAFE}, {@link #SAFE_AND_REQUEST_CACHEABLE} or (obviously) {@link #IDEMPOTENT}.
         *
         * @deprecated - see {@link SemanticsOf#isIdempotentInNature()}
         */
        @Deprecated
        public boolean isIdempotentInNature() {
            return SemanticsOf.from(this).isIdempotentInNature();
        }

        /**
         * Either of {@link #SAFE} or {@link #SAFE_AND_REQUEST_CACHEABLE}.
         *
         * @deprecated - see {@link SemanticsOf#isSafeInNature()}.
         */
        @Deprecated
        public boolean isSafeInNature() {
            return SemanticsOf.from(this).isSafeInNature();
        }

        /**
         * @deprecated - see {@link #isSafeInNature()} (avoid any ambiguity)
         */
        @Deprecated
        public boolean isSafe() {
            return isSafeInNature();
        }

        /**
         * @deprecated - see {@link SemanticsOf#isSafeInNature()}.
         */
        @Deprecated
        public boolean isSafeAndRequestCacheable() {
            return SemanticsOf.from(this).isSafeAndRequestCacheable();
        }

        /**
         * @deprecated - see {@link SemanticsOf#isAreYouSure()}.
         */
        @Deprecated
        public boolean isAreYouSure() {
            return SemanticsOf.from(this).isAreYouSure();
        }
    }

    /**
     * @deprecated - use {@link Action#semantics()} instead.
     */
    @Deprecated
    Of value() default Of.NON_IDEMPOTENT;
    
}
