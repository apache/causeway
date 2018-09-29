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

import static org.apache.isis.commons.internal.base._With.requires;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.InstanceAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Specification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

public final class PojoAdapter extends InstanceAbstract implements ObjectAdapter {

    private final static Logger LOG = LoggerFactory.getLogger(PojoAdapter.class);

    // -- Constructor, fields

    private final AuthenticationSession authenticationSession;
    private final SpecificationLoader specificationLoader;
    private final PersistenceSession persistenceSession;

    private final Object pojo;
    private final Oid oid;
    
    public static PojoAdapter of(
            final Object pojo,
            final Oid oid,
            final AuthenticationSession authenticationSession,
            final SpecificationLoader specificationLoader,
            final PersistenceSession persistenceSession) {
        return new PojoAdapter(pojo, oid, authenticationSession, specificationLoader, persistenceSession);
    }

    private PojoAdapter(
            final Object pojo,
            final Oid oid,
            final AuthenticationSession authenticationSession,
            final SpecificationLoader specificationLoader,
            final PersistenceSession persistenceSession) {

        this.persistenceSession = persistenceSession;
        this.specificationLoader = specificationLoader;
        this.authenticationSession = authenticationSession;
        
        if (pojo instanceof ObjectAdapter) {
            throw new IsisException("ObjectAdapter can't be used to wrap an ObjectAdapter: " + pojo);
        }
        if (pojo instanceof Oid) {
            throw new IsisException("ObjectAdapter can't be used to wrap an Oid: " + pojo);
        }
        
        this.pojo = pojo;
        this.oid = requires(oid, "oid");
    }
    
    @Override
    public Object getPojo() {
        return pojo;
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

    // -- getOid
    @Override
    public Oid getOid() {
        return oid;
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
        ParentedOid collectionOid = (ParentedOid) oid;
        final Oid rootOid = collectionOid.getParentOid();
        ObjectAdapter rootadapter = persistenceSession.adapterFor(rootOid);
        return rootadapter;
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

            if(ConcurrencyChecking.isCurrentlyEnabled()) {
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

    // -- getInstance (unsupported for this impl)

    /**
     * Not supported by this implementation.
     */
    @Override
    public Instance getInstance(final Specification specification) {
        throw new UnsupportedOperationException();
    }
    

}
