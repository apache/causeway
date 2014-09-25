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
 * Indicates the (entity) action should be used only against many objects
 * in a collection.
 * 
 * <p>
 * Bulk actions have a number of constraints:
 * <ul>
 * <li>It must take no arguments
 * <li>It cannot be hidden (any annotations or supporting methods to that effect will be
 *     ignored).
 * <li>It cannot be disabled (any annotations or supporting methods to that effect will be
 *     ignored).
 * </ul>
 * 
 * <p>
 * Has no meaning if annotated on an action of a domain service.
 */
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Bulk {
    
    public static enum AppliesTo {
        BULK_AND_REGULAR,
        BULK_ONLY
    }
    
    AppliesTo value() default AppliesTo.BULK_AND_REGULAR;
    
    /**
     * This service (API and implementation) provides access to context information about a bulk action invocation.
     *
     * <p>
     * This implementation has no UI and there is only one implementation (this class) in applib, so it is annotated
     * with {@link org.apache.isis.applib.annotation.DomainService}.  This means that it is automatically registered
     * and available for use; no further configuration is required.
     */
    @DomainService
    @RequestScoped
    public static class InteractionContext {

        public static enum InvokedAs {
            BULK,
            REGULAR;
            public boolean isRegular() { return this == REGULAR; }
            public boolean isBulk() { return this == BULK; }
        }

        /**
         * Intended only to be set only by the framework.
         * 
         * <p>
         * Will be populated while a bulk action is being invoked.
         * 
         * @deprecated - now a {@link RequestScoped} service
         */
        @Deprecated
        public static final ThreadLocal<InteractionContext> current = new ThreadLocal<InteractionContext>();

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
         * Intended only to support unit testing.
         */
        public static InteractionContext regularAction(Object domainObject) {
            return new InteractionContext(InvokedAs.REGULAR, Collections.singletonList(domainObject));
        }
        
        /**
         * Intended only to support unit testing.
         */
        public static InteractionContext bulkAction(Object... domainObjects) {
            return bulkAction(Arrays.asList(domainObjects));
        }

        /**
         * Intended only to support unit testing.
         */
        public static InteractionContext bulkAction(List<Object> domainObjects) {
            return new InteractionContext(InvokedAs.BULK, domainObjects);
        }
        
        // //////////////////////////////////////

        private InvokedAs invokedAs;
        private List<Object> domainObjects;

        private int index;

        // //////////////////////////////////////

        
        public InteractionContext() {
        }

        /**
         * @deprecated - now a {@link RequestScoped} service
         */
        @Deprecated
        public InteractionContext(final InvokedAs invokedAs, final Object... domainObjects) {
            this(invokedAs, Arrays.asList(domainObjects));
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
         * Whether this particular {@link InteractionContext} was applied as a {@link InvokedAs#BULK bulk} action 
         * (against each domain object in a list of domain objects) or as a {@link InvokedAs#REGULAR regular} 
         * action (against a single domain object).
         */
        @Programmatic
        public InvokedAs getInvokedAs() {
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
