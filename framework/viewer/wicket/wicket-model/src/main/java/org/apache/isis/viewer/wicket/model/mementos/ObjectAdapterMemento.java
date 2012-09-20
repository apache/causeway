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

package org.apache.isis.viewer.wicket.model.mementos;

import java.io.Serializable;

import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.memento.Memento;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.util.Oids;

public class ObjectAdapterMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Factory method
     */
    public static ObjectAdapterMemento createOrNull(final ObjectAdapter adapter) {
        if (adapter == null) {
            return null;
        }
        return new ObjectAdapterMemento(adapter);
    }

    /**
     * Factory method
     */
    public static ObjectAdapterMemento createPersistent(final RootOid rootOid) {
        return new ObjectAdapterMemento(rootOid);
    }


    enum Type {
        /**
         * The {@link ObjectAdapter} that this is the memento for directly has
         * an {@link EncodableFacet} (it is almost certainly a value), and so is
         * stored directly.
         */
        ENCODEABLE {
            @Override
            ObjectAdapter recreateAdapter(final ObjectAdapterMemento oam, ConcurrencyChecking concurrencyChecking) {
                ObjectSpecId objectSpecId = oam.objectSpecId;
                ObjectSpecification objectSpec = SpecUtils.getSpecificationFor(objectSpecId);
                final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
                return encodableFacet.fromEncodedString(oam.encodableValue);
            }

            @Override
            public boolean equals(ObjectAdapterMemento oam, ObjectAdapterMemento other) {
                return other.type == ENCODEABLE && oam.encodableValue.equals(other.encodableValue);
            }

            @Override
            public int hashCode(ObjectAdapterMemento oam) {
                return oam.encodableValue.hashCode();
            }

            @Override
            public String toString(final ObjectAdapterMemento oam) {
                return oam.encodableValue;
            }

            @Override
            public void resetVersion(ObjectAdapterMemento objectAdapterMemento) {
            }
        },
        /**
         * The {@link ObjectAdapter} that this is for is already known by its
         * (persistent) {@link Oid}.
         */
        PERSISTENT {
            @Override
            ObjectAdapter recreateAdapter(final ObjectAdapterMemento oam, ConcurrencyChecking concurrencyChecking) {
                TypedOid oid = getOidMarshaller().unmarshal(oam.persistentOidStr, TypedOid.class);
                final ObjectAdapter adapter = getAdapterManager().adapterFor(oid, concurrencyChecking);
                // reset version
                if(concurrencyChecking == ConcurrencyChecking.NO_CHECK) {
                    oam.persistentOidStr = oid.enString(getOidMarshaller());
                }
                return adapter;
            }

            @Override
            public void resetVersion(ObjectAdapterMemento oam) {
                // REVIEW: this may be redundant because recreateAdapter also guarantees the version will be reset.
                final ObjectAdapter adapter = recreateAdapter(oam, ConcurrencyChecking.NO_CHECK);
                Oid oid = adapter.getOid();
                oam.persistentOidStr = oid.enString(getOidMarshaller());
            }

            @Override
            public boolean equals(ObjectAdapterMemento oam, ObjectAdapterMemento other) {
                return other.type == PERSISTENT && oam.persistentOidStr.equals(other.persistentOidStr);
            }

            @Override
            public int hashCode(ObjectAdapterMemento oam) {
                return oam.persistentOidStr.hashCode();
            }

            @Override
            public String toString(final ObjectAdapterMemento oam) {
                return oam.persistentOidStr;
            }

        },
        /**
         * Uses Isis' own {@link Memento}, to capture the state of a transient
         * object.
         */
        TRANSIENT {
            /**
             * {@link ConcurrencyChecking} is ignored for transients.
             */
            @Override
            ObjectAdapter recreateAdapter(final ObjectAdapterMemento oam, ConcurrencyChecking concurrencyChecking) {
                return oam.transientMemento.recreateObject();
            }

            @Override
            public boolean equals(ObjectAdapterMemento oam, ObjectAdapterMemento other) {
                return other.type == TRANSIENT && oam.transientMemento.equals(other.transientMemento);
            }

            @Override
            public int hashCode(ObjectAdapterMemento oam) {
                return oam.transientMemento.hashCode();
            }
            
            @Override
            public String toString(final ObjectAdapterMemento oam) {
                return oam.transientMemento.toString();
            }

            @Override
            public void resetVersion(ObjectAdapterMemento objectAdapterMemento) {
            }
        };

        public synchronized ObjectAdapter getAdapter(final ObjectAdapterMemento nom, ConcurrencyChecking concurrencyChecking) {
            return recreateAdapter(nom, concurrencyChecking);
        }

        abstract ObjectAdapter recreateAdapter(ObjectAdapterMemento nom, ConcurrencyChecking concurrencyChecking);

        public abstract boolean equals(ObjectAdapterMemento oam, ObjectAdapterMemento other);
        public abstract int hashCode(ObjectAdapterMemento objectAdapterMemento);
        
        public abstract String toString(ObjectAdapterMemento adapterMemento);

        public abstract void resetVersion(ObjectAdapterMemento objectAdapterMemento);
    }

    private Type type;

    private final ObjectSpecId objectSpecId;
    private String titleHint;

    /**
     * The current value, if {@link Type#ENCODEABLE}.
     * 
     * <p>
     * Will be <tt>null</tt> otherwise.
     */
    private String encodableValue;
    
    /**
     * The current value, if {@link Type#PERSISTENT}.
     * 
     * <p>
     * Will be <tt>null</tt> otherwise.
     */
    private String persistentOidStr;

    /**
     * The current value, if {@link Type#TRANSIENT}.
     * 
     * <p>
     * Will be <tt>null</tt> otherwise.
     */
    private Memento transientMemento;

    private ObjectAdapterMemento(final RootOid rootOid) {
        Ensure.ensureThatArg(rootOid, Oids.isPersistent());
        this.persistentOidStr = rootOid.enString(getOidMarshaller());
        this.objectSpecId = rootOid.getObjectSpecId();
        this.type = Type.PERSISTENT;
    }

    private ObjectAdapterMemento(final ObjectAdapter adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("adapter cannot be null");
        }
        final ObjectSpecification specification = adapter.getSpecification();
        objectSpecId = specification.getSpecId();
        init(adapter);
        captureTitleHintIfPossible();
    }

    private void init(final ObjectAdapter adapter) {
        
        final ObjectSpecification specification = adapter.getSpecification();

        final EncodableFacet encodableFacet = specification.getFacet(EncodableFacet.class);
        final boolean isEncodable = encodableFacet != null;
        if (isEncodable) {
            encodableValue = encodableFacet.toEncodedString(adapter);
            type = Type.ENCODEABLE;
            return;
        }
        
        final RootOid oid = (RootOid) adapter.getOid();
        if (oid.isTransient()) {
            transientMemento = new Memento(adapter);
            type = Type.TRANSIENT;
            return;
        } 
        
        persistentOidStr = oid.enString(getOidMarshaller());
        type = Type.PERSISTENT;
    }

    public void resetVersion() {
        type.resetVersion(this);
    }
    

    
    public void captureTitleHintIfPossible() {
        if (this.titleHint != null) {
            return;
        } 
        
        // REVIEW: this check is because Wicket 6.0.0 seems to call onEndRequest prior to onDetach
        // so by the time we get here, there is no session to use...
        if(IsisContext.inSession()) {
            ObjectAdapter objectAdapter = this.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
            if (objectAdapter.isTitleAvailable()) {
                this.titleHint = objectAdapter.titleString();
            }
        }
    }

    public String getTitleHint() {
        return titleHint;
    }

    /**
     * Lazily looks up {@link ObjectAdapter} if required.
     * 
     * <p>
     * For transient objects, be aware that calling this method more than once
     * will cause the underlying {@link ObjectAdapter} to be recreated,
     * overwriting any changes that may have been made. In general then it's
     * best to call once and then hold onto the value thereafter. Alternatively,
     * can call {@link #setAdapter(ObjectAdapter)} to keep this memento in sync.
     */
    public ObjectAdapter getObjectAdapter(ConcurrencyChecking concurrencyChecking) {
        return type.getAdapter(this, concurrencyChecking);
    }

    /**
     * Updates the memento if the adapter's state has changed.
     * 
     * <p>
     * This is a no-op for
     * 
     * @param adapter
     */
    public void setAdapter(final ObjectAdapter adapter) {
        init(adapter);
    }

    public ObjectSpecId getObjectSpecId() {
        return objectSpecId;
    }

    
    @Override
    public int hashCode() {
        return type.hashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ObjectAdapterMemento) && type.equals(this, (ObjectAdapterMemento)obj);
    }

    @Override
    public String toString() {
        return type.toString(this);
    }


    //////////////////////////////////////////////////
    // Dependencies (from context)
    //////////////////////////////////////////////////
    
    private static AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

	public static OidMarshaller getOidMarshaller() {
		return IsisContext.getOidMarshaller();
	}



}
