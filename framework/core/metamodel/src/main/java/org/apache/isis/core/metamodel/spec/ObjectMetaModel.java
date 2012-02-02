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

package org.apache.isis.core.metamodel.spec;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;

/**
 * Adapters to domain objects, where the application is written in terms of
 * domain objects and those objects are represented within the NOF through these
 * adapter, and not directly.
 * 
 * <p>
 * This interface defines just those aspects of the adapter that are of interest
 * to the meta-model (hence the 'MM' suffix), while the {@link ObjectAdapter}
 * subtype defines additional properties used by the framework (
 * <tt>PersistenceSession</tt>s et al) for tracking the persistence state of the
 * object. In theory it ought to be possible to move {@link ObjectAdapter}
 * interface out of the metamodel module. However, this would require either
 * lots of genericizing or lots of downcasting to ensure that the framework is
 * only ever handed or deals with the {@link ObjectAdapter}s.
 * 
 * @see ObjectAdapter
 */
public interface ObjectMetaModel extends Instance {

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
     * Whether the object is persisted.
     * 
     * <p>
     * Note: not necessarily the reciprocal of {@link #isTransient()};
     * standalone adapters (with {@link ResolveState#VALUE}) report as neither
     * persistent or transient.
     */
    boolean isPersistent();

    /**
     * Whether the object is transient.
     * 
     * <p>
     * Note: not necessarily the reciprocal of {@link #isPersistent()};
     * standalone adapters (with {@link ResolveState#VALUE}) report as neither
     * persistent or transient.
     */
    boolean isTransient();

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

}
