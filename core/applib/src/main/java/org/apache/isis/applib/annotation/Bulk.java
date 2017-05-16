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
 * @deprecated - use {@link Action#invokeOn()} instead.
 */
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Bulk {

    /**
     * @deprecated - see {@link InvokeOn}
     */
    @Deprecated
    public static enum AppliesTo {
        /**
         * @deprecated - see {@link InvokeOn#OBJECT_AND_COLLECTION}
         */
        @Deprecated
        BULK_AND_REGULAR,
        /**
         * @deprecated - see {@link InvokeOn#COLLECTION_ONLY}
         */
        @Deprecated
        BULK_ONLY,
        /**
         * @deprecated - see {@link InvokeOn#OBJECT_ONLY}
         */
        @Deprecated
        REGULAR_ONLY
    }

    /**
     * @deprecated - see {@link Action#invokeOn()}.
     */
    @Deprecated
    AppliesTo value() default AppliesTo.BULK_AND_REGULAR;


    // //////////////////////////////////////

    /**
     * @deprecated - see {@link org.apache.isis.applib.services.actinvoc.ActionInvocationContext}.
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
         * @deprecated - use {@link InvokedOn} instead.
         */
        @Deprecated
        public static enum InvokedAs {
            /**
             * @deprecated - use {@link InvokedOn#COLLECTION} instead.
             */
            @Deprecated
            BULK,
            /**
             * @deprecated - use {@link InvokedOn#OBJECT} instead.
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
         * @deprecated - see {@link org.apache.isis.applib.services.actinvoc.ActionInvocationContext#onObject(Object)}.
         */
        @Deprecated
        public static InteractionContext regularAction(Object domainObject) {
            return new InteractionContext(InvokedAs.REGULAR, Collections.singletonList(domainObject));
        }

        /**
         * @deprecated - see {@link org.apache.isis.applib.services.actinvoc.ActionInvocationContext#onCollection(Object...)}.
         */
        @Deprecated
        public static InteractionContext bulkAction(Object... domainObjects) {
            return bulkAction(Arrays.asList(domainObjects));
        }

        /**
         * @deprecated - see {@link org.apache.isis.applib.services.actinvoc.ActionInvocationContext#onCollection(java.util.List)}.
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
         * @deprecated - see {@link org.apache.isis.applib.services.actinvoc.ActionInvocationContext ()}.
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
         */
        @Deprecated
        @Programmatic
        public Bulk.InteractionContext.InvokedAs getInvokedAs() {
            return invokedAs;
        }

        /**
         * The list of domain objects which are being acted upon.
         */
        @Programmatic
        public List<Object> getDomainObjects() {
            return domainObjects;
        }

        /**
         * The number of {@link #domainObjects domain objects} being acted upon.
         */
        @Programmatic
        public int getSize() {
            return domainObjects.size();
        }

        /**
         * The 0-based index to the object being acted upon.
         *
         * <p>
         * Will be a value in range [0, {@link #getSize() size}).
         */
        @Programmatic
        public int getIndex() {
            return index;
        }

        /**
         * Whether this object being acted upon is the first such.
         */
        @Programmatic
        public boolean isFirst() {
            return this.index == 0;
        }

        /**
         * Whether this object being acted upon is the last such.
         */
        @Programmatic
        public boolean isLast() {
            return this.index == (getSize()-1);
        }



    }

}
