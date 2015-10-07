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

package org.apache.isis.applib;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * Convenience super class for all domain objects that wish to interact with the
 * container.
 * 
 * <p>
 * Subclassing is NOT mandatory; the methods in this superclass can be pushed
 * down into domain objects and another superclass used if required.
 * 
 * @see org.apache.isis.applib.DomainObjectContainer
 */
public abstract class AbstractDomainObject extends AbstractContainedObject {

    //region > resolve, objectChanged

    /**
     * Re-initialises the fields of an object, using the
     * JDO {@link javax.jdo.PersistenceManager#refresh(Object) refresh} API.
     *
     * <p>
     *     Previously this method was provided for manual control of lazy loading; with the JDO/DataNucleus objectstore
     *     that original functionality is performed automatically by the framework.
     * </p>
     *
     * @deprecated - equivalent to {@link org.apache.isis.applib.services.jdosupport.IsisJdoSupport#refresh(Object)}.
     */
    @Programmatic
    @Deprecated
    protected void resolve() {
        getContainer().resolve(this);
    }

    /**
     * Provided that the <tt>field</tt> parameter is <tt>null</tt>, re-initialises the fields of an object, using the
     * JDO {@link javax.jdo.PersistenceManager#refresh(Object) refresh} API.
     *
     * <p>
     *     Previously this method was provided for manual control of lazy loading; with the JDO/DataNucleus objectstore
     *     that original functionality is performed automatically by the framework.
     * </p>
     *
     * @deprecated - equivalent to {@link org.apache.isis.applib.services.jdosupport.IsisJdoSupport#refresh(Object)}.
     */
    @Programmatic
    @Deprecated
    protected void resolve(final Object referencedObject) {
        getContainer().resolve(this, referencedObject);
    }

    /**
     * This method does nothing (is a no-op).
     *
     * <p>
     *     Previous this method was provided for manual control of object dirtyng; with the JDO/DataNucleus objectstore
     *     that original functionality is performed automatically by the framework.
     * </p>
     *
     * @deprecated
     */
    @Programmatic
    @Deprecated
    protected void objectChanged() {
        getContainer().objectChanged(this);
    }

    //endregion

    //region > isPersistent, makePersistent (overloads)
    /**
     * Deprecated, recommend use {@link #isPersistent(Object)} instead.
     *
     * @see #isPersistent(Object)
     *
     * @deprecated - instead recommend that {@link #isPersistent(Object)} or simply {@link DomainObjectContainer#isPersistent(Object)} be used instead.
     */
    @Deprecated
    @Programmatic
    protected boolean isPersistent() {
        return isPersistent(this);
    }

    /**
     * Deprecated, has been renamed to {@link #persist(Object)}.
     *
     * @see #persist(Object)
     *
     * @deprecated - instead use {@link #persist(Object)}.
     */
    @Deprecated
    @Programmatic
    protected void makePersistent() {
        persist(this);
    }

    /**
     * Deprecated, has been renamed to {@link #persistIfNotAlready(Object)}.
     *
     * @see #persistIfNotAlready(Object)
     * 
     * @deprecated - instead use {@link #persistIfNotAlready(Object)}.
     */
    @Deprecated
    @Programmatic
    protected void makePersistentIfNotAlready() {
        persistIfNotAlready(this);
    }

    /**
     * Deprecated, has been renamed to {@link #remove(Object)}.
     *
     * @see #remove(Object).
     * 
     * @deprecated - instead use {@link #remove(Object)}.
     */
    @Deprecated
    @Programmatic
    protected void disposeInstance() {
        remove(this);
    }
    //endregion

}
