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

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.security.authentication.AuthenticationSession;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PojoAdapter implements ObjectAdapter {

    private final AuthenticationSession authenticationSession;
    private final SpecificationLoader specificationLoader;
    private final PersistenceSession persistenceSession;

    private final Object pojo;
    private final Oid oid;

    // -- FACTORIES
    
    public static PojoAdapter of(
            final Object pojo,
            final Oid oid) {
        
        return of(pojo, oid, IsisSession.currentOrElseNull(), null);
    }
    
    public static PojoAdapter ofValue(Serializable value) {
        val oid = Oid.Factory.value();
        return PojoAdapter.of(value, oid);
    }
    
    public static ObjectAdapter ofTransient(Object pojo, ObjectSpecId specId) {
        val identifier = UUID.randomUUID().toString();
        return PojoAdapter.of(pojo, Oid.Factory.transientOf(specId, identifier));
    }
    
    public static PojoAdapter of(
            final Object pojo,
            final Oid oid,
            final IsisSession isisSession,
            final PersistenceSession persistenceSession) {
        
        val authenticationSession = isisSession.getAuthenticationSession(); 
        val specificationLoader = isisSession.getSpecificationLoader();
        
        return new PojoAdapter(pojo, oid, authenticationSession, specificationLoader, persistenceSession);
    }
    
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
        
        Objects.requireNonNull(pojo);

        this.specificationLoader = specificationLoader;
        this.authenticationSession = authenticationSession;
        this.persistenceSession = persistenceSession;
        
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
    
    final _Lazy<ObjectSpecification> objectSpecification = _Lazy.of(this::loadSpecification);

    @Override
    public ObjectSpecification getSpecification() {
        return objectSpecification.get();
    }

    private ObjectSpecification loadSpecification() {
        final Class<?> aClass = getPojo().getClass();
        final ObjectSpecification specification = specificationLoader.loadSpecification(aClass);
        return specification;
    }

    // -- getOid
    
    @Override
    public Oid getOid() {
        return oid;
    }

    // -- PREDICATES
    
    @Override
    public boolean isTransient() {
        val spec = getSpecification();
        if(spec.isBean() || spec.isViewModel()) {
            // services and view models are treated as persistent objects
            return false;
        }
        val state = persistenceSession.stateOf(this);
        return state.isDetached();
    }

    @Override
    public boolean isRepresentingPersistent() {
        val spec = getSpecification();
        if(spec.isBean() || spec.isViewModel()) {
            // services and view models are treated as persistent objects
            return true;
        }
        val state = persistenceSession.stateOf(this);
        val isRepresentingPersistent = state.isAttached() || state.isDestroyed();
        return isRepresentingPersistent;
    }

    @Override
    public boolean isDestroyed() {
        val spec = getSpecification();
        if(spec.isBean() || spec.isViewModel() || spec.isValue()) {
            // services and view models are treated as persistent objects
            return false;
        }
        val state = persistenceSession.stateOf(this);
        return state.isDestroyed();
    }

    // -- getAggregateRoot
    @Override
    public ObjectAdapter getAggregateRoot() {
        if(!isParentedCollection()) {
            return this;
        }
        val collectionOid = (ParentedOid) oid;
        val rootOid = collectionOid.getParentOid();
        val rootAdapter = persistenceSession.adapterFor(rootOid);
        return rootAdapter;
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
                log.info("concurrency conflict detected on {} ({})", thisOid, otherVersion);
                final String currentUser = authenticationSession.getUserName();
                throw new ConcurrencyException(currentUser, thisOid, thisVersion, otherVersion);
            } else {
                log.info("concurrency conflict detected but suppressed, on {} ({})", thisOid, otherVersion );
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
        if (!objectSpecification.isMemoized()) {
            str.append("class", getPojo().getClass().getName());
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


}
