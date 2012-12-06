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

package org.apache.isis.core.metamodel.adapter;

import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.CollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ElementSpecificationProvider;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Specification;

/**
 * Adapters to domain objects, where the application is written in terms of
 * domain objects and those objects are represented within the NOF through these
 * adapter, and not directly.
 */
public interface ObjectAdapter extends Instance, org.apache.isis.applib.annotation.When.Persistable {

    
    /**
     * Refines {@link Instance#getSpecification()}.
     */
    @Override
    ObjectSpecification getSpecification();

    /**
     * Returns the adapted domain object, the POJO, that this adapter represents
     * with the NOF.
     */
    Object getObject();

    /**
     * Returns the title to display this object with, which is usually got from
     * the wrapped {@link #getObject() domain object}.
     */
    String titleString();

    /**
     * Return an {@link Instance} of the specified {@link Specification} with
     * respect to this {@link ObjectAdapter}.
     * 
     * <p>
     * If called with {@link ObjectSpecification}, then just returns
     * <tt>this</tt>). If called for other subinterfaces, then should provide an
     * appropriate {@link Instance} implementation.
     * 
     * <p>
     * Designed to be called in a double-dispatch design from
     * {@link Specification#getInstance(ObjectAdapter)}.
     * 
     * <p>
     * Note: this method will throw an {@link UnsupportedOperationException}
     * unless the extended <tt>PojoAdapterXFactory</tt> is configured. (That is,
     * only <tt>PojoAdapterX</tt> provides support for this; the regular
     * <tt>PojoAdapter</tt> does not currently.
     * 
     * @param adapter
     * @return
     */
    Instance getInstance(Specification specification);

    /**
     * Sometimes it is necessary to manage the replacement of the underlying
     * domain object (by another component such as an object store). This method
     * allows the adapter to be kept while the domain object is replaced.
     */
    void replacePojo(Object pojo);

    /**
     * For (stand-alone) collections, returns the element type.
     * 
     * <p>
     * For owned (aggregated) collections, the element type can be determined
     * from the <tt>TypeOfFacet</tt> associated with the
     * <tt>ObjectAssociation</tt> representing the collection.
     * 
     * @see #setElementSpecificationProvider(ElementSpecificationProvider)
     */
    ObjectSpecification getElementSpecification();

    /**
     * For (stand-alone) collections, returns the element type.
     * 
     * @see #getElementSpecification()
     */
    void setElementSpecificationProvider(ElementSpecificationProvider elementSpecificationProvider);

    
    
    
    /**
     * Returns the name of an icon to use if this object is to be displayed
     * graphically.
     * 
     * <p>
     * May return <code>null</code> if no icon is specified.
     */
    String getIconName();

    /**
     * Changes the 'lazy loaded' state of the domain object.
     * 
     * @see ResolveState
     */
    void changeState(ResolveState newState);

    /**
     * Checks the version of this adapter to make sure that it does not differ
     * from the specified version.
     * 
     * @throws ConcurrencyException
     *             if the specified version differs from the version held this
     *             adapter.
     */
    void checkLock(Version version);

    /**
     * The object's unique {@link Oid}. 
     * 
     * <p>
     * This id allows the object to added to, stored by,
     * and retrieved from the object store.  Objects can be looked up by their
     * {@link Oid} from the {@link AdapterManager}.
     * 
     * <p>
     * Note that standalone value objects ("foobar", or 5, or a date),
     * are not mapped and have a <tt>null</tt> oid.
     */
    Oid getOid();

    /**
     * Since {@link Oid}s are now immutable, it is the reference from the 
     * {@link ObjectAdapter} to its {@link Oid} that must now be updated. 
     */
    void replaceOid(Oid persistedOid);

    /**
     * Determines what 'lazy loaded' state the domain object is in.
     * 
     * @see ResolveState
     */
    ResolveState getResolveState();


    /**
     * Whether the object is persisted.
     * 
     * <p>
     * Note: not necessarily the reciprocal of {@link #isTransient()};
     * standalone adapters (with {@link ResolveState#VALUE}) report as neither
     * persistent or transient.
     */
    boolean representsPersistent();

    boolean isNew();
    boolean isTransient();

    boolean isGhost();
    boolean isResolved();

    boolean isResolving();
    boolean isUpdating();

    boolean isDestroyed();


    boolean canTransitionToResolving();
    boolean isTitleAvailable();
    void markAsResolvedIfPossible();

    
    
    Version getVersion();

    void setVersion(Version version);

    void fireChangedEvent();

    /**
     * Whether this instance belongs to another object (meaning its
     * {@link #getOid()} will be <tt>ParentedOid</tt>, either an 
     * {@link AggregatedOid} or a {@link CollectionOid}).
     */
    boolean isParented();

    /**
     * Whether this is an aggregated Oid.
     */
    boolean isAggregated();

    /**
     * Whether this is a value (standalone, has no oid).
     */
    boolean isValue();


    /**
     * Either the aggregate root (either itself or, if parented, then its parent adapter).
     * 
     * TODO: should this be recursive, to support root->aggregate->aggregate etc.
     */
    ObjectAdapter getAggregateRoot();

    boolean respondToChangesInPersistentObjects();




}
