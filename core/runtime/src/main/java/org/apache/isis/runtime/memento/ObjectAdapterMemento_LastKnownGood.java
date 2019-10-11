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
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.adapter.oid.ObjectNotFoundException;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.Oid.Factory;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.persistence.PersistenceSession;

import lombok.val;

/**
 * 
 *  TODO[2112] LAST KNOWN GOOD, remove once replaced
 *
 */
public class ObjectAdapterMemento_LastKnownGood implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Factory method
     */
    public static ObjectAdapterMemento_LastKnownGood createOrNull(final ObjectAdapter adapter) {
        if (adapter == null) {
            return null;
        }
        final Object object = adapter.getPojo();
        if(object == null) {
            return null;
        }
        return new ObjectAdapterMemento_LastKnownGood(adapter);
    }

    /**
     * Factory method
     */
    public static ObjectAdapterMemento_LastKnownGood createPersistent(final RootOid rootOid) {
        return new ObjectAdapterMemento_LastKnownGood(rootOid);
    }

    public static ObjectAdapterMemento_LastKnownGood createForList(
            final ArrayList<ObjectAdapterMemento_LastKnownGood> list,
            final ObjectSpecId objectSpecId) {
        return new ObjectAdapterMemento_LastKnownGood(list, objectSpecId);
    }

    public static ObjectAdapterMemento_LastKnownGood createForList(
            final Collection<ObjectAdapterMemento_LastKnownGood> list,
            final ObjectSpecId objectSpecId) {
        return list != null ? createForList(_Lists.newArrayList(list), objectSpecId) :  null;
    }

    public static ObjectAdapterMemento_LastKnownGood createForIterable(
            final Iterable<?> iterable,
            final ObjectSpecId specId,
            final PersistenceSession persistenceSession) {
        final List<ObjectAdapterMemento_LastKnownGood> listOfMementos =
                _NullSafe.stream(iterable)
                .map(Functions.fromPojo(persistenceSession))
                .collect(Collectors.toList());
        return createForList(listOfMementos, specId);
    }

    public static ObjectAdapterMemento_LastKnownGood createForEncodeable(
            final ObjectSpecId specId,
            final String encodableValue) {
        return new ObjectAdapterMemento_LastKnownGood(specId, encodableValue);
    }

    public enum Cardinality {
        /**
         * represents a single object
         */
        SCALAR {

            @Override
            public ObjectAdapter asAdapter(
                    final ObjectAdapterMemento_LastKnownGood oam,
                    final PersistenceSession persistenceSession,
                    final SpecificationLoader specificationLoader) {
                return oam.recreateStrategy.getAdapter(oam, persistenceSession, specificationLoader);
            }

            @Override
            public int hashCode(final ObjectAdapterMemento_LastKnownGood oam) {
                return oam.recreateStrategy.hashCode(oam);
            }

            @Override
            public boolean equals(final ObjectAdapterMemento_LastKnownGood oam, final Object other) {
                if (!(other instanceof ObjectAdapterMemento_LastKnownGood)) {
                    return false;
                }
                final ObjectAdapterMemento_LastKnownGood otherOam = (ObjectAdapterMemento_LastKnownGood) other;
                if(otherOam.cardinality != SCALAR) {
                    return false;
                }
                return oam.recreateStrategy.equals(oam, (ObjectAdapterMemento_LastKnownGood) other);
            }

            @Override
            public String asString(final ObjectAdapterMemento_LastKnownGood oam) {
                return oam.recreateStrategy.toString(oam);
            }
        },
        /**
         * represents a list of objects
         */
        VECTOR {

            @Override
            public ObjectAdapter asAdapter(
                    final ObjectAdapterMemento_LastKnownGood oam,
                    final PersistenceSession persistenceSession,
                    final SpecificationLoader specificationLoader) {
                final List<Object> listOfPojos =
                        _Lists.map(oam.list, Functions.toPojo(persistenceSession, specificationLoader));

                return persistenceSession.adapterFor(listOfPojos);
            }

            @Override
            public int hashCode(final ObjectAdapterMemento_LastKnownGood oam) {
                return oam.list.hashCode();
            }

            @Override
            public boolean equals(final ObjectAdapterMemento_LastKnownGood oam, final Object other) {
                if (!(other instanceof ObjectAdapterMemento_LastKnownGood)) {
                    return false;
                }
                final ObjectAdapterMemento_LastKnownGood otherOam = (ObjectAdapterMemento_LastKnownGood) other;
                if(otherOam.cardinality != VECTOR) {
                    return false;
                }
                return oam.list.equals(otherOam.list);
            }

            @Override
            public String asString(final ObjectAdapterMemento_LastKnownGood oam) {
                return oam.list.toString();
            }
        };

        void ensure(final Cardinality sort) {
            if(this == sort) {
                return;
            }
            throw new IllegalStateException("Memento is not for " + sort);
        }

        public abstract ObjectAdapter asAdapter(
                final ObjectAdapterMemento_LastKnownGood oam,
                final PersistenceSession persistenceSession,
                final SpecificationLoader specificationLoader);

        public abstract int hashCode(final ObjectAdapterMemento_LastKnownGood oam);

        public abstract boolean equals(final ObjectAdapterMemento_LastKnownGood oam, final Object other);

        public abstract String asString(final ObjectAdapterMemento_LastKnownGood oam);
    }

    enum RecreateStrategy {
        /**
         * The {@link ObjectAdapter} that this is the memento for directly has
         * an {@link EncodableFacet} (it is almost certainly a value), and so is
         * stored directly.
         */
        ENCODEABLE {
            @Override
            ObjectAdapter recreateAdapter(
                    final ObjectAdapterMemento_LastKnownGood oam,
                    final PersistenceSession persistenceSession,
                    final SpecificationLoader specificationLoader) {
                ObjectSpecId objectSpecId = oam.objectSpecId;
                ObjectSpecification objectSpec = specificationLoader.lookupBySpecIdElseLoad(objectSpecId);
                final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
                return encodableFacet.fromEncodedString(oam.encodableValue);
            }

            @Override
            public boolean equals(ObjectAdapterMemento_LastKnownGood oam, ObjectAdapterMemento_LastKnownGood other) {
                return other.recreateStrategy == ENCODEABLE && oam.encodableValue.equals(other.encodableValue);
            }

            @Override
            public int hashCode(ObjectAdapterMemento_LastKnownGood oam) {
                return oam.encodableValue.hashCode();
            }

            @Override
            public String toString(final ObjectAdapterMemento_LastKnownGood oam) {
                return oam.encodableValue;
            }

            @Override
            public void resetVersion(
                    ObjectAdapterMemento_LastKnownGood ObjectAdapterMemento_LastKnownGood,
                    final PersistenceSession persistenceSession, final SpecificationLoader specificationLoader) {
            }
        },
        /**
         * The {@link ObjectAdapter} that this is for is already known by its
         * (persistent) {@link Oid}.
         */
        LOOKUP {
            @Override
            ObjectAdapter recreateAdapter(
                    final ObjectAdapterMemento_LastKnownGood oam,
                    final PersistenceSession persistenceSession, 
                    final SpecificationLoader specificationLoader) {
                RootOid oid = Oid.unmarshaller().unmarshal(oam.persistentOidStr, RootOid.class);
                try {
                    final ObjectAdapter adapter = persistenceSession.adapterFor(oid);
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
                    final ObjectAdapterMemento_LastKnownGood oam,
                    final PersistenceSession persistenceSession,
                    final SpecificationLoader specificationLoader) {
                // REVIEW: this may be redundant because recreateAdapter also guarantees the version will be reset.
                final ObjectAdapter adapter = recreateAdapter(
                        oam, persistenceSession, specificationLoader);
                Oid oid = adapter.getOid();
                oam.persistentOidStr = oid.enString();
            }

            @Override
            public boolean equals(ObjectAdapterMemento_LastKnownGood oam, ObjectAdapterMemento_LastKnownGood other) {
                return other.recreateStrategy == LOOKUP && oam.persistentOidStr.equals(other.persistentOidStr);
            }

            @Override
            public int hashCode(ObjectAdapterMemento_LastKnownGood oam) {
                return oam.persistentOidStr.hashCode();
            }

            @Override
            public String toString(final ObjectAdapterMemento_LastKnownGood oam) {
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
            ObjectAdapter recreateAdapter(
                    final ObjectAdapterMemento_LastKnownGood oam,
                    final PersistenceSession persistenceSession, 
                    final SpecificationLoader specificationLoader) {
                return oam.transientMemento.recreateObject();
            }

            @Override
            public boolean equals(ObjectAdapterMemento_LastKnownGood oam, ObjectAdapterMemento_LastKnownGood other) {
                return other.recreateStrategy == TRANSIENT && oam.transientMemento.equals(other.transientMemento);
            }

            @Override
            public int hashCode(ObjectAdapterMemento_LastKnownGood oam) {
                return oam.transientMemento.hashCode();
            }

            @Override
            public String toString(final ObjectAdapterMemento_LastKnownGood oam) {
                return oam.transientMemento.toString();
            }

            @Override
            public void resetVersion(
                    final ObjectAdapterMemento_LastKnownGood ObjectAdapterMemento_LastKnownGood,
                    final PersistenceSession persistenceSession, final SpecificationLoader specificationLoader) {
            }
        };

        public ObjectAdapter getAdapter(
                final ObjectAdapterMemento_LastKnownGood nom,
                final PersistenceSession persistenceSession,
                final SpecificationLoader specificationLoader) {
            return recreateAdapter(nom, persistenceSession, specificationLoader);
        }

        abstract ObjectAdapter recreateAdapter(
                final ObjectAdapterMemento_LastKnownGood nom,
                final PersistenceSession persistenceSession, 
                final SpecificationLoader specificationLoader);

        public abstract boolean equals(ObjectAdapterMemento_LastKnownGood oam, ObjectAdapterMemento_LastKnownGood other);
        public abstract int hashCode(ObjectAdapterMemento_LastKnownGood ObjectAdapterMemento_LastKnownGood);

        public abstract String toString(ObjectAdapterMemento_LastKnownGood adapterMemento);

        public abstract void resetVersion(
                ObjectAdapterMemento_LastKnownGood ObjectAdapterMemento_LastKnownGood,
                final PersistenceSession persistenceSession, final SpecificationLoader specificationLoader);
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
    private ArrayList<ObjectAdapterMemento_LastKnownGood> list;

    public ObjectAdapterMemento_LastKnownGood(final ArrayList<ObjectAdapterMemento_LastKnownGood> list, final ObjectSpecId objectSpecId) {
        this.cardinality = Cardinality.VECTOR;
        this.list = list;
        this.objectSpecId = objectSpecId;
    }

    private ObjectAdapterMemento_LastKnownGood(final RootOid rootOid) {

        // -- // TODO[2112] do we ever need to create ENCODEABLE here?
        val specId = rootOid.getObjectSpecId(); 
        val specificationLoader = IsisContext.getSpecificationLoader();
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
        this.bookmark = rootOid.asBookmark();
        this.objectSpecId = rootOid.getObjectSpecId();
        this.recreateStrategy = RecreateStrategy.LOOKUP;
    }

    private ObjectAdapterMemento_LastKnownGood(final ObjectAdapter adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("adapter cannot be null");
        }
        this.cardinality = Cardinality.SCALAR;
        final ObjectSpecification specification = adapter.getSpecification();
        objectSpecId = specification.getSpecId();
        init(adapter);
    }

    private ObjectAdapterMemento_LastKnownGood(final ObjectSpecId specId, final String encodableValue) {
        this.cardinality = Cardinality.SCALAR;
        this.objectSpecId = specId;
        this.encodableValue = encodableValue;
        this.recreateStrategy = RecreateStrategy.ENCODEABLE;
    }


    private void init(final ObjectAdapter adapter) {

        final ObjectSpecification specification = adapter.getSpecification();

        final EncodableFacet encodableFacet = specification.getFacet(EncodableFacet.class);
        final boolean isEncodable = encodableFacet != null;
        if (isEncodable) {
            encodableValue = encodableFacet.toEncodedString(adapter);
            recreateStrategy = RecreateStrategy.ENCODEABLE;
            return;
        }

        final RootOid oid = (RootOid) adapter.getOid();
        if (oid.isTransient()) {
            transientMemento = new Memento(adapter);
            recreateStrategy = RecreateStrategy.TRANSIENT;
            return;
        }

        persistentOidStr = oid.enString();
        bookmark = oid.asBookmark();
        if(adapter.getPojo() instanceof HintStore.HintIdProvider) {
            HintStore.HintIdProvider provider = (HintStore.HintIdProvider) adapter.getPojo();
            this.hintId = provider.hintId();
        }
        recreateStrategy = RecreateStrategy.LOOKUP;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public ArrayList<ObjectAdapterMemento_LastKnownGood> getList() {
        ensureVector();
        return list;
    }


    public void resetVersion(
            final PersistenceSession persistenceSession,
            final SpecificationLoader specificationLoader) {
        ensureScalar();
        recreateStrategy.resetVersion(this, persistenceSession, specificationLoader);
    }


    public Bookmark asBookmark() {
        ensureScalar();
        return bookmark;
    }

    public Bookmark asHintingBookmark() {
        Bookmark bookmark = asBookmark();
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
    public ObjectAdapter getObjectAdapter(
            final PersistenceSession persistenceSession,
            final SpecificationLoader specificationLoader) {
        
        val spec = specificationLoader.loadSpecification(objectSpecId);
        
        // intercept when trivial
        if(spec.getBeanSort().isManagedBean()) {
            return MetaModelContext.current().lookupServiceAdapterById(objectSpecId.asString());
        }
        
        return cardinality.asAdapter(this, persistenceSession, specificationLoader);
    }

    /**
     * Updates the memento if the adapter's state has changed.
     *
     * @param adapter
     */
    public void setAdapter(final ObjectAdapter adapter) {
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
            List<ObjectAdapterMemento_LastKnownGood> list,
            final PersistenceSession persistenceSession,
            final SpecificationLoader specificationLoader) {

        ensureScalar();

        // REVIEW: heavy handed, ought to be possible to just compare the OIDs
        // ignoring the concurrency checking
        final ObjectAdapter currAdapter = getObjectAdapter(persistenceSession,
                specificationLoader);
        for (ObjectAdapterMemento_LastKnownGood each : list) {
            if(each == null) {
                continue;
            }
            final ObjectAdapter otherAdapter = each.getObjectAdapter(persistenceSession, specificationLoader);
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


    //////////////////////////////////////////////////
    // Functions
    //////////////////////////////////////////////////


    public final static class Functions {

        private Functions() {
        }

        //        public static Function<ObjectAction, ActionMemento> fromAction() {
        //            return ActionMemento::new;
        //        }
        //
        //        public static Function<ObjectActionParameter, ActionParameterMemento> fromActionParameter() {
        //            return ActionParameterMemento::new;
        //        }

        public static Function<Object, ObjectAdapterMemento_LastKnownGood> fromPojo(final ObjectAdapterProvider adapterProvider) {
            return pojo->ObjectAdapterMemento_LastKnownGood.createOrNull( adapterProvider.adapterFor(pojo) );
        }

        public static Function<ObjectAdapter, ObjectAdapterMemento_LastKnownGood> fromAdapter() {
            return ObjectAdapterMemento_LastKnownGood::createOrNull;
        }

        public static Function<ObjectAdapterMemento_LastKnownGood, ObjectAdapter> fromMemento(
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

        public static Function<ObjectAdapter, ObjectAdapterMemento_LastKnownGood> toMemento() {
            return ObjectAdapterMemento_LastKnownGood::createOrNull;
        }


        public static Function<ObjectAdapterMemento_LastKnownGood, Object> toPojo(
                final PersistenceSession persistenceSession,
                final SpecificationLoader specificationLoader) {
            return input->{
                if(input == null) {
                    return null;
                }
                final ObjectAdapter objectAdapter = input
                        .getObjectAdapter(persistenceSession, specificationLoader);
                if(objectAdapter == null) {
                    return null;
                }
                return objectAdapter.getPojo();
            };
        }

        public static Function<ObjectAdapterMemento_LastKnownGood, RootOid> toOid() {
            return ObjectAdapterMemento_LastKnownGood->Factory.ofBookmark(ObjectAdapterMemento_LastKnownGood.asBookmark());
        }

    }

    private void ensureScalar() {
        getCardinality().ensure(Cardinality.SCALAR);
    }

    private void ensureVector() {
        getCardinality().ensure(Cardinality.VECTOR);
    }



}
