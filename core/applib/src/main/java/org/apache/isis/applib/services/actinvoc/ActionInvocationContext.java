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
package org.apache.isis.applib.services.actinvoc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.InvokedOn;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * This service (API and implementation) provides access to context information about a bulk action invocation.
 *
 * <p>
 * This implementation has no UI and there is only one implementation (this class) in applib, so it is annotated
 * with {@link org.apache.isis.applib.annotation.DomainService}.  This means that it is automatically registered
 * and available for use; no further configuration is required.
 * </p>
 */
@DomainService
public class ActionInvocationContext {

    /**
     * Intended only to support unit testing.
     */
    public static ActionInvocationContext onObject(final Object domainObject) {
        return new ActionInvocationContext(InvokedOn.OBJECT, Collections.singletonList(domainObject)){};
    }

    /**
     * Intended only to support unit testing.
     */
    public static ActionInvocationContext onCollection(final Object... domainObjects) {
        return onCollection(Arrays.asList(domainObjects));
    }

    /**
     * Intended only to support unit testing.
     */
    public static ActionInvocationContext onCollection(final List<Object> domainObjects) {
        return new ActionInvocationContext(InvokedOn.COLLECTION, domainObjects){};
    }

    // //////////////////////////////////////


    private InvokedOn invokedOn;
    private List<Object> domainObjects;

    private int index;

    // //////////////////////////////////////

    public ActionInvocationContext() {
    }

    /**
     * @deprecated - now a {@link javax.enterprise.context.RequestScoped} service
     */
    @Deprecated
    public ActionInvocationContext(final InvokedOn invokedOn, final Object... domainObjects) {
        this(invokedOn, Arrays.asList(domainObjects));
    }

    /**
     * @deprecated - now a {@link javax.enterprise.context.RequestScoped} service
     */
    @Deprecated
    public ActionInvocationContext(final InvokedOn invokedOn, final List<Object> domainObjects) {
        this.invokedOn = invokedOn;
        this.domainObjects = domainObjects;
    }

    // //////////////////////////////////////

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    @Programmatic
    public void setInvokedOn(final InvokedOn invokedOn) {
        this.invokedOn = invokedOn;
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    @Programmatic
    public void setDomainObjects(final List<Object> domainObjects) {
        this.domainObjects = domainObjects;
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    @Programmatic
    public void setIndex(final int index) {
        this.index = index;
    }

    // //////////////////////////////////////


    /**
     * Whether this particular {@link ActionInvocationContext} was applied as a {@link InvokedOn#COLLECTION bulk} action
     * (against each domain object in a list of domain objects) or as a {@link InvokedOn#OBJECT regular}
     * action (against a single domain object).
     */
    @Programmatic
    public InvokedOn getInvokedOn() {
        return invokedOn;
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
