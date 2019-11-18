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

package org.apache.isis.runtime.memento;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.hint.HintStore;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.adapter.oid.ObjectNotFoundException;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.persistence.PersistenceSession;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

public class ObjectAdapterMementoDefault implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Factory method
     */
    public static ObjectAdapterMementoDefault createOrNull(ManagedObject adapter) {
        if (adapter == null) {
            return null;
        }
        val pojo = adapter.getPojo();
        if(pojo == null) {
            return null;
        }
        return new ObjectAdapterMementoDefault(adapter);
    }

    /**
     * Factory method
     */
    public static ObjectAdapterMementoDefault createPersistent(
            RootOid rootOid, 
            SpecificationLoader specificationLoader) {
        
        return new ObjectAdapterMementoDefault(rootOid, specificationLoader);
    }

    public static ObjectAdapterMementoDefault createForList(
            ArrayList<ObjectAdapterMementoDefault> list,
            ObjectSpecId objectSpecId) {
        
        return new ObjectAdapterMementoDefault(list, objectSpecId);
    }

    public static ObjectAdapterMementoDefault createForList(
            Collection<ObjectAdapterMementoDefault> list,
            ObjectSpecId objectSpecId) {
        
        return list != null ? createForList(_Lists.newArrayList(list), objectSpecId) :  null;
    }

    public static ObjectAdapterMementoDefault createForIterable(
            Iterable<?> iterable,
            ObjectSpecId specId,
            PersistenceSession persistenceSession) {
        
        final List<ObjectAdapterMementoDefault> listOfMementos =
                _NullSafe.stream(iterable)
                .map(Functions.fromPojo(persistenceSession))
                .collect(Collectors.toList());
        return createForList(listOfMementos, specId);
    }

    public static ObjectAdapterMementoDefault createForEncodeable(
            ObjectSpecId specId,
            String encodableValue) {
        
        return new ObjectAdapterMementoDefault(specId, encodableValue);
    }

    public enum Cardinality {
        /**
         * represents a single object
         */
        SCALAR {

            @Override
            public ManagedObject asAdapter(
                    ObjectAdapterMementoDefault memento,
                    PersistenceSession persistenceSession,
                    SpecificationLoader specificationLoader) {
                
                return memento.recreateStrategy.getAdapter(memento, persistenceSession, specificationLoader);
            }

            @Override
            public int hashCode(ObjectAdapterMementoDefault memento) {
                return memento.recreateStrategy.hashCode(memento);
            }

            @Override
            public boolean equals(ObjectAdapterMementoDefault memento, Object other) {
                if (!(other instanceof ObjectAdapterMementoDefault)) {
                    return false;
                }
                final ObjectAdapterMementoDefault otherMemento = (ObjectAdapterMementoDefault) other;
                if(otherMemento.cardinality != SCALAR) {
                    return false;
                }
                return memento.recreateStrategy.equals(memento, otherMemento);
            }

            @Override
            public String asString(final ObjectAdapterMementoDefault memento) {
                return memento.recreateStrategy.toString(memento);
            }
        },
        /**
         * represents a list of objects
         */
        VECTOR {

            @Override
            public ManagedObject asAdapter(
                    ObjectAdapterMementoDefault memento,
                    PersistenceSession persistenceSession,
                    SpecificationLoader specificationLoader) {
                
                final List<Object> listOfPojos =
                        _Lists.map(memento.list, Functions.toPojo(persistenceSession, specificationLoader));

                return persistenceSession.adapterFor(listOfPojos);
            }

            @Override
            public int hashCode(ObjectAdapterMementoDefault memento) {
                return memento.list.hashCode();
            }

            @Override
            public boolean equals(ObjectAdapterMementoDefault memento, Object other) {
                if (!(other instanceof ObjectAdapterMementoDefault)) {
                    return false;
                }
                final ObjectAdapterMementoDefault otherMemento = (ObjectAdapterMementoDefault) other;
                if(otherMemento.cardinality != VECTOR) {
                    return false;
                }
                return memento.list.equals(otherMemento.list);
            }

            @Override
            public String asString(ObjectAdapterMementoDefault memento) {
                return memento.list.toString();
            }
        };

        void ensure(Cardinality sort) {
            if(this == sort) {
                return;
            }
            throw new IllegalStateException("Memento is not for " + sort);
        }

        public abstract ManagedObject asAdapter(
                ObjectAdapterMementoDefault memento,
                PersistenceSession persistenceSession,
                SpecificationLoader specificationLoader);

        public abstract int hashCode(ObjectAdapterMementoDefault memento);

        public abstract boolean equals(ObjectAdapterMementoDefault memento, Object other);

        public abstract String asString(ObjectAdapterMementoDefault memento);
    }

    enum RecreateStrategy {
        /**
         * The {@link ObjectAdapter} that this is the memento for directly has
         * an {@link EncodableFacet} (it is almost certainly a value), and so is
         * stored directly.
         */
        ENCODEABLE {
            @Override
            ManagedObject recreateAdapter(
                    ObjectAdapterMementoDefault memento,
                    PersistenceSession persistenceSession,
                    SpecificationLoader specificationLoader) {
                
                ObjectSpecId specId = memento.objectSpecId;
                ObjectSpecification objectSpec = specificationLoader.lookupBySpecIdElseLoad(specId);
                EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
                return encodableFacet.fromEncodedString(memento.encodableValue);
            }

            @Override
            public boolean equals(
                    ObjectAdapterMementoDefault memento, 
                    ObjectAdapterMementoDefault otherMemento) {
                
                return otherMemento.recreateStrategy == ENCODEABLE && 
                        memento.encodableValue.equals(otherMemento.encodableValue);
            }

            @Override
            public int hashCode(ObjectAdapterMementoDefault memento) {
                return memento.encodableValue.hashCode();
            }

            @Override
            public String toString(ObjectAdapterMementoDefault memento) {
                return memento.encodableValue;
            }

            @Override
            public void resetVersion(
                    ObjectAdapterMementoDefault memento,
                    PersistenceSession persistenceSession, 
                    SpecificationLoader specificationLoader) {
            }
        },
        /**
         * The {@link ObjectAdapter} that this is for is already known by its
         * (persistent) {@link Oid}.
         */
        LOOKUP {
            @Override
            ManagedObject recreateAdapter(
                    ObjectAdapterMementoDefault memento,
                    PersistenceSession persistenceSession, 
                    SpecificationLoader specificationLoader) {
                
                RootOid rootOid = Oid.unmarshaller().unmarshal(memento.persistentOidStr, RootOid.class);
                try {
                    
                    return ManagedObject._adapterOfRootOid(specificationLoader, rootOid);

                } finally {
                    // possibly out-dated insight ...
                    // a side-effect of AdapterManager#adapterFor(...) is that it will update the oid
                    // with the correct version, even when there is a concurrency exception
                    // we copy this updated oid string into our memento so that, if we retry,
                    // we will succeed second time around

                    memento.persistentOidStr = rootOid.enString();
                }
            }

            @Override
            public void resetVersion(
                    ObjectAdapterMementoDefault memento,
                    PersistenceSession persistenceSession,
                    SpecificationLoader specificationLoader) {
                
                //XXX REVIEW: this may be redundant because recreateAdapter also guarantees the version will be reset.
                final ManagedObject adapter = recreateAdapter(
                        memento, persistenceSession, specificationLoader);
                Oid oid = ManagedObject._identify(adapter);
                memento.persistentOidStr = oid.enString();
            }

            @Override
            public boolean equals(ObjectAdapterMementoDefault oam, ObjectAdapterMementoDefault other) {
                return other.recreateStrategy == LOOKUP && oam.persistentOidStr.equals(other.persistentOidStr);
            }

            @Override
            public int hashCode(ObjectAdapterMementoDefault oam) {
                return oam.persistentOidStr.hashCode();
            }

            @Override
            public String toString(final ObjectAdapterMementoDefault oam) {
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
            ManagedObject recreateAdapter(
                    ObjectAdapterMementoDefault memento,
                    PersistenceSession persistenceSession, 
                    SpecificationLoader specificationLoader) {
                
                return memento.transientMemento.recreateObject(specificationLoader, persistenceSession);
            }

            @Override
            public boolean equals(ObjectAdapterMementoDefault oam, ObjectAdapterMementoDefault other) {
                return other.recreateStrategy == TRANSIENT && oam.transientMemento.equals(other.transientMemento);
            }

            @Override
            public int hashCode(ObjectAdapterMementoDefault oam) {
                return oam.transientMemento.hashCode();
            }

            @Override
            public String toString(final ObjectAdapterMementoDefault oam) {
                return oam.transientMemento.toString();
            }

            @Override
            public void resetVersion(
                    ObjectAdapterMementoDefault memento,
                    PersistenceSession persistenceSession,
                    SpecificationLoader specificationLoader) {
            }
        };

        public ManagedObject getAdapter(
                ObjectAdapterMementoDefault memento,
                PersistenceSession persistenceSession,
                SpecificationLoader specificationLoader) {
            
            return recreateAdapter(memento, persistenceSession, specificationLoader);
        }

        abstract ManagedObject recreateAdapter(
                ObjectAdapterMementoDefault memento,
                PersistenceSession persistenceSession, 
                SpecificationLoader specificationLoader);

        public abstract boolean equals(
                ObjectAdapterMementoDefault memento, 
                ObjectAdapterMementoDefault otherMemento);
        
        public abstract int hashCode(ObjectAdapterMementoDefault memento);

        public abstract String toString(ObjectAdapterMementoDefault memento);

        public abstract void resetVersion(
                ObjectAdapterMementoDefault memento,
                PersistenceSession persistenceSession, 
                SpecificationLoader specificationLoader);
    }



    private final Cardinality cardinality;
    private final ObjectSpecId objectSpecId;

    /**
     * Populated only if {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}
     */
    private RecreateStrategy recreateStrategy;

    /**
     * Populated only if {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}
     */
    @SuppressWarnings("unused")
    private String titleHint;

    /**
     * The current value, if {@link RecreateStrategy#ENCODEABLE}; will be <tt>null</tt> otherwise.
     *
     * <p>
     * Also, populated only if {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}
     */
    private String encodableValue;

    /**
     * The current value, if {@link RecreateStrategy#LOOKUP}, will be <tt>null</tt> otherwise.
     *
     * <p>
     * Also, populated only if {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}
     */
    private String persistentOidStr;

    /**
     * The current value, if {@link RecreateStrategy#LOOKUP}, will be <tt>null</tt> otherwise.
     *
     * <p>
     * Also, populated only if {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}
     */
    private Bookmark bookmark;

    /**
     * Only populated for {@link ObjectAdapter#getPojo() domain object}s that implement {@link HintStore.HintIdProvider}.
     */
    private String hintId;

    /**
     * The current value, if {@link RecreateStrategy#TRANSIENT}, will be <tt>null</tt> otherwise.
     *
     * <p>
     * Also, populated only if {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}
     */
    private Memento transientMemento;

    /**
     * populated only if {@link #getCardinality() sort} is {@link Cardinality#VECTOR vector}
     */
    private ArrayList<ObjectAdapterMementoDefault> list;

    public ObjectAdapterMementoDefault(
            ArrayList<ObjectAdapterMementoDefault> list, 
            ObjectSpecId objectSpecId) {
        
        this.cardinality = Cardinality.VECTOR;
        this.list = list;
        this.objectSpecId = objectSpecId;
    }

    private ObjectAdapterMementoDefault(RootOid rootOid, SpecificationLoader specificationLoader) {

        // -- // TODO[2112] do we ever need to create ENCODEABLE here?
        val specId = rootOid.getObjectSpecId(); 
        val spec = specificationLoader.lookupBySpecIdElseLoad(specId);
        if(spec!=null && spec.isEncodeable()) {
            this.cardinality = Cardinality.SCALAR;
            this.objectSpecId = specId;
            this.encodableValue = rootOid.getIdentifier();
            this.recreateStrategy = RecreateStrategy.ENCODEABLE;
            return;
        } 
        // -- //

        _Assert.assertFalse("expected not to be transient", rootOid.isTransient());

        this.cardinality = Cardinality.SCALAR;

        this.persistentOidStr = rootOid.enString();
        
        requires(persistentOidStr, "persistentOidStr");
        
        this.bookmark = rootOid.asBookmark();
        this.objectSpecId = rootOid.getObjectSpecId();
        this.recreateStrategy = RecreateStrategy.LOOKUP;
    }

    private ObjectAdapterMementoDefault(ManagedObject adapter) {
        
        requires(adapter, "adapter");
        
        this.cardinality = Cardinality.SCALAR;
        val spec = adapter.getSpecification();
        objectSpecId = spec.getSpecId();
        init(adapter);
    }

    private ObjectAdapterMementoDefault(ObjectSpecId specId, String encodableValue) {
        this.cardinality = Cardinality.SCALAR;
        this.objectSpecId = specId;
        this.encodableValue = encodableValue;
        this.recreateStrategy = RecreateStrategy.ENCODEABLE;
    }


    private void init(ManagedObject adapter) {

        val spec = adapter.getSpecification();

        val encodableFacet = spec.getFacet(EncodableFacet.class);
        val isEncodable = encodableFacet != null;
        if (isEncodable) {
            encodableValue = encodableFacet.toEncodedString(adapter);
            recreateStrategy = RecreateStrategy.ENCODEABLE;
            return;
        }

        val rootOid = (RootOid) ManagedObject._identify(adapter);
        if (rootOid.isTransient()) {
            transientMemento = new Memento(adapter);
            recreateStrategy = RecreateStrategy.TRANSIENT;
            return;
        }

        persistentOidStr = rootOid.enString();
        bookmark = rootOid.asBookmark();
        if(adapter.getPojo() instanceof HintStore.HintIdProvider) {
            HintStore.HintIdProvider provider = (HintStore.HintIdProvider) adapter.getPojo();
            this.hintId = provider.hintId();
        }
        recreateStrategy = RecreateStrategy.LOOKUP;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public ArrayList<ObjectAdapterMementoDefault> getList() {
        ensureVector();
        return list;
    }


    public void resetVersion(
            PersistenceSession persistenceSession,
            SpecificationLoader specificationLoader) {
        
        ensureScalar();
        recreateStrategy.resetVersion(this, persistenceSession, specificationLoader);
    }


    public Bookmark asBookmark() {
        ensureScalar();
        return bookmark;
    }

    public Bookmark asHintingBookmark() {
        val bookmark = asBookmark();
        return hintId != null && bookmark != null
                ? new HintStore.BookmarkWithHintId(bookmark, hintId)
                        : bookmark;
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
    public ManagedObject getObjectAdapter(
            PersistenceSession persistenceSession,
            SpecificationLoader specificationLoader) {
        
        val spec = specificationLoader.loadSpecification(objectSpecId);
        if(spec==null) {
            // eg. ill-formed request
            return null;
        }
        
        // intercept when trivial
        if(spec.getBeanSort().isManagedBean()) {
            return spec.getMetaModelContext().lookupServiceAdapterById(objectSpecId.asString());
        }
        
        return cardinality.asAdapter(this, persistenceSession, specificationLoader);
    }

    /**
     * Updates the memento if the adapter's state has changed.
     *
     * @param adapter
     */
    public void setAdapter(ManagedObject adapter) {
        ensureScalar();
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
            List<ObjectAdapterMementoDefault> mementos,
            PersistenceSession persistenceSession,
            SpecificationLoader specificationLoader) {

        ensureScalar();

        //XXX REVIEW: heavy handed, ought to be possible to just compare the OIDs
        // ignoring the concurrency checking
        val currAdapter = getObjectAdapter(persistenceSession, specificationLoader);
        
        for (val memento : mementos) {
            if(memento == null) {
                continue;
            }
            val otherAdapter = memento.getObjectAdapter(persistenceSession, specificationLoader);
            if(currAdapter == otherAdapter) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return cardinality.hashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return cardinality.equals(this, obj);
    }


    @Override
    public String toString() {
        return asString();
    }

    public String asString() {
        return cardinality.asString(this);
    }


    // -- FUNCTIONS

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final static class Functions {

        public static Function<Object, ObjectAdapterMementoDefault> fromPojo(final ObjectAdapterProvider adapterProvider) {
            return pojo->ObjectAdapterMementoDefault.createOrNull( adapterProvider.adapterFor(pojo) );
        }

        public static Function<ObjectAdapterMementoDefault, ManagedObject> fromMemento(
                final PersistenceSession persistenceSession,
                final SpecificationLoader specificationLoader) {

            return memento->{
                try {
                    return memento.getObjectAdapter(persistenceSession, specificationLoader);
                } catch (ObjectNotFoundException e) {
                    // this can happen if for example the object is not visible (due to the security tenanted facet)
                    return null;
                }
            };
        }

        public static Function<ObjectAdapterMementoDefault, Object> toPojo(
                final PersistenceSession persistenceSession,
                final SpecificationLoader specificationLoader) {
            
            return memento->{
                if(memento == null) {
                    return null;
                }
                val objectAdapter = memento.getObjectAdapter(persistenceSession, specificationLoader);
                if(objectAdapter == null) {
                    return null;
                }
                return objectAdapter.getPojo();
            };
        }

    }

    private void ensureScalar() {
        getCardinality().ensure(Cardinality.SCALAR);
    }

    private void ensureVector() {
        getCardinality().ensure(Cardinality.VECTOR);
    }



}
