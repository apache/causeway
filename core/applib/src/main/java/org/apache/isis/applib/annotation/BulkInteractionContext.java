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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This service (API and implementation) provides access to context information about a bulk action invocation.
 *
 * <p>
 * This implementation has no UI and there is only one implementation (this class) in applib, so it is annotated
 * with {@link DomainService}.  This means that it is automatically registered
 * and available for use; no further configuration is required.
 * </p>
 *
 * <p>
 *     Note that this not annotated as <tt>@DomainService</tt> an <tt>@RequestScoped</tt> only because of the
 *     legacy {@link org.apache.isis.applib.annotation.Bulk.InteractionContext} subclass (which *is* registered as
 *     the service).
 * </p>
 */
public abstract class BulkInteractionContext {

    public static enum InvokedAs {
        BULK,
        REGULAR;
        public boolean isRegular() { return this == REGULAR; }
        public boolean isBulk() { return this == BULK; }

        @Deprecated
        public static InvokedAs from(final Bulk.InteractionContext.InvokedAs invokedAs) {
            if (invokedAs == null) return null;
            if (invokedAs == Bulk.InteractionContext.InvokedAs.REGULAR) return REGULAR;
            if (invokedAs == Bulk.InteractionContext.InvokedAs.BULK) return BULK;
            // shouldn't happen
            throw new IllegalArgumentException("Unrecognized bulk interactionContext invokedAs: " + invokedAs);
        }

        @Deprecated
        public static Bulk.InteractionContext.InvokedAs from(final BulkInteractionContext.InvokedAs invokedAs) {
            if (invokedAs == null) return null;
            if (invokedAs == REGULAR) return Bulk.InteractionContext.InvokedAs.REGULAR;
            if (invokedAs == BULK) return Bulk.InteractionContext.InvokedAs.BULK;
            // shouldn't happen
            throw new IllegalArgumentException("Unrecognized bulk interactionContext invokedAs: " + invokedAs);
        }


    }

    /**
     * Intended only to be set only by the framework.
     *
     * <p>
     * Will be populated while a bulk action is being invoked.
     *
     * @deprecated - now a {@link javax.enterprise.context.RequestScoped} service
     */
    @Deprecated
    public static final ThreadLocal<BulkInteractionContext> current = new ThreadLocal<>();

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
    public static void with(final Runnable runnable, final InvokedAs invokedAs, final Object... domainObjects) {
        throw new RuntimeException("No longer supported - instead inject Bulk.InteractionContext as service");
    }

    // //////////////////////////////////////

    private InvokedAs actionInvokedAs;
    private List<Object> domainObjects;

    private int index;

    // //////////////////////////////////////


    public BulkInteractionContext() {
    }

    /**
     * @deprecated - now a {@link javax.enterprise.context.RequestScoped} service
     */
    @Deprecated
    public BulkInteractionContext(final InvokedAs actionInvokedAs, final Object... domainObjects) {
        this(actionInvokedAs, Arrays.asList(domainObjects));
    }

    /**
     * @deprecated - now a {@link javax.enterprise.context.RequestScoped} service
     */
    @Deprecated
    public BulkInteractionContext(final InvokedAs actionInvokedAs, final List<Object> domainObjects) {
        this.actionInvokedAs = actionInvokedAs;
        this.domainObjects = domainObjects;
    }

    // //////////////////////////////////////

    /**
     * Intended only to support unit testing.
     */
    public static BulkInteractionContext regularAction(Object domainObject) {
        return new BulkInteractionContext(InvokedAs.REGULAR, Collections.singletonList(domainObject)){};
    }

    /**
     * Intended only to support unit testing.
     */
    public static BulkInteractionContext bulkAction(Object... domainObjects) {
        return bulkAction(Arrays.asList(domainObjects));
    }

    /**
     * Intended only to support unit testing.
     */
    public static BulkInteractionContext bulkAction(List<Object> domainObjects) {
        return new BulkInteractionContext(InvokedAs.BULK, domainObjects){};
    }


    // //////////////////////////////////////

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    @Programmatic
    public void setActionInvokedAs(InvokedAs actionInvokedAs) {
        this.actionInvokedAs = actionInvokedAs;
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
     * Whether this particular {@link org.apache.isis.applib.annotation.BulkInteractionContext} was applied as a {@link InvokedAs#BULK bulk} action
     * (against each domain object in a list of domain objects) or as a {@link InvokedAs#REGULAR regular}
     * action (against a single domain object).
     */
    @Programmatic
    public BulkInteractionContext.InvokedAs getActionInvokedAs() {
        return actionInvokedAs;
    }

    /**
     * Whether this particular {@link org.apache.isis.applib.annotation.Bulk.InteractionContext} was applied as a {@link InvokedAs#BULK bulk} action
     * (against each domain object in a list of domain objects) or as a {@link InvokedAs#REGULAR regular}
     * action (against a single domain object).
     *
     * @deprecated - use {@link #getActionInvokedAs()} instead.
     */
    @Deprecated
    @Programmatic
    public Bulk.InteractionContext.InvokedAs getInvokedAs() {
        return BulkInteractionContext.InvokedAs.from(actionInvokedAs);
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
