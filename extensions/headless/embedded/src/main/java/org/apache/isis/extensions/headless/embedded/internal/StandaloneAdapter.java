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


package org.apache.isis.extensions.headless.embedded.internal;

import org.apache.isis.metamodel.adapter.Instance;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectMetaModel;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.Specification;

/**
 * Only provides a concrete implementation of the methods corresponding to 
 * the {@link ObjectMetaModel} interface.
 */
public class StandaloneAdapter implements ObjectAdapter {

	private final ObjectSpecification spec;
	private final PersistenceState persistenceState;
	private Object domainObject;

    private TypeOfFacet typeOfFacet;

	public StandaloneAdapter(ObjectSpecification spec, Object domainObject, PersistenceState persistenceState) {
		this.spec = spec;
		this.domainObject = domainObject;
		this.persistenceState = persistenceState;
	}

	/**
	 * Returns the {@link ObjectSpecification} as provided in the constructor.
	 */
	public ObjectSpecification getSpecification() {
		return spec;
	}

	/**
	 * Returns the domain object as provided in the constructor.
	 */
	public Object getObject() {
		return domainObject;
	}
	
	/**
	 * Replaces the {@link #getObject() domain object}.
	 */
	public void replacePojo(Object pojo) {
		this.domainObject = pojo;
	}

	/**
	 * Whether the object is persisted.
	 * 
	 * <p>
	 * As per the {@link PersistenceState} provided in the constructor.
	 */
	public boolean isPersistent() {
		return persistenceState.isPersistent();
	}

	/**
	 * Whether the object is not persisted.
	 * 
	 * <p>
	 * As per the {@link PersistenceState} provided in the constructor.
	 */
	public boolean isTransient() {
		return persistenceState.isTransient();
	}

	/**
	 * Always returns <tt>null</tt>.
	 */
	public ObjectAdapter getOwner() {
		return null;
	}

	public String titleString() {
		final ObjectSpecification specification = getSpecification();
        if (specification.isCollection()) {
            return "A collection of " + (" " + specification.getPluralName()).toLowerCase();
        } 
        
        String title = specification.getTitle(this);
        if (title != null) {
        	return title;
        }
        return "A " + specification.getSingularName().toLowerCase();
	}


	/**
	 * Returns the {@link #setTypeOfFacet(TypeOfFacet) overridden} {@link TypeOfFacet}, else
	 * looks up from {@link #getSpecification() specification} (if any).
	 */
	public TypeOfFacet getTypeOfFacet() {
        if (typeOfFacet == null) {
            return getSpecification().getFacet(TypeOfFacet.class);
        }
        return typeOfFacet;
	}
	
	/**
	 * Override (or set the) {@link TypeOfFacet}.
	 */
	public void setTypeOfFacet(TypeOfFacet typeOfFacet) {
        this.typeOfFacet = typeOfFacet;
	}



	///////////////////////////////////////////////////////////
	// Methods specified to ObjectAdapter (as opposed to 
	// ObjectMetaModel) do not need to be implemented.
	///////////////////////////////////////////////////////////
	
	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public ResolveState getResolveState() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public void changeState(ResolveState newState) {
		throw new UnsupportedOperationException();
	}


	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public Oid getOid() {
		return null;
	}

	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public boolean isAggregated() {
		return false;
	}

	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public String getIconName() {
		throw new UnsupportedOperationException();
	}


	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public Instance getInstance(Specification specification) {
		throw new UnsupportedOperationException();
	}

	
	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public Version getVersion() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public void checkLock(Version version) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public void setOptimisticLock(Version version) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public void fireChangedEvent() {
		throw new UnsupportedOperationException();
	}

}
