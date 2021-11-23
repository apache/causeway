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
package org.apache.isis.core.runtimeservices.memento;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.id.HasLogicalType;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.Oid;
import org.apache.isis.applib.services.hint.HintIdProvider;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
final class _ObjectMemento implements HasLogicalType, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Factory method
     */
    public static _ObjectMemento createOrNull(final ManagedObject adapter) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            return null;
        }
        return new _ObjectMemento(adapter);
    }

    /**
     * Factory method
     */
    static _ObjectMemento createPersistent(
            final Bookmark bookmark,
            final SpecificationLoader specificationLoader) {

        return new _ObjectMemento(bookmark, specificationLoader);
    }

    private enum Cardinality {
        /**
         * represents a single object
         */
        SCALAR {

            @Override
            public ManagedObject asAdapter(
                    final _ObjectMemento memento,
                    final MetaModelContext mmc) {

                return memento.recreateStrategy.recreateObject(memento, mmc);
            }

            @Override
            public int hashCode(final _ObjectMemento memento) {
                return memento.recreateStrategy.hashCode(memento);
            }

            @Override
            public boolean equals(final _ObjectMemento memento, final Object other) {
                if (!(other instanceof _ObjectMemento)) {
                    return false;
                }
                final _ObjectMemento otherMemento = (_ObjectMemento) other;
                if(otherMemento.cardinality != SCALAR) {
                    return false;
                }
                return memento.recreateStrategy.equals(memento, otherMemento);
            }

            @Override
            public String asString(final _ObjectMemento memento) {
                return memento.recreateStrategy.toString(memento);
            }
        },
        /**
         * represents a list of objects
         */
        VECTOR {

            @Override
            public ManagedObject asAdapter(
                    final _ObjectMemento memento,
                    final MetaModelContext mmc) {

                final List<Object> listOfPojos =
                        _Lists.map(memento.list, Functions.toPojo(mmc));
                return ManagedObject.lazy(mmc.getSpecificationLoader(), listOfPojos);
            }

            @Override
            public int hashCode(final _ObjectMemento memento) {
                return memento.list.hashCode();
            }

            @Override
            public boolean equals(final _ObjectMemento memento, final Object other) {
                if (!(other instanceof _ObjectMemento)) {
                    return false;
                }
                final _ObjectMemento otherMemento = (_ObjectMemento) other;
                if(otherMemento.cardinality != VECTOR) {
                    return false;
                }
                return memento.list.equals(otherMemento.list);
            }

            @Override
            public String asString(final _ObjectMemento memento) {
                return memento.list.toString();
            }
        };

        void ensure(final Cardinality sort) {
            if(this == sort) {
                return;
            }
            throw new IllegalStateException("Memento is not for " + sort);
        }

        public abstract ManagedObject asAdapter(
                _ObjectMemento memento,
                MetaModelContext mmc);

        public abstract int hashCode(_ObjectMemento memento);

        public abstract boolean equals(_ObjectMemento memento, Object other);

        public abstract String asString(_ObjectMemento memento);
    }

    private enum RecreateStrategy {
        /**
         * The {@link ManagedObject} that this is the memento for, directly has
         * an {@link EncodableFacet} (it is almost certainly a value), and so is
         * stored directly.
         */
        ENCODEABLE {
            @Override
            public ManagedObject recreateObject(
                    final _ObjectMemento memento,
                    final MetaModelContext mmc) {

                EncodableFacet encodableFacet = mmc.getSpecificationLoader()
                        .specForLogicalType(memento.logicalType)
                        .map(spec->spec.getFacet(EncodableFacet.class))
                        .orElseThrow(()->_Exceptions.unrecoverableFormatted(
                                "logical type %s is expected to have a EncodableFacet", memento.logicalType));

                return encodableFacet.fromEncodedString(memento.encodableValue);
            }

            @Override
            public boolean equals(
                    final _ObjectMemento memento,
                    final _ObjectMemento otherMemento) {

                return otherMemento.recreateStrategy == ENCODEABLE &&
                        memento.encodableValue.equals(otherMemento.encodableValue);
            }

            @Override
            public int hashCode(final _ObjectMemento memento) {
                return memento.encodableValue.hashCode();
            }

            @Override
            public String toString(final _ObjectMemento memento) {
                return memento.encodableValue;
            }

            @Override
            public void resetVersion(
                    final _ObjectMemento memento,
                    final MetaModelContext mmc) {
            }
        },
        /**
         * The {@link ManagedObject} that this is for, is already known by its
         * (persistent) {@link Oid}.
         */
        LOOKUP {
            @Override
            public @Nullable ManagedObject recreateObject(
                    final _ObjectMemento memento,
                    final MetaModelContext mmc) {

                if(_NullSafe.isEmpty(memento.persistentOidStr)) {
                    throw _Exceptions.illegalArgument(
                            "need an id to lookup an object, got logical-type %s", memento.logicalType);
                }

                final Bookmark bookmark = Bookmark.parseElseFail(memento.persistentOidStr);

                try {

                    log.debug("lookup by oid [{}]", bookmark);
                    return mmc.loadObject(bookmark).orElse(null);

                } finally {
                    // possibly out-dated insight ...
                    // a side-effect of AdapterManager#adapterFor(...) is that it will update the oid
                    // with the correct version, even when there is a concurrency exception
                    // we copy this updated oid string into our memento so that, if we retry,
                    // we will succeed second time around

                    memento.persistentOidStr = bookmark.stringify();
                }
            }

            @Override
            public void resetVersion(
                    final _ObjectMemento memento,
                    final MetaModelContext mmc) {

                //XXX REVIEW: this may be redundant because recreateAdapter also guarantees the version will be reset.
                ManagedObject adapter = recreateObject(memento, mmc);

                memento.persistentOidStr = ManagedObjects.stringifyElseFail(adapter);
            }

            @Override
            public boolean equals(final _ObjectMemento oam, final _ObjectMemento other) {
                return other.recreateStrategy == LOOKUP
                        && oam.persistentOidStr.equals(other.persistentOidStr);
            }

            @Override
            public int hashCode(final _ObjectMemento oam) {
                return oam.persistentOidStr.hashCode();
            }

            @Override
            public String toString(final _ObjectMemento oam) {
                return oam.persistentOidStr;
            }

        },
        /**
         * If all other strategies fail, as last resort we use plain java serialization, provided
         * that the type in question is serializable
         */
        SERIALIZABLE {
            @Override
            public ManagedObject recreateObject(
                    final _ObjectMemento memento,
                    final MetaModelContext mmc) {
                ObjectSpecification spec = mmc.getSpecificationLoader()
                        .specForLogicalTypeElseFail(memento.logicalType);
                return mmc.getObjectManager().getObjectSerializer()
                        .deserialize(spec, memento.serializedObject);
            }

            @Override
            public boolean equals(
                    final _ObjectMemento memento,
                    final _ObjectMemento otherMemento) {
                return otherMemento.recreateStrategy == SERIALIZABLE
                        && Objects.equals(memento.logicalType, otherMemento.logicalType)
                        && Objects.equals(memento.serializedObject, otherMemento.serializedObject);
            }

            @Override
            public int hashCode(final _ObjectMemento memento) {
                return Arrays.hashCode(memento.serializedObject); // potentially expensive, unfortunately cannot be cached in enum
            }

            @Override
            public String toString(final _ObjectMemento memento) {
                return "ObjectMementoWkt {SERIALIZABLE " + memento.getLogicalTypeName() + "}";
            }

            @Override
            public void resetVersion(
                    final _ObjectMemento memento,
                    final MetaModelContext mmc) {
                // nope
            }
        };

        public abstract @Nullable ManagedObject recreateObject(
                _ObjectMemento memento,
                MetaModelContext mmc);

        public abstract boolean equals(
                _ObjectMemento memento,
                _ObjectMemento otherMemento);

        public abstract int hashCode(_ObjectMemento memento);

        public abstract String toString(_ObjectMemento memento);

        public abstract void resetVersion(
                _ObjectMemento memento,
                MetaModelContext mmc);
    }



    private final Cardinality cardinality;
    @Getter(onMethod_ = {@Override}) private final LogicalType logicalType;

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
     * The current value, if {@link RecreateStrategy#SERIALIZABLE}; will be <tt>null</tt> otherwise.
     *
     * <p>
     * Also, populated only if {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}
     */
    private byte[] serializedObject;

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
     * Only populated for {@link ManagedObject#getPojo() domain object}s that implement {@link HintIdProvider}.
     */
    private String hintId;

    /**
     * populated only if {@link #getCardinality() sort} is {@link Cardinality#VECTOR vector}
     */
    private ArrayList<_ObjectMemento> list;

    private _ObjectMemento(
            final ArrayList<_ObjectMemento> list,
            final LogicalType logicalType) {

        this.cardinality = Cardinality.VECTOR;
        this.list = list;
        this.logicalType = logicalType;
    }

    private _ObjectMemento(final Bookmark bookmark, final SpecificationLoader specificationLoader) {

        // -- // TODO[2112] do we ever need to create ENCODEABLE here?
        val logicalTypeName = bookmark.getLogicalTypeName();
        val spec = specificationLoader.specForLogicalTypeName(logicalTypeName)
                .orElseThrow(()->_Exceptions.unrecoverableFormatted(
                        "cannot recreate spec from logicalTypeName %s", logicalTypeName));

        this.cardinality = Cardinality.SCALAR;
        this.logicalType = spec.getLogicalType();

        if(spec.isEncodeable()) {
            this.encodableValue = bookmark.getIdentifier();
            this.recreateStrategy = RecreateStrategy.ENCODEABLE;
            return;
        }

        this.persistentOidStr = bookmark.stringify();
        Objects.requireNonNull(persistentOidStr, "persistentOidStr");

        this.bookmark = bookmark;
        this.recreateStrategy = RecreateStrategy.LOOKUP;
    }

    private _ObjectMemento(final @NonNull ManagedObject adapter) {
        this.cardinality = Cardinality.SCALAR;
        val spec = adapter.getSpecification();
        this.logicalType = spec.getLogicalType();
        init(adapter);
    }

    private _ObjectMemento(final LogicalType logicalType, final String encodableValue) {
        this.cardinality = Cardinality.SCALAR;
        this.logicalType = logicalType;
        this.encodableValue = encodableValue;
        this.recreateStrategy = RecreateStrategy.ENCODEABLE;
    }


    private void init(final ManagedObject adapter) {

        val spec = adapter.getSpecification();

        if(spec.isIdentifiable() || spec.isParented() ) {
            bookmark = ManagedObjects.bookmarkElseFail(adapter);
            persistentOidStr = bookmark.stringify();
            if(adapter.getPojo() instanceof HintIdProvider) {
                this.hintId = ((HintIdProvider) adapter.getPojo()).hintId();
            }
            recreateStrategy = RecreateStrategy.LOOKUP;
            return;
        }

        val encodableFacet = spec.getFacet(EncodableFacet.class);
        val isEncodable = encodableFacet != null;
        if (isEncodable) {
            encodableValue = encodableFacet.toEncodedString(adapter);
            recreateStrategy = RecreateStrategy.ENCODEABLE;
            return;
        }

        if(spec.isSerializable()) {
            val serializer = spec.getMetaModelContext().getObjectManager().getObjectSerializer();
            serializedObject = serializer.serialize(adapter);
            recreateStrategy = RecreateStrategy.SERIALIZABLE;
            return;
        }

        throw _Exceptions.illegalArgument("Don't know how to create an ObjectMemento for a type "
                + "with ObjectSpecification %s. "
                + "All other strategies failed. Type is neither "
                + "identifiable (isManagedBean() || isViewModel() || isEntity()), "
                + "nor is a 'parented' Collection, "
                + "nor has 'encodable' semantics, nor is (Serializable || Externalizable)", spec);

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
    ManagedObject reconstructObject(final MetaModelContext mmc) {

        val specificationLoader = mmc.getSpecificationLoader();
        val spec = specificationLoader.specForLogicalType(logicalType).orElse(null);
        if(spec==null) {
            // eg. ill-formed request
            return null;
        }

        // intercept when managed by IoCC
        if(spec.getBeanSort().isManagedBean()) {
            return spec.getMetaModelContext().lookupServiceAdapterById(getLogicalTypeName());
        }

        return cardinality.asAdapter(this, mmc);
    }

    @Override
    public int hashCode() {
        return cardinality.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
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

        private static Function<_ObjectMemento, Object> toPojo(
                final MetaModelContext mmc) {

            return memento->{
                if(memento == null) {
                    return null;
                }
                val objectAdapter = memento
                        .reconstructObject(mmc);
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
