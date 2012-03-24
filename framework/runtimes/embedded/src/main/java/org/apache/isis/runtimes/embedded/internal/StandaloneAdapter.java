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

package org.apache.isis.runtimes.embedded.internal;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterLookup;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ElementSpecificationProvider;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectMetaModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Specification;
import org.apache.isis.runtimes.embedded.PersistenceState;

/**
 * Only provides a concrete implementation of the methods corresponding to the
 * {@link ObjectMetaModel} interface.
 */
public class StandaloneAdapter implements ObjectAdapter {

    private final ObjectSpecification spec;
    private final PersistenceState persistenceState;
    private Object domainObject;

    private ElementSpecificationProvider elementSpecificationProvider;

    public StandaloneAdapter(final ObjectSpecification spec, final Object domainObject, final PersistenceState persistenceState) {
        this.spec = spec;
        this.domainObject = domainObject;
        this.persistenceState = persistenceState;
    }

    /**
     * Returns the {@link ObjectSpecification} as provided in the constructor.
     */
    @Override
    public ObjectSpecification getSpecification() {
        return spec;
    }

    /**
     * Returns the domain object as provided in the constructor.
     */
    @Override
    public Object getObject() {
        return domainObject;
    }

    /**
     * Replaces the {@link #getObject() domain object}.
     */
    @Override
    public void replacePojo(final Object pojo) {
        this.domainObject = pojo;
    }

    /**
     * Whether the object is persisted.
     * 
     * <p>
     * As per the {@link PersistenceState} provided in the constructor.
     */
    @Override
    public boolean isPersistent() {
        return persistenceState.isPersistent();
    }

    /**
     * Whether the object is not persisted.
     * 
     * <p>
     * As per the {@link PersistenceState} provided in the constructor.
     */
    @Override
    public boolean isTransient() {
        return persistenceState.isTransient();
    }


    @Override
    public String titleString() {
        final ObjectSpecification specification = getSpecification();
        if (specification.isCollection()) {
            return "A collection of " + (" " + specification.getPluralName()).toLowerCase();
        }
        // TODO do we want to localize titles for embedded work?
        final String title = specification.getTitle(this, null);
        if (title != null) {
            return title;
        }
        return "A " + specification.getSingularName().toLowerCase();
    }

    @Override
    public ObjectSpecification getElementSpecification() {
        if (elementSpecificationProvider == null) {
            return null;
        }
        return elementSpecificationProvider.getElementType();
    }

    @Override
    public void setElementSpecificationProvider(final ElementSpecificationProvider elementSpecificationProvider) {
        this.elementSpecificationProvider = elementSpecificationProvider;
    }

    // /////////////////////////////////////////////////////////
    // Methods specified to ObjectAdapter (as opposed to
    // ObjectMetaModel) do not need to be implemented.
    // /////////////////////////////////////////////////////////

    /**
     * Not supported, always throws an exception.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public ResolveState getResolveState() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported, always throws an exception.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public void changeState(final ResolveState newState) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported, does nothing.
     */
    @Override
    public Oid getOid() {
        return null;
    }

    /**
     * No-op, since have no {@link Oid}s.
     */
    @Override
    public void replaceOid(Oid persistedOid) {
    }

    @Override
    public boolean isParented() {
        return false;
    }

    @Override
    public boolean isAggregated() {
        return false;
    }

    @Override
    public boolean isValue() {
        return false;
    }

    /**
     * Not supported, always throws an exception.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public String getIconName() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported, always throws an exception.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public Instance getInstance(final Specification specification) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported, always throws an exception.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public Version getVersion() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported, always throws an exception.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public void checkLock(final Version version) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported, always throws an exception.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public void setVersion(final Version version) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported, always throws an exception.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public void fireChangedEvent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectAdapter getAggregateRoot(ObjectAdapterLookup objectAdapterLookup) {
        throw new UnsupportedOperationException();
    }



}
