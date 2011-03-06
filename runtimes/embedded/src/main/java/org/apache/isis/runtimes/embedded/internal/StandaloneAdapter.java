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
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ElementSpecificationProvider;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectMetaModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Specification;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManager;

/**
 * Only provides a concrete implementation of the methods corresponding to 
 * the {@link ObjectMetaModel} interface.
 */
public class StandaloneAdapter implements ObjectAdapter {

	private final ObjectSpecification spec;
	private final PersistenceState persistenceState;
	private Object domainObject;

    private ElementSpecificationProvider elementSpecificationProvider;

	public StandaloneAdapter(ObjectSpecification spec, Object domainObject, PersistenceState persistenceState) {
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
    public void replacePojo(Object pojo) {
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

	/**
	 * Always returns <tt>null</tt>.
	 */
	@Override
    public ObjectAdapter getOwner() {
		return null;
	}

	@Override
    public String titleString() {
		final ObjectSpecification specification = getSpecification();
        if (specification.isCollection()) {
            return "A collection of " + (" " + specification.getPluralName()).toLowerCase();
        } 
        // TODO do we want to localize titles  for embedded work?
        String title = specification.getTitle(this, null);
        if (title != null) {
        	return title;
        }
        return "A " + specification.getSingularName().toLowerCase();
	}


    @Override
    public ObjectSpecification getElementSpecification() {
        if(elementSpecificationProvider==null) {
            return null;
        }
        return elementSpecificationProvider.getElementType();
    }

    @Override
    public void setElementSpecificationProvider(ElementSpecificationProvider elementSpecificationProvider) {
        this.elementSpecificationProvider = elementSpecificationProvider;
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
    public void changeState(ResolveState newState) {
		throw new UnsupportedOperationException();
	}


	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
    public Oid getOid() {
		return null;
	}

	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
    public boolean isAggregated() {
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
    public Instance getInstance(Specification specification) {
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
    public void checkLock(Version version) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported, always throws an exception.
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
    public void setOptimisticLock(Version version) {
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
    public ObjectAdapter getAggregateRoot() {
        if (getSpecification().isAggregated()) {
            Oid parentOid = ((AggregatedOid) this.getOid()).getParentOid();
            return getAdapterManager().getAdapterFor(parentOid);
        } else {
            return this;
        }
    }

    //////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    //////////////////////////////////////////////////////////////////
    
    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }


}
