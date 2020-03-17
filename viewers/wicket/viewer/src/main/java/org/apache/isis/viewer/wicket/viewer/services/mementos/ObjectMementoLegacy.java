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

package org.apache.isis.viewer.wicket.viewer.services.mementos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.hint.HintStore;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
final class ObjectMementoLegacy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Factory method
     */
    public static ObjectMementoLegacy createOrNull(ManagedObject adapter) {
        if (adapter == null) {
            return null;
        }
        val pojo = adapter.getPojo();
        if(pojo == null) {
            return null;
        }
        return new ObjectMementoLegacy(adapter);
    }

    /**
     * Factory method
     */
    static ObjectMementoLegacy createPersistent(
            RootOid rootOid, 
            SpecificationLoader specificationLoader) {

        return new ObjectMementoLegacy(rootOid, specificationLoader);
    }

    private enum Cardinality {
        /**
         * represents a single object
         */
        SCALAR {

            @Override
            public ManagedObject asAdapter(
                    ObjectMementoLegacy memento,
                    SpecificationLoader specificationLoader) {

                return memento.recreateStrategy.recreateObject(memento, specificationLoader);
            }

            @Override
            public int hashCode(ObjectMementoLegacy memento) {
                return memento.recreateStrategy.hashCode(memento);
            }

            @Override
            public boolean equals(ObjectMementoLegacy memento, Object other) {
                if (!(other instanceof ObjectMementoLegacy)) {
                    return false;
                }
                final ObjectMementoLegacy otherMemento = (ObjectMementoLegacy) other;
                if(otherMemento.cardinality != SCALAR) {
                    return false;
                }
                return memento.recreateStrategy.equals(memento, otherMemento);
            }

            @Override
            public String asString(final ObjectMementoLegacy memento) {
                return memento.recreateStrategy.toString(memento);
            }
        },
        /**
         * represents a list of objects
         */
        VECTOR {

            @Override
            public ManagedObject asAdapter(
                    ObjectMementoLegacy memento,
                    SpecificationLoader specificationLoader) {

                final List<Object> listOfPojos =
                        _Lists.map(memento.list, Functions.toPojo(specificationLoader));

                return ManagedObject.of(specificationLoader::loadSpecification, listOfPojos);
            }

            @Override
            public int hashCode(ObjectMementoLegacy memento) {
                return memento.list.hashCode();
            }

            @Override
            public boolean equals(ObjectMementoLegacy memento, Object other) {
                if (!(other instanceof ObjectMementoLegacy)) {
                    return false;
                }
                final ObjectMementoLegacy otherMemento = (ObjectMementoLegacy) other;
                if(otherMemento.cardinality != VECTOR) {
                    return false;
                }
                return memento.list.equals(otherMemento.list);
            }

            @Override
            public String asString(ObjectMementoLegacy memento) {
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
                ObjectMementoLegacy memento,
                SpecificationLoader specificationLoader);

        public abstract int hashCode(ObjectMementoLegacy memento);

        public abstract boolean equals(ObjectMementoLegacy memento, Object other);

        public abstract String asString(ObjectMementoLegacy memento);
    }

    private enum RecreateStrategy {
        /**
         * The {@link ObjectAdapter} that this is the memento for directly has
         * an {@link EncodableFacet} (it is almost certainly a value), and so is
         * stored directly.
         */
        ENCODEABLE {
            @Override
            public ManagedObject recreateObject(
                    ObjectMementoLegacy memento,
                    SpecificationLoader specificationLoader) {

                ObjectSpecId specId = memento.objectSpecId;
                ObjectSpecification objectSpec = specificationLoader.lookupBySpecIdElseLoad(specId);
                EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
                return encodableFacet.fromEncodedString(memento.encodableValue);
            }

            @Override
            public boolean equals(
                    ObjectMementoLegacy memento, 
                    ObjectMementoLegacy otherMemento) {

                return otherMemento.recreateStrategy == ENCODEABLE && 
                        memento.encodableValue.equals(otherMemento.encodableValue);
            }

            @Override
            public int hashCode(ObjectMementoLegacy memento) {
                return memento.encodableValue.hashCode();
            }

            @Override
            public String toString(ObjectMementoLegacy memento) {
                return memento.encodableValue;
            }

            @Override
            public void resetVersion(
                    ObjectMementoLegacy memento,
                    SpecificationLoader specificationLoader) {
            }
        },
        /**
         * The {@link ObjectAdapter} that this is for is already known by its
         * (persistent) {@link Oid}.
         */
        LOOKUP {
            @Override
            public ManagedObject recreateObject(
                    ObjectMementoLegacy memento,
                    SpecificationLoader specificationLoader) {

                if(_NullSafe.isEmpty(memento.persistentOidStr)) {
                    return ManagedObject.empty(); 
                }
                
                RootOid rootOid = Oid.unmarshaller().unmarshal(memento.persistentOidStr, RootOid.class);
                try {

                    log.debug("lookup by rootOid [{}]", rootOid);
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
                    ObjectMementoLegacy memento,
                    SpecificationLoader specificationLoader) {

                //XXX REVIEW: this may be redundant because recreateAdapter also guarantees the version will be reset.
                ManagedObject adapter = recreateObject(memento, specificationLoader);

                memento.persistentOidStr = ManagedObject.stringifyElseFail(adapter);
            }

            @Override
            public boolean equals(ObjectMementoLegacy oam, ObjectMementoLegacy other) {
                return other.recreateStrategy == LOOKUP && oam.persistentOidStr.equals(other.persistentOidStr);
            }

            @Override
            public int hashCode(ObjectMementoLegacy oam) {
                return oam.persistentOidStr.hashCode();
            }

            @Override
            public String toString(final ObjectMementoLegacy oam) {
                return oam.persistentOidStr;
            }

        };

        public abstract ManagedObject recreateObject(
                ObjectMementoLegacy memento,
                SpecificationLoader specificationLoader);

        public abstract boolean equals(
                ObjectMementoLegacy memento, 
                ObjectMementoLegacy otherMemento);

        public abstract int hashCode(ObjectMementoLegacy memento);

        public abstract String toString(ObjectMementoLegacy memento);

        public abstract void resetVersion(
                ObjectMementoLegacy memento,
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
     * Only populated for {@link ManagedObject#getPojo() domain object}s that implement {@link HintStore.HintIdProvider}.
     */
    private String hintId;

    /**
     * populated only if {@link #getCardinality() sort} is {@link Cardinality#VECTOR vector}
     */
    private ArrayList<ObjectMementoLegacy> list;

    private ObjectMementoLegacy(
            ArrayList<ObjectMementoLegacy> list, 
            ObjectSpecId objectSpecId) {

        this.cardinality = Cardinality.VECTOR;
        this.list = list;
        this.objectSpecId = objectSpecId;
    }

    private ObjectMementoLegacy(RootOid rootOid, SpecificationLoader specificationLoader) {

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

        this.cardinality = Cardinality.SCALAR;

        this.persistentOidStr = rootOid.enString();

        requires(persistentOidStr, "persistentOidStr");

        this.bookmark = rootOid.asBookmark();
        this.objectSpecId = rootOid.getObjectSpecId();
        this.recreateStrategy = RecreateStrategy.LOOKUP;
    }

    private ObjectMementoLegacy(ManagedObject adapter) {

        requires(adapter, "adapter");

        this.cardinality = Cardinality.SCALAR;
        val spec = adapter.getSpecification();
        objectSpecId = spec.getSpecId();
        init(adapter);
    }

    private ObjectMementoLegacy(ObjectSpecId specId, String encodableValue) {
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

        val rootOid = ManagedObject.identifyElseFail(adapter);
        persistentOidStr = rootOid.enString();
        bookmark = rootOid.asBookmark();
        if(adapter.getPojo() instanceof HintStore.HintIdProvider) {
            HintStore.HintIdProvider provider = (HintStore.HintIdProvider) adapter.getPojo();
            this.hintId = provider.hintId();
        }
        recreateStrategy = RecreateStrategy.LOOKUP;
    }

    private Cardinality getCardinality() {
        return cardinality;
    }

    Bookmark asBookmark() {
        ensureScalar();
        return bookmark;
    }

    Bookmark asHintingBookmark() {
        val bookmark = asBookmark();
        return hintId != null && bookmark != null
                ? bookmark.withHintId(hintId)
                        : bookmark;
    }

    /**
     * Lazily looks up {@link ManagedObject} if required.
     *
     * <p>
     * For transient objects, be aware that calling this method more than once
     * will cause the underlying {@link ManagedObject} to be recreated,
     * overwriting any changes that may have been made. In general then it's
     * best to call once and then hold onto the value thereafter. Alternatively,
     * can call {@link #setAdapter(ManagedObject)} to keep this memento in sync.
     */
    ManagedObject reconstructObject(SpecificationLoader specificationLoader) {

        val spec = specificationLoader.loadSpecification(objectSpecId);
        if(spec==null) {
            // eg. ill-formed request
            return null;
        }

        // intercept when managed by IoCC
        if(spec.getBeanSort().isManagedBean()) {
            return spec.getMetaModelContext().lookupServiceAdapterById(objectSpecId.asString());
        }
        
        return cardinality.asAdapter(this, specificationLoader);
    }

    ObjectSpecId getObjectSpecId() {
        return objectSpecId;
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
    private static final class Functions {

        private static Function<ObjectMementoLegacy, Object> toPojo(SpecificationLoader specificationLoader) {

            return memento->{
                if(memento == null) {
                    return null;
                }
                val objectAdapter = memento
                        .reconstructObject(specificationLoader);
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


}
