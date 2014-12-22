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

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.RequestScoped;

/**
 * @deprecated - use {@link Action#bulk()} instead.
 */
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Bulk {

    /**
     * @deprecated - see {@link org.apache.isis.applib.annotation.Action.BulkAppliesTo}
     */
    @Deprecated
    public static enum AppliesTo {
        /**
         * @deprecated - see {@link org.apache.isis.applib.annotation.Action.BulkAppliesTo#BULK_AND_REGULAR}
         */
        @Deprecated
        BULK_AND_REGULAR,
        /**
         * @deprecated - see {@link org.apache.isis.applib.annotation.Action.BulkAppliesTo#BULK_ONLY}
         */
        @Deprecated
        BULK_ONLY
    }

    /**
     * @deprecated - see {@link Action#bulk()}.
     */
    @Deprecated
    AppliesTo value() default AppliesTo.BULK_AND_REGULAR;


    // //////////////////////////////////////

    /**
     * @deprecated - see {@link BulkInteractionContext}.
     */
    @Deprecated
    @DomainService
    @RequestScoped
    public static class InteractionContext extends BulkInteractionContext {


        /**
         * @deprecated - use {@link org.apache.isis.applib.annotation.BulkInteractionContext.InvokedAs} instead.
         */
        @Deprecated
        public static enum InvokedAs {
            /**
             * @deprecated - use {@link org.apache.isis.applib.annotation.BulkInteractionContext.InvokedAs#BULK} instead.
             */
            @Deprecated
            BULK,
            /**
             * @deprecated - use {@link org.apache.isis.applib.annotation.BulkInteractionContext.InvokedAs#REGULAR} instead.
             */
            @Deprecated
            REGULAR;
            public boolean isRegular() { return this == REGULAR; }
            public boolean isBulk() { return this == BULK; }


        }


        /**
         * @deprecated - now a {@link RequestScoped} service
         */
        @Deprecated
        public static void with(final Runnable runnable, final Object... domainObjects) {
            throw new RuntimeException("No longer supported - instead inject Bulk.InteractionContext as service");
        }

        /**
         * @deprecated - now a {@link RequestScoped} service
         */
        @Deprecated
        public static void with(final Runnable runnable, final InvokedAs invokedAs, final Object... domainObjects) {
            throw new RuntimeException("No longer supported - instead inject Bulk.InteractionContext as service");
        }

        // //////////////////////////////////////

        /**
         * @deprecated - see {@link BulkInteractionContext#regularAction(Object)}.
         */
        @Deprecated
        public static InteractionContext regularAction(Object domainObject) {
            return new InteractionContext(InvokedAs.REGULAR, Collections.singletonList(domainObject));
        }

        /**
         * @deprecated - see {@link BulkInteractionContext#bulkAction(Object...)}.
         */
        @Deprecated
        public static InteractionContext bulkAction(Object... domainObjects) {
            return bulkAction(Arrays.asList(domainObjects));
        }

        /**
         * @deprecated - see {@link BulkInteractionContext#bulkAction(java.util.List)}.
         */
        @Deprecated
        public static InteractionContext bulkAction(List<Object> domainObjects) {
            return new InteractionContext(InvokedAs.BULK, domainObjects);
        }


        // //////////////////////////////////////

        /**
         * @deprecated - see {@link org.apache.isis.applib.annotation.BulkInteractionContext()}.
         */
        @Deprecated
        public InteractionContext() {
            super();
        }

        /**
         * @deprecated - now a {@link RequestScoped} service
         */
        @Deprecated
        public InteractionContext(final InvokedAs invokedAs, final List<Object> domainObjects) {
            super(BulkInteractionContext.InvokedAs.from(invokedAs), domainObjects);
        }
    }

}
