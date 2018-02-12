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
 * Superceded by {@link Action#invokeOn()}.
 *
 * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
 */
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Bulk {

    /**
     * Superceded by {@link InvokeOn}
     *
     * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
     */
    @Deprecated
    public static enum AppliesTo {
        /**
         * Superceded by {@link InvokeOn#OBJECT_AND_COLLECTION}
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        BULK_AND_REGULAR,
        /**
         * Superceded by {@link InvokeOn#COLLECTION_ONLY}
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        BULK_ONLY,
        /**
         * Superceded by {@link InvokeOn#OBJECT_ONLY}
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        REGULAR_ONLY
    }

    /**
     * Superceded by {@link Action#invokeOn()}.
     *
     * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
     */
    @Deprecated
    AppliesTo value() default AppliesTo.BULK_AND_REGULAR;


    // //////////////////////////////////////

    /**
     * Superceded by {@link org.apache.isis.applib.services.actinvoc.ActionInvocationContext}.
     *
     * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
     */
    @Deprecated
    @DomainService(
            nature = NatureOfService.DOMAIN,
            menuOrder = "" + Integer.MAX_VALUE
    )
    @RequestScoped
    public static class InteractionContext {

        /**
         * Intended only to be set only by the framework.
         *
         * <p>
         * Will be populated while a bulk action is being invoked.
         *
         * @deprecated - now a {@link javax.enterprise.context.RequestScoped} service; simply inject an instance of {@link org.apache.isis.applib.annotation.Bulk.InteractionContext}.
         */
        @Deprecated
        public static final ThreadLocal<Bulk.InteractionContext> current = new ThreadLocal<>();

        // //////////////////////////////////////

        /**
         * Superceded by {@link InvokedOn} instead.
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        public static enum InvokedAs {
            /**
             * Superceded by {@link InvokedOn#COLLECTION} instead.
             *
             * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
             */
            @Deprecated
            BULK,
            /**
             * Superceded by {@link InvokedOn#OBJECT} instead.
             *
             * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
             */
            @Deprecated
            REGULAR;
            public boolean isRegular() { return this == REGULAR; }
            public boolean isBulk() { return this == BULK; }
        }


        /**
         * @deprecated - now a {@link javax.enterprise.context.RequestScoped} service
         */
        @Deprecated
        public static void with(final Runnable runnable, final Object... domainObjects) {
            throw new RuntimeException("No longer supported - instead inject Bulk.InteractionContext as service");
        }

        /**
         * @deprecated - now a {@link javax.enterprise.context.RequestScoped} service
         */
        @Deprecated
        public static void with(final Runnable runnable, final InvokedOn invokedOn, final Object... domainObjects) {
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
         * Superceded by  {@link org.apache.isis.applib.services.actinvoc.ActionInvocationContext#onObject(Object)}.
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        public static InteractionContext regularAction(Object domainObject) {
            return new InteractionContext(InvokedAs.REGULAR, Collections.singletonList(domainObject));
        }

        /**
         * Superceded by {@link org.apache.isis.applib.services.actinvoc.ActionInvocationContext#onCollection(Object...)}.
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        public static InteractionContext bulkAction(Object... domainObjects) {
            return bulkAction(Arrays.asList(domainObjects));
        }

        /**
         * Superceded by  {@link org.apache.isis.applib.services.actinvoc.ActionInvocationContext#onCollection(java.util.List)}.
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        public static InteractionContext bulkAction(List<Object> domainObjects) {
            return new InteractionContext(InvokedAs.BULK, domainObjects);
        }


        // //////////////////////////////////////

        private InvokedAs invokedAs;
        private List<Object> domainObjects;

        private int index;

        /**
         * Superceded by {@link org.apache.isis.applib.services.actinvoc.ActionInvocationContext ()}.
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
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
            this.invokedAs = invokedAs;
            this.domainObjects = domainObjects;
        }


        // //////////////////////////////////////

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        @Programmatic
        public void setInvokedAs(InvokedAs invokedAs) {
            this.invokedAs = invokedAs;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        @Programmatic
        public void setDomainObjects(List<Object> domainObjects) {
            this.domainObjects = domainObjects;
        }

        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        @Programmatic
        public void setIndex(int index) {
            this.index = index;
        }

        // //////////////////////////////////////


        /**
         * Whether this particular {@link org.apache.isis.applib.annotation.Bulk.InteractionContext} was applied as a {@link InvokedOn#COLLECTION bulk} action
         * (against each domain object in a list of domain objects) or as a {@link InvokedOn#OBJECT regular}
         * action (against a single domain object).
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        @Programmatic
        public Bulk.InteractionContext.InvokedAs getInvokedAs() {
            return invokedAs;
        }

        /**
         * The list of domain objects which are being acted upon.
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        @Programmatic
        public List<Object> getDomainObjects() {
            return domainObjects;
        }

        /**
         * The number of {@link #domainObjects domain objects} being acted upon.
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        @Programmatic
        public int getSize() {
            return domainObjects.size();
        }

        /**
         * The 0-based index to the object being acted upon.
         *
         * <p>
         * Will be a value in range [0, {@link #getSize() size}).
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        @Programmatic
        public int getIndex() {
            return index;
        }

        /**
         * Whether this object being acted upon is the first such.
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        @Programmatic
        public boolean isFirst() {
            return this.index == 0;
        }

        /**
         * Whether this object being acted upon is the last such.
         *
         * @deprecated - instead of bulk actions, use view models with collection parameters and {@link Action#associateWith()}.
         */
        @Deprecated
        @Programmatic
        public boolean isLast() {
            return this.index == (getSize()-1);
        }



    }

}
