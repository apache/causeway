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

import org.apache.isis.applib.annotation.Hidden;

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

    // {{ resolve, objectChanged
    /**
     * Resolve this object, populating references to other objects.
     */
    @Hidden
    protected void resolve() {
        getContainer().resolve(this);
    }

    /**
     * Resolve this object if the referenced object is still unknown.
     */
    @Hidden
    protected void resolve(final Object referencedObject) {
        getContainer().resolve(this, referencedObject);
    }

    /**
     * Notifies the container that this object has changed, so that it can be
     * persisted.
     */
    @Hidden
    protected void objectChanged() {
        getContainer().objectChanged(this);
    }

    // }}

    // {{ isPersistent, makePersistent (overloads)
    /**
     * Whether this object is persistent.
     * 
     * @deprecated - instead use {@link #isPersistent(Object)}.
     */
    @Deprecated
    @Hidden
    protected boolean isPersistent() {
        return isPersistent(this);
    }

    /**
     * Save this object to the persistent object store.
     * 
     * <p>
     * If the object {@link #isPersistent(Object) is persistent} already, then
     * will throw an exception.
     * 
     * @see #persistIfNotAlready(Object)
     * 
     * @deprecated - instead use {@link #persist(Object)}.
     */
    @Deprecated
    @Hidden
    protected void makePersistent() {
        persist(this);
    }

    /**
     * Saves the object, but only if not already {@link #isPersistent(Object)
     * persistent}.
     * 
     * @see #isPersistent(Object)
     * @see #persist(Object)
     * 
     * @deprecated - instead use {@link #persistIfNotAlready(Object)}.
     */
    @Deprecated
    @Hidden
    protected void makePersistentIfNotAlready() {
        persistIfNotAlready(this);
    }

    /**
     * Delete this object from the persistent object store.
     * 
     * @deprecated - instead use {@link #remove(Object)}.
     */
    @Deprecated
    @Hidden
    protected void disposeInstance() {
        remove(this);
    }
    // }}

}
