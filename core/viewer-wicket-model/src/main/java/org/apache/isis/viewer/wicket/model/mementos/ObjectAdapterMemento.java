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
import java.util.List;

import com.google.common.base.Function;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.memento.Memento;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

public class ObjectAdapterMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final OidMarshaller OID_MARSHALLER = OidMarshaller.INSTANCE;

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

    public Bookmark asBookmark() {
        return bookmark;
    }

    enum Type {
        /**
         * The {@link ObjectAdapter} that this is the memento for directly has
         * an {@link EncodableFacet} (it is almost certainly a value), and so is
         * stored directly.
         */
        ENCODEABLE {
            @Override
            ObjectAdapter recreateAdapter(
                    final ObjectAdapterMemento oam,
                    final ConcurrencyChecking concurrencyChecking,
                    final PersistenceSession persistenceSession,
                    final SpecificationLoader specificationLoader) {
                ObjectSpecId objectSpecId = oam.objectSpecId;
                ObjectSpecification objectSpec = SpecUtils.getSpecificationFor(objectSpecId, specificationLoader);
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
            public void resetVersion(
                    ObjectAdapterMemento objectAdapterMemento,
                    final PersistenceSession persistenceSession, final SpecificationLoader specificationLoader) {
            }
        },
        /**
         * The {@link ObjectAdapter} that this is for is already known by its
         * (persistent) {@link Oid}.
         */
        PERSISTENT {
            @Override
            ObjectAdapter recreateAdapter(
                    final ObjectAdapterMemento oam,
                    ConcurrencyChecking concurrencyChecking,
                    final PersistenceSession persistenceSession, final SpecificationLoader specificationLoader) {
                RootOid oid = OID_MARSHALLER.unmarshal(oam.persistentOidStr, RootOid.class);
                try {
                    final ObjectAdapter adapter = persistenceSession.adapterFor(oid, concurrencyChecking);
                    return adapter;
                    
                } finally {
                    // a side-effect of AdapterManager#adapterFor(...) is that it will update the oid
                    // with the correct version, even when there is a concurrency exception
                    // we copy this updated oid string into our memento so that, if we retry, 
                    // we will succeed second time around

                    oam.persistentOidStr = oid.enString();
                }
            }

            @Override
            public void resetVersion(
                    ObjectAdapterMemento oam,
                    final PersistenceSession persistenceSession,
                    final SpecificationLoader specificationLoader) {
                // REVIEW: this may be redundant because recreateAdapter also guarantees the version will be reset.
                final ObjectAdapter adapter = recreateAdapter(
                        oam, ConcurrencyChecking.NO_CHECK, persistenceSession, specificationLoader);
                Oid oid = adapter.getOid();
                oam.persistentOidStr = oid.enString();
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
             * {@link AdapterManager.ConcurrencyChecking} is ignored for transients.
             */
            @Override
            ObjectAdapter recreateAdapter(
                    final ObjectAdapterMemento oam,
                    ConcurrencyChecking concurrencyChecking,
                    final PersistenceSession persistenceSession, final SpecificationLoader specificationLoader) {
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
            public void resetVersion(
                    ObjectAdapterMemento objectAdapterMemento,
                    final PersistenceSession persistenceSession, final SpecificationLoader specificationLoader) {
            }
        };

        public ObjectAdapter getAdapter(
                final ObjectAdapterMemento nom,
                ConcurrencyChecking concurrencyChecking,
                final PersistenceSession persistenceSession,
                final SpecificationLoader specificationLoader) {
            return recreateAdapter(nom, concurrencyChecking, persistenceSession, specificationLoader);
        }

        abstract ObjectAdapter recreateAdapter(
                ObjectAdapterMemento nom,
                ConcurrencyChecking concurrencyChecking,
                final PersistenceSession persistenceSession, final SpecificationLoader specificationLoader);

        public abstract boolean equals(ObjectAdapterMemento oam, ObjectAdapterMemento other);
        public abstract int hashCode(ObjectAdapterMemento objectAdapterMemento);
        
        public abstract String toString(ObjectAdapterMemento adapterMemento);

        public abstract void resetVersion(
                ObjectAdapterMemento objectAdapterMemento,
                final PersistenceSession persistenceSession, final SpecificationLoader specificationLoader);
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
     * The current value, if {@link Type#PERSISTENT}.
     *
     * <p>
     * Will be <tt>null</tt> otherwise.
     */
    private Bookmark bookmark;

    /**
     * The current value, if {@link Type#TRANSIENT}.
     * 
     * <p>
     * Will be <tt>null</tt> otherwise.
     */
    private Memento transientMemento;

    private ObjectAdapterMemento(final RootOid rootOid) {

        assert !rootOid.isTransient();

        this.persistentOidStr = rootOid.enString();
        this.bookmark = rootOid.asBookmark();
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

        persistentOidStr = oid.enString();
        bookmark = oid.asBookmark();
        type = Type.PERSISTENT;
    }

    public void resetVersion(
            final PersistenceSession persistenceSession, final SpecificationLoader specificationLoader) {
        type.resetVersion(this, persistenceSession, specificationLoader);
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
    public ObjectAdapter getObjectAdapter(
            final ConcurrencyChecking concurrencyChecking,
            final PersistenceSession persistenceSession, final SpecificationLoader specificationLoader) {
        return type.getAdapter(this, concurrencyChecking, persistenceSession, specificationLoader);
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

    /**
     * Analogous to {@link List#contains(Object)}, but does not perform
     * {@link ConcurrencyChecking concurrency checking} of the OID.
     */
    public boolean containedIn(
            List<ObjectAdapterMemento> list,
            final PersistenceSession persistenceSession,
            final SpecificationLoader specificationLoader) {
        // REVIEW: heavy handed, ought to be possible to just compare the OIDs
        // ignoring the concurrency checking
        final ObjectAdapter currAdapter = getObjectAdapter(ConcurrencyChecking.NO_CHECK, persistenceSession,
                specificationLoader);
        for (ObjectAdapterMemento each : list) {
            if(each == null) {
                continue;
            }
            final ObjectAdapter otherAdapter = each.getObjectAdapter(
                    ConcurrencyChecking.NO_CHECK, persistenceSession, specificationLoader);
            if(currAdapter == otherAdapter) {
                return true;
            }
        }
        return false;
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
        return asString();
    }

    public String asString() {
        return type.toString(this);
    }


    //////////////////////////////////////////////////
    // Functions
    //////////////////////////////////////////////////
    
    
    public final static class Functions {

        private Functions() {
        }

        public static Function<ObjectSpecification, ObjectSpecId> fromSpec() {
            return new Function<ObjectSpecification, ObjectSpecId>() {

                @Override
                public ObjectSpecId apply(final ObjectSpecification from) {
                    return from.getSpecId();
                }
            };
        }

        public static Function<OneToOneAssociation, PropertyMemento> fromProperty(
                final IsisSessionFactory isisSessionFactory) {
            return new Function<OneToOneAssociation, PropertyMemento>() {
                @Override
                public PropertyMemento apply(final OneToOneAssociation from) {
                    return new PropertyMemento(from, isisSessionFactory);
                }
            };
        }

        public static Function<OneToManyAssociation, CollectionMemento> fromCollection(
                final IsisSessionFactory isisSessionFactory) {
            return new Function<OneToManyAssociation, CollectionMemento>() {
                @Override
                public CollectionMemento apply(final OneToManyAssociation from) {
                    return new CollectionMemento(from, isisSessionFactory);
                }
            };
        }

        public static Function<ObjectAction, ActionMemento> fromAction() {
            return new Function<ObjectAction, ActionMemento>() {
                @Override
                public ActionMemento apply(final ObjectAction from) {
                    return new ActionMemento(from);
                }
            };
        }

        public static Function<ObjectActionParameter, ActionParameterMemento> fromActionParameter() {
            return new Function<ObjectActionParameter, ActionParameterMemento>() {
                @Override
                public ActionParameterMemento apply(final ObjectActionParameter from) {
                    return new ActionParameterMemento(from);
                }
            };
        }

        public static Function<Object, ObjectAdapterMemento> fromPojo(final AdapterManager adapterManager) {
            return new Function<Object, ObjectAdapterMemento>() {
                @Override
                public ObjectAdapterMemento apply(final Object pojo) {
                    final ObjectAdapter adapter = adapterManager.adapterFor(pojo);
                    return ObjectAdapterMemento.createOrNull(adapter);
                }
            };
        }

        public static Function<ObjectAdapter, ObjectAdapterMemento> fromAdapter() {
            return new Function<ObjectAdapter, ObjectAdapterMemento>() {
                @Override
                public ObjectAdapterMemento apply(final ObjectAdapter adapter) {
                    return ObjectAdapterMemento.createOrNull(adapter);
                }
            };
        }


        public static Function<ObjectAdapterMemento, ObjectAdapter> fromMemento(
                final ConcurrencyChecking concurrencyChecking,
                final PersistenceSession persistenceSession,
                final SpecificationLoader specificationLoader) {
            return new Function<ObjectAdapterMemento, ObjectAdapter>() {
                @Override
                public ObjectAdapter apply(final ObjectAdapterMemento from) {
                    try {
                        return from.getObjectAdapter(concurrencyChecking, persistenceSession, specificationLoader);
                    } catch (ObjectNotFoundException e) {
                        // this can happen if for example the object is not visible (due to the security tenanted facet)
                        return null;
                    }
                }
            };
        }

        public static Function<ObjectAdapter, ObjectAdapterMemento> toMemento() {
            return new Function<ObjectAdapter, ObjectAdapterMemento>() {

                @Override
                public ObjectAdapterMemento apply(ObjectAdapter from) {
                    return ObjectAdapterMemento.createOrNull(from);
                }
                
            };
        }

    }


}
