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

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ElementSpecificationProvider;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.InstanceAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Specification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

public class PojoAdapter extends InstanceAbstract implements ObjectAdapter {

    private final static Logger LOG = LoggerFactory.getLogger(PojoAdapter.class);

    // -- Constructor, fields

    private final AuthenticationSession authenticationSession;
    private final SpecificationLoader specificationLoader;
    private final PersistenceSession persistenceSession;

    /**
     * can be {@link #replacePojo(Object) replace}d.
     */
    private Object pojo;
    /**
     * can be {@link #replaceOid(Oid) replace}d.
     */
    private final Oid oid;

    /**
     * only for standalone or parented collections.
     */
    private ElementSpecificationProvider elementSpecificationProvider;

    public PojoAdapter(
            final Object pojo,
            final Oid oid,
            final AuthenticationSession authenticationSession,
            final SpecificationLoader specificationLoader,
            final PersistenceSession persistenceSession) {

        this.persistenceSession = persistenceSession;
        this.specificationLoader = specificationLoader;
        this.authenticationSession = authenticationSession;
        
        if (pojo instanceof ObjectAdapter) {
            throw new IsisException("Adapter can't be used to adapt an adapter: " + pojo);
        }
        this.pojo = pojo;
        this.oid = oid;
    }


    // -- getSpecification

    /**
     * Downcasts {@link #getSpecification()}.
     */
    @Override
    public ObjectSpecification getSpecification() {
        return (ObjectSpecification) super.getSpecification();
    }

    @Override
    protected ObjectSpecification loadSpecification() {
        final Class<?> aClass = getObject().getClass();
        final ObjectSpecification specification = specificationLoader.loadSpecification(aClass);
        return specification;
    }

    // -- getObject, replacePojo
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

    // -- getOid
    @Override
    public Oid getOid() {
        return oid;
    }

    // -- isParentedCollection, isValue

    @Override
    public boolean isParentedCollection() {
        return oid instanceof ParentedCollectionOid;
    }

    @Override
    public boolean isValue() {
        return oid == null;
    }

    // -- isTransient, representsPersistent, isDestroyed
    
    @Override
    public boolean isTransient() {
        if(getSpecification().isService() || getSpecification().isViewModel()) {
            // services and view models are treated as persistent objects
            return false;
        }
        return persistenceSession.isTransient(pojo); 
    }

    @Override
    public boolean representsPersistent() {
        if(getSpecification().isService() || getSpecification().isViewModel()) {
            // services and view models are treated as persistent objects
            return true;
        }
        return persistenceSession.isRepresentingPersistent(pojo);
    }

    @Override
    public boolean isDestroyed() {
        if(getSpecification().isService() || getSpecification().isViewModel()) {
            // services and view models are treated as persistent objects
            return false;
        }
        return persistenceSession.isDestroyed(pojo);
    }


    // -- getAggregateRoot
    @Override
    public ObjectAdapter getAggregateRoot() {
        if(!isParentedCollection()) {
            return this;
        }
        ParentedCollectionOid collectionOid = (ParentedCollectionOid) oid;
        return persistenceSession.getAggregateRoot(collectionOid);
    }

    // -- getVersion, setVersion, checkLock

    @Override
    public Version getVersion() {
        if(isParentedCollection()) {
            return getAggregateRoot().getVersion();
        } else {
            return getOid().getVersion();
        }
    }


    @Override
    public void checkLock(final Version otherVersion) {
        if(isParentedCollection()) {
            getAggregateRoot().checkLock(otherVersion);
            return;
        }

        Oid thisOid = getOid();
        final Version thisVersion = thisOid.getVersion();

        // check for exception, but don't throw if suppressed through thread-local
        if(thisVersion != null &&
                otherVersion != null &&
                thisVersion.different(otherVersion)) {

            if(AdapterManager.ConcurrencyChecking.isCurrentlyEnabled()) {
                LOG.info("concurrency conflict detected on {} ({})", thisOid, otherVersion);
                final String currentUser = authenticationSession.getUserName();
                throw new ConcurrencyException(currentUser, thisOid, thisVersion, otherVersion);
            } else {
                LOG.info("concurrency conflict detected but suppressed, on {} ({})", thisOid, otherVersion );
            }
        }
    }

    @Override
    public void setVersion(final Version version) {
        if(isParentedCollection()) {
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



    // -- titleString
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
        if (getObject() instanceof String) {
            return (String) getObject();
        }
        final ObjectSpecification specification = getSpecification();
        String title = specification.getTitle(contextAdapterIfAny, this);

        if (title == null) {
            title = getDefaultTitle();
        }
        return title;
    }

    private String collectionTitleString(final CollectionFacet facet) {
        final int size = facet.size(this);
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
    public String toString() {
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
        return "A" + (" " + getSpecification().getSingularName()).toLowerCase();
    }

    protected void toString(final ToString str) {
        str.append(aggregateResolveStateCode());
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

    private String aggregateResolveStateCode() {

        // this is an approximate re-implementation...
        final Oid oid = getOid();
        if(oid != null) {
            if(oid.isPersistent()) return "P";
            if(oid.isTransient()) return "T";
            if(oid.isViewModel()) return "V";
        }
        return "S"; // standalone adapter (value)
    }


    // -- iconName

    /**
     * Returns the name of the icon to use to represent this object.
     */
    @Override
    public String getIconName() {
        return getSpecification().getIconName(this);
    }



    // -- elementSpecification

    @Override
    public ObjectSpecification getElementSpecification() {
        if (elementSpecificationProvider == null) {
            return null;
        }
        return elementSpecificationProvider.getElementType();
    }

    /**
     * Called whenever there is a {@link org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet} present.
     *
     * <p>
     *     Specifically, if an action which has been annotated (is copied by {@link org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet action invocation facet}), and for a parented collection
     *     (is copied by the {@link PersistenceSession5} when {@link PersistenceSession5#adapterFor(Object, ObjectAdapter, OneToManyAssociation) creating} an adapter for a collection.
     * </p>
     */
    @Override
    public void setElementSpecificationProvider(final ElementSpecificationProvider elementSpecificationProvider) {
        this.elementSpecificationProvider = elementSpecificationProvider;
    }


    // -- getInstance (unsupported for this impl)

    /**
     * Not supported by this implementation.
     */
    @Override
    public Instance getInstance(final Specification specification) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectAdapter withOid(RootOid newOid) {
        return new PojoAdapter(pojo, newOid, authenticationSession, specificationLoader, persistenceSession);
    }

}
