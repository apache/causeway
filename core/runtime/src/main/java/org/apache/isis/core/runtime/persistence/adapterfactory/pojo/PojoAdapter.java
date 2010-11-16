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


package org.apache.isis.core.runtime.persistence.adapterfactory.pojo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.apache.log4j.Logger;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.Instance;
import org.apache.isis.core.metamodel.adapter.InstanceAbstract;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Specification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.ConcurrencyException;
import org.apache.isis.core.runtime.persistence.PersistenceSession;




public class PojoAdapter extends InstanceAbstract implements ObjectAdapter {
    
    private final static Logger LOG = Logger.getLogger(PojoAdapter.class);
    
    private static final int INCOMPLETE_COLLECTION = -1;
    
    private Object pojo;
    
    private transient ResolveState resolveState;
    
    private Oid oid;
    private Version version;
    
    private String defaultTitle;

    /**
     * Overridden {@link TypeOfFacet} (if any)
     */
    private TypeOfFacet typeOfFacet;

    /////////////////////////////////////////////////////////////////////
    // Constructor, finalizer
    /////////////////////////////////////////////////////////////////////

    public PojoAdapter(final Object pojo, final Oid oid) {
        if (pojo instanceof ObjectAdapter) {
            throw new IsisException("Adapter can't be used to adapt an adapter: " + pojo);
        }
        this.pojo = pojo;
        this.oid = oid;
        resolveState = ResolveState.NEW;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (LOG.isDebugEnabled()) {
        	
        	// be careful not to touch the pojo
        	// this method is called by the FinalizerThread so could be called at any time.
        	// for Hibernate-based object stores (and similar), it may no longer be valid to
        	// touch the pojo (although arguably, closing the session should detach these pojos)

        	// we also mustn't touch the adapter's specification.  That's because the loading
        	// of specifications isn't threadsafe, due to the way in which we put non-introspected
        	// specifications into the SpecificationCache to prevent infinite loops. 
        	
        	// better safe than sorry, though
        	// LOG.debug("finalizing pojo, oid: " + getOid());
        }
    }

    /////////////////////////////////////////////////////////////////////
    // Specification
    /////////////////////////////////////////////////////////////////////

    @Override
    protected ObjectSpecification loadSpecification() {
        ObjectSpecification specification = getReflector().loadSpecification(getObject().getClass());
        this.defaultTitle = "A" + (" " + specification.getSingularName()).toLowerCase();
        return specification;
    }

    /**
     * Downcasts {@link #getSpecification()}.
     */
    @Override
    public final ObjectSpecification getSpecification() {
    	ObjectSpecification specification = (ObjectSpecification) super.getSpecification();
		return specification;
    }


    /////////////////////////////////////////////////////////////////////
    // Object, replacePojo
    /////////////////////////////////////////////////////////////////////

    public Object getObject() {
        return pojo;
    }
    
    /**
     * Sometimes it is necessary to manage the replacement of the underlying domain object (by another
     * component such as an object store). This method allows the adapter to be kept while the domain object
     * is replaced.
     */
    public void replacePojo(final Object pojo) {
        this.pojo = pojo;
    }

    

    /////////////////////////////////////////////////////////////////////
    // ResolveState, changeState
    /////////////////////////////////////////////////////////////////////

    public ResolveState getResolveState() {
        return resolveState;
    }


    public void changeState(final ResolveState newState) {
        
        boolean validToChangeTo = resolveState.isValidToChangeTo(newState);
        // don't call toString() since that could hit titleString() and we might be
        // in the process of transitioning to ghost
        Assert.assertTrue("oid= " + this.getOid() + "; can't change from " + resolveState.name() + " to " + newState.name(), validToChangeTo);

        if (LOG.isDebugEnabled()) {
            String oidString;
            if (oid == null) {
                oidString = "";
            } else {
                // don't call toString() in case in process of transitioning to ghost
                oidString = "for " + this.getOid() + " ";
            }
            LOG.debug(oidString + "changing resolved state to " + newState.name());
        }
        resolveState = newState;
    }

    private boolean elementsLoaded() {
        return isTransient() || this.getResolveState().isResolved();
    }


    /////////////////////////////////////////////////////////////////////
    // isPersistent, isTransient
    /////////////////////////////////////////////////////////////////////

    /**
     * Just delegates to {@link #getResolveState() resolve state}.
     * 
     * @see ResolveState#isPersistent()
     * @see #isTransient()
     */
	public boolean isPersistent() {
		return getResolveState().isPersistent();
	}

    /**
     * Just delegates to {@link #getResolveState() resolve state}.
     * 
     * @see ResolveState#isTransient()
     * @see #isPersistent()
     */
	public boolean isTransient() {
		return getResolveState().isTransient();
	}



    /////////////////////////////////////////////////////////////////////
    // Oid
    /////////////////////////////////////////////////////////////////////

    public Oid getOid() {
        return oid;
    }

    protected void setOid(final Oid oid) {
        Ensure.ensureThatArg(oid, is(notNullValue()));
        this.oid = oid;
    }

    public boolean isAggregated() {
        return getOid() instanceof AggregatedOid;
    }


    /////////////////////////////////////////////////////////////////////
    // Version
    /////////////////////////////////////////////////////////////////////

    public Version getVersion() {
        return version;
    }

    public void checkLock(final Version version) {
        if (this.version != null && this.version.different(version)) {
            LOG.info("concurrency conflict on " + this + " (" + version + ")");
            throw new ConcurrencyException(this, version);
        }
    }

    public void setOptimisticLock(final Version version) {
        if (shouldSetVersion(version)) {
            this.version = version;
        }
    }

    private boolean shouldSetVersion(final Version version) {
        return this.version == null || version == null || version.different(this.version);
    }


    /////////////////////////////////////////////////////////////////////
    // Title, toString
    /////////////////////////////////////////////////////////////////////

    /**
     * Returns the title from the underlying business object.
     * 
     * <p>
     * If the object has not yet been resolved the specification will be asked for a unresolved title, 
     * which could of been persisted by the persistence mechanism. If either of the above provides null 
     * as the title then this method will return a title relating to the name of the object type, 
     * e.g. "A Customer", "A Product".
     */
    public String titleString() {
        if (getSpecification().isCollection()) {
            final CollectionFacet facet = getSpecification().getFacet(CollectionFacet.class);
            return collectionTitleString(facet);
        } else {
            return objectTitleString();
        }
    }

    private String objectTitleString() {
        final ResolveState resolveState = getResolveState();
        if (resolveState.isNew()) {
            return "";
        } else {
            final ObjectSpecification specification = getSpecification();
            String title = specification.getTitle(this);
            if (title == null) {
                if (resolveState.isGhost()) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("attempting to use unresolved object; resolving it immediately: oid=" + this.getOid());
                    }
                    getObjectPersistor().resolveImmediately(this);
                }
            }
            if (title == null) {
                title = getDefaultTitle();
            }
            return title;
            }
    }

    private String collectionTitleString(final CollectionFacet facet) {
        final int size = elementsLoaded() ? facet.size(this) : INCOMPLETE_COLLECTION;
        final TypeOfFacet typeFacet = getTypeOfFacet();
        final ObjectSpecification elementSpecification = typeFacet == null ? null : typeFacet.valueSpec();
        if (elementSpecification == null || elementSpecification.getFullName().equals(Object.class.getName())) {
            switch (size) {
            case -1:
                return "Objects";
            case 0:
                return "No objects";
            case 1:
                return "1 object";
            default:
                return size + " objects";
            }
        } else {
            switch (size) {
            case -1:
                return elementSpecification.getPluralName();
            case 0:
                return "No " + elementSpecification.getPluralName();
            case 1:
                return "1 " + elementSpecification.getSingularName();
            default:
                return size + " " + elementSpecification.getPluralName();
            }
        }
    }

    @Override
    public synchronized String toString() {
        final ToString str = new ToString(this);
        toString(str);

        // don't do title of any entities.  For persistence entities, might forces an unwanted resolve 
        // of the object.  For transient objects, may not be fully initialized.
        
        str.append("pojo-toString", pojo.toString());
        str.appendAsHex("pojo-hash", pojo.hashCode());
        return str.toString();
    }

    protected String getDefaultTitle() {
        return defaultTitle;
    }

    protected void toString(final ToString str) {
        str.append(resolveState.code());
        final Oid oid = getOid();
        if (oid != null) {
            str.append(":");
            str.append(oid.toString());
        } else {
            str.append(":-");
        }
        str.setAddComma();
        if (getSpecificationNoLoad() == null) {
            str.append("class", getObject().getClass().getName());
        } else {
            str.append("specification", getSpecification().getShortName());
        }
        str.append("version", version == null ? null : version.sequence());
    }

    
    /////////////////////////////////////////////////////////////////////
    // IconName
    /////////////////////////////////////////////////////////////////////

    /**
     * Returns the name of the icon to use to represent this object.
     */
    public String getIconName() {
        return getSpecification().getIconName(this);
    }


    /////////////////////////////////////////////////////////////////////
    // TypeOfFacet
    /////////////////////////////////////////////////////////////////////

    public TypeOfFacet getTypeOfFacet() {
        if (typeOfFacet == null) {
            return getSpecification().getFacet(TypeOfFacet.class);
        }
        return typeOfFacet;
    }

    public void setTypeOfFacet(final TypeOfFacet typeOfFacet) {
        this.typeOfFacet = typeOfFacet;
    }



    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////
    
    /**
     * Not supported by this implementation.
     */
    public Instance getInstance(Specification specification) {
        throw new UnsupportedOperationException();
    }


    /////////////////////////////////////////////////////////////////////
    // Fire Changes
    /////////////////////////////////////////////////////////////////////

    /**
     * Guaranteed to be called whenever this object is known to have changed
     * (specifically, by the <tt>ObjectStorePersistor</tt>).
     * 
     * <p>
     * This implementation does nothing, but subclasses (for example <tt>PojoAdapterX</tt>)
     * might provide listeners. 
     */
    public void fireChangedEvent() {
    }

    
    /////////////////////////////////////////////////////////////////////
    // Dependencies (from singleton)
    /////////////////////////////////////////////////////////////////////
    
    private SpecificationLoader getReflector() {
        return IsisContext.getSpecificationLoader();
    }

    private PersistenceSession getObjectPersistor() {
        return IsisContext.getPersistenceSession();
    }



}
