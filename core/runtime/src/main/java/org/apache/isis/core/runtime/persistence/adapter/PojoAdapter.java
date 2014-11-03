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

package org.apache.isis.core.runtime.persistence.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.*;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class PojoAdapter extends InstanceAbstract implements ObjectAdapter {

    private final static Logger LOG = LoggerFactory.getLogger(PojoAdapter.class);

    private static final int INCOMPLETE_COLLECTION = -1;

    private final SpecificationLoader specificationLoader;
    private final AdapterManager objectAdapterLookup;
    private final Localization localization;
    
    private Object pojo;
    private Oid oid;
    private ResolveState resolveState;

    private String defaultTitle;

    private ElementSpecificationProvider elementSpecificationProvider;

    private AuthenticationSession authenticationSession;


    // ///////////////////////////////////////////////////////////////////
    // Constructor, finalizer
    // ///////////////////////////////////////////////////////////////////

    public PojoAdapter(final Object pojo, final Oid oid, SpecificationLoader specificationLoader, AdapterManager adapterManager, Localization localization, AuthenticationSession authenticationSession) {
        this.specificationLoader = specificationLoader;
        this.objectAdapterLookup = adapterManager;
        this.localization = localization;
        this.authenticationSession = authenticationSession;
        
        if (pojo instanceof ObjectAdapter) {
            throw new IsisException("Adapter can't be used to adapt an adapter: " + pojo);
        }
        this.pojo = pojo;
        this.oid = oid;
        resolveState = ResolveState.NEW;
    }

    
    // ///////////////////////////////////////////////////////////////////
    // Specification
    // ///////////////////////////////////////////////////////////////////

    @Override
    protected ObjectSpecification loadSpecification() {
        final ObjectSpecification specification = specificationLoader.loadSpecification(getObject().getClass());
        this.defaultTitle = "A" + (" " + specification.getSingularName()).toLowerCase();
        return specification;
    }

    /**
     * Downcasts {@link #getSpecification()}.
     */
    @Override
    public ObjectSpecification getSpecification() {
        return (ObjectSpecification) super.getSpecification();
    }

    // ///////////////////////////////////////////////////////////////////
    // Object, replacePojo
    // ///////////////////////////////////////////////////////////////////

    @Override
    public Object getObject() {
        return pojo;
    }

    /**
     * Sometimes it is necessary to manage the replacement of the underlying
     * domain object (by another component such as an object store). This method
     * allows the adapter to be kept while the domain object is replaced.
     */
    @Override
    public void replacePojo(final Object pojo) {
        this.pojo = pojo;
    }


    // ///////////////////////////////////////////////////////////////////
    // ResolveState, changeState
    // ///////////////////////////////////////////////////////////////////

    @Override
    public ResolveState getResolveState() {
        return aggregateResolveState();
    }


    private ResolveState aggregateResolveState() {
        return isAggregated() ? 
                ((PojoAdapter)getAggregateRoot()).aggregateResolveState() : 
                resolveState;
    }



    @Override
    public void changeState(final ResolveState newState) {
        if(isAggregated()) {
            return; // no-op for aggregated objects.
        }

        final boolean validToChangeTo = resolveState.isValidToChangeTo(newState);
        // don't call toString() since that could hit titleString() and we might
        // be in the process of transitioning to ghost
        Assert.assertTrue("oid= " + this.getOid() + "; can't change from " + resolveState.name() + " to " + newState.name(), validToChangeTo);

        if (LOG.isTraceEnabled()) {
            String oidString;
            if (oid == null) {
                oidString = "";
            } else {
                // don't call toString() in case in process of transitioning to
                // ghost
                oidString = "for " + this.getOid() + " ";
            }
            LOG.trace(oidString + "changing resolved state to " + newState.name());
        }
        resolveState = newState;
    }

    private boolean elementsLoaded() {
        return isTransient() || this.isResolved();
    }

    // ///////////////////////////////////////////////////////////////////
    // ResolveState
    // ///////////////////////////////////////////////////////////////////

    /**
     * Just delegates to {@link #aggregateResolveState() resolve state}.
     * 
     * @see ResolveState#representsPersistent()
     * @see #isTransient()
     */
    @Override
    public boolean representsPersistent() {
        return aggregateResolveState().representsPersistent();
    }


    /**
     * Just delegates to {@link #aggregateResolveState() resolve state}.
     * 
     * @see ResolveState#isTransient()
     * @see #representsPersistent()
     */
    @Override
    public boolean isTransient() {
        return aggregateResolveState().isTransient();
    }

    @Override
    public boolean isNew() {
        return aggregateResolveState().isNew();
    }

    @Override
    public boolean isResolving() {
        return aggregateResolveState().isResolving();
    }

    @Override
    public boolean isResolved() {
        return aggregateResolveState().isResolved();
    }

    @Override
    public boolean isGhost() {
        return aggregateResolveState().isGhost();
    }

    @Override
    public boolean isUpdating() {
        return aggregateResolveState().isUpdating();
    }

    @Override
    public boolean isDestroyed() {
        return aggregateResolveState().isDestroyed();
    }


    @Override
    public boolean canTransitionToResolving() {
        return aggregateResolveState().canTransitionToResolving();
    }

    /**
     * If {@link #isGhost()}, then will become resolved.
     */
    @Override
    public void markAsResolvedIfPossible() {
        if (!canTransitionToResolving()) {
            return;
        } 
        changeState(ResolveState.RESOLVING);
        changeState(ResolveState.RESOLVED);
    }


    
    // ///////////////////////////////////////////////////////////////////
    // Oid
    // ///////////////////////////////////////////////////////////////////

    @Override
    public Oid getOid() {
        return oid;
    }

    @Override
    public void replaceOid(Oid persistedOid) {
        Ensure.ensureThatArg(oid, is(notNullValue())); // values have no oid, so cannot be replaced 
        this.oid = persistedOid;
    }

    @Override
    public boolean isParented() {
        return oid instanceof ParentedOid;
    }

    @Override
    public boolean isAggregated() {
        return oid instanceof AggregatedOid;
    }

    @Override
    public boolean isValue() {
        // equivalently: aggregateResolveState().isValue();
        return oid == null;
    }

    @Override
    public ObjectAdapter getAggregateRoot() {
        if(!isParented()) {
            return this;
        }
        ParentedOid parentedOid = (ParentedOid) oid;
        final Oid parentOid = parentedOid.getParentOid();
        ObjectAdapter parentAdapter = objectAdapterLookup.getAdapterFor(parentOid);
        if(parentAdapter == null) {
            final Oid parentOidNowPersisted = getPersistenceSession().remappedFrom(parentOid);
            parentAdapter = objectAdapterLookup.getAdapterFor(parentOidNowPersisted);
        }
        return parentAdapter;
    }

    

    

    // ///////////////////////////////////////////////////////////////////
    // Version 
    // (nb: delegates to parent if parented)
    // ///////////////////////////////////////////////////////////////////

    @Override
    public Version getVersion() {
        if(isParented()) {
            return getAggregateRoot().getVersion();
        } else {
            return getOid().getVersion();
        }
    }


    @Override
    public void checkLock(final Version otherVersion) {
        if(isParented()) {
            getAggregateRoot().checkLock(otherVersion);
            return;
        }
        
        Oid thisOid = getOid();
        final Version thisVersion = thisOid.getVersion();
        
        // check for exception, but don't throw if suppressed through thread-local
        if(thisVersion != null && 
           otherVersion != null && 
           thisVersion.different(otherVersion)) {
            
            if(ConcurrencyChecking.isCurrentlyEnabled()) {
                LOG.info("concurrency conflict detected on " + thisOid + " (" + otherVersion + ")");
                final String currentUser = getAuthenticationSession().getUserName();
                throw new ConcurrencyException(currentUser, thisOid, thisVersion, otherVersion);
            } else {
                LOG.warn("concurrency conflict detected but suppressed, on " + thisOid + " (" + otherVersion + ")");
            }
        }
    }

    @Override
    public void setVersion(final Version version) {
        if(isParented()) {
            // ignored
            return;
        }
        if (shouldSetVersion(version)) {
            RootOid rootOid = (RootOid) getOid(); // since not parented
            rootOid.setVersion(version);
        }
    }

    private boolean shouldSetVersion(final Version otherVersion) {
        final Version version = getOid().getVersion();
        return version == null || otherVersion == null || otherVersion.different(version);
    }

    // ///////////////////////////////////////////////////////////////////
    // Title, toString
    // ///////////////////////////////////////////////////////////////////

    /**
     * Returns the title from the underlying business object.
     * 
     * <p>
     * If the object has not yet been resolved the specification will be asked
     * for a unresolved title, which could of been persisted by the persistence
     * mechanism. If either of the above provides null as the title then this
     * method will return a title relating to the name of the object type, e.g.
     * "A Customer", "A Product".
     */
    @Override
    public String titleString() {
        return titleString(null);
    }

    @Override
    public String titleString(ObjectAdapter contextAdapterIfAny) {
        if (getSpecification().isParentedOrFreeCollection()) {
            final CollectionFacet facet = getSpecification().getFacet(CollectionFacet.class);
            return collectionTitleString(facet);
        } else {
            return objectTitleString(contextAdapterIfAny);
        }
    }

    private String objectTitleString(ObjectAdapter contextAdapterIfAny) {
        if (isNew()) {
            return "";
        } 
        if (getObject() instanceof String) {
            return (String) getObject();
        }
        final ObjectSpecification specification = getSpecification();
        String title = specification.getTitle(contextAdapterIfAny, this, localization);
        
        if (title == null) {
            title = getDefaultTitle();
        }
        return title;
    }

    private String collectionTitleString(final CollectionFacet facet) {
        final int size = elementsLoaded() ? facet.size(this) : INCOMPLETE_COLLECTION;
        final ObjectSpecification elementSpecification = getElementSpecification();
        if (elementSpecification == null || elementSpecification.getFullIdentifier().equals(Object.class.getName())) {
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

        // don't do title of any entities. For persistence entities, might
        // forces an unwanted resolve
        // of the object. For transient objects, may not be fully initialized.

        str.append("pojo-toString", pojo.toString());
        str.appendAsHex("pojo-hash", pojo.hashCode());
        return str.toString();
    }

    protected String getDefaultTitle() {
        return defaultTitle;
    }

    protected void toString(final ToString str) {
        str.append(aggregateResolveState().code());
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
            str.append("specification", getSpecification().getShortIdentifier());
        }
        if(getOid() != null) {
            final Version version = getOid().getVersion();
            str.append("version", version != null ? version.sequence() : null);
        }
    }

    // ///////////////////////////////////////////////////////////////////
    // IconName
    // ///////////////////////////////////////////////////////////////////

    /**
     * Returns the name of the icon to use to represent this object.
     */
    @Override
    public String getIconName() {
        return getSpecification().getIconName(this);
    }

    // ///////////////////////////////////////////////////////////////////
    // ElementType
    // ///////////////////////////////////////////////////////////////////

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

    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////

    /**
     * Not supported by this implementation.
     */
    @Override
    public Instance getInstance(final Specification specification) {
        throw new UnsupportedOperationException();
    }

    // ///////////////////////////////////////////////////////////////////
    // Fire Changes
    // ///////////////////////////////////////////////////////////////////


    @Override
    public boolean respondToChangesInPersistentObjects() {
        return aggregateResolveState().respondToChangesInPersistentObjects();
    }



    
    ////////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    ////////////////////////////////////////////////////////////////////

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }



}
