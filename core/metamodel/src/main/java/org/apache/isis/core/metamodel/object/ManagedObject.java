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
package org.apache.isis.core.metamodel.object;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.isis.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Represents an instance of some element of the meta-model managed by the framework,
 * that is <i>Spring</i> managed beans, persistence-stack provided entities, view-models
 * or instances of value types.
 *
 * @since 2.0 {@index}}
 *
 */
public interface ManagedObject extends HasMetaModelContext {

    /**
     * ManagedObject specializations have varying contract/behavior.
     */
    @Getter
    @RequiredArgsConstructor
    @Log4j2
    enum Specialization {
        /**
         * <h1>Contract</h1><ul>
         * <li>Specification (null, immutable)</li>
         * <li>Bookmark (n/a)</li>
         * <li>Pojo (null, immutable)</li>
         * </ul>
         * @implNote realized by a singleton (static) {@link ManagedObject} instance;
         */
        UNSPECIFIED(TypePolicy.NO_TYPE, BookmarkPolicy.NO_BOOKMARK, PojoPolicy.NO_POJO),

        /**
         * <h1>Contract</h1><ul>
         * <li>Specification (immutable, allowed to correspond to abstract type)</li>
         * <li>Bookmark (n/a)</li>
         * <li>Pojo (null, immutable)</li>
         * </ul>
         */
        EMPTY(TypePolicy.ABSTRACT_TYPE_ALLOWED, BookmarkPolicy.NO_BOOKMARK, PojoPolicy.NO_POJO),

        /**
         * <h1>Contract</h1><ul>
         * <li>Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (immutable)</li>
         * <li>Pojo (immutable)</li>
         * </ul>
         */
        VALUE(TypePolicy.EXACT_TYPE_REQUIRED, BookmarkPolicy.IMMUTABLE, PojoPolicy.IMMUTABLE),

        /**
         * <h1>Contract</h1><ul>
         * <li>Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (immutable)</li>
         * <li>Pojo (immutable)</li>
         * </ul>
         */
        SERVICE(TypePolicy.EXACT_TYPE_REQUIRED, BookmarkPolicy.IMMUTABLE, PojoPolicy.IMMUTABLE),

        /**
         * <h1>Contract</h1><ul>
         * <li>Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (refreshable, as VM state changes manifest in change of ID)</li>
         * <li>Pojo (mutable, but immutable obj. ref.)</li>
         * </ul>
         */
        VIEWMODEL(TypePolicy.EXACT_TYPE_REQUIRED, BookmarkPolicy.REFRESHABLE, PojoPolicy.STATEFUL),

        /**
         * <h1>Contract</h1><ul>
         * <li>Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (immutable,  entity must be persistent, it must have an ID,  fail otherwise)</li>
         * <li>Pojo (refetchable)</li>
         * </ul>
         */
        ENTITY(TypePolicy.EXACT_TYPE_REQUIRED, BookmarkPolicy.IMMUTABLE, PojoPolicy.REFETCHABLE),

        /**
         * <h1>Contract</h1><ul>
         * <li>Element Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (n/a)</li>
         * <li>Pojo (allowed stateful, immutable obj. ref)</li>
         * </ul>
         */
        MIXIN(TypePolicy.EXACT_TYPE_REQUIRED, BookmarkPolicy.NO_BOOKMARK, PojoPolicy.STATEFUL),

        /**
         * <h1>Contract</h1><ul>
         * <li>Element Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (n/a)</li>
         * <li>Pojo (allowed stateful, immutable obj. ref)</li>
         * </ul>
         */
        OTHER(TypePolicy.EXACT_TYPE_REQUIRED, BookmarkPolicy.NO_BOOKMARK, PojoPolicy.STATEFUL),

        /**
         * <h1>Contract</h1><ul>
         * <li>Element Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (n/a)</li>
         * <li>Pojo (unmod. Collection of pojos)</li>
         * </ul>
         */
        PACKED(TypePolicy.ABSTRACT_TYPE_ALLOWED, BookmarkPolicy.NO_BOOKMARK, PojoPolicy.PACKED);

        static enum TypePolicy {
            /** has no type information */
            NO_TYPE,
            /** has type information, abstract types are allowed */
            ABSTRACT_TYPE_ALLOWED,
            /** has type information, exact types are required */
            EXACT_TYPE_REQUIRED;
            ////
            /** has no type information */
            public boolean isNoType() { return this == NO_TYPE; }
            /** has type information, abstract types are allowed */
            public boolean isAbstractTypeAllowed() { return this == ABSTRACT_TYPE_ALLOWED; }
            /** has type information, exact types are required */
            public boolean isExactTypeRequired() { return this == EXACT_TYPE_REQUIRED; }
            /** has type information */
            public boolean isTypeRequiredAny() { return !isNoType(); }
        }
        static enum BookmarkPolicy {
            /** has no {@link Bookmark} */
            NO_BOOKMARK,
            /** has an immutable {@link Bookmark} */
            IMMUTABLE,
            /** has an refreshable {@link Bookmark}, that is a mutable object reference */
            REFRESHABLE;
            ////
            /** has no {@link Bookmark} */
            public boolean isNoBookmark() { return this == NO_BOOKMARK; }
            /** has an immutable {@link Bookmark} */
            public boolean isImmutable() { return this == IMMUTABLE; }
            /** has an refreshable {@link Bookmark}, that is a mutable object reference */
            public boolean isRefreshable() { return this == REFRESHABLE; }
        }
        static enum PojoPolicy {
            /** has no pojo, immutable <code>null</code> */
            NO_POJO,
            /** has a non-null pojo, immutable, with immutable object reference */
            IMMUTABLE,
            /** has a stateful pojo, with immutable object reference */
            STATEFUL,
            /** has a stateful pojo, with mutable object reference */
            REFETCHABLE,
            /** creates an unmodifiable collection of pojos (lazily);
             * supports unpacking into a {@link Can} of {@link ManagedObject}s;*/
            PACKED;
            ////
            /** has no pojo, immutable <code>null</code> */
            public boolean isNoPojo() { return this == NO_POJO; }
            /** has a non-null pojo, immutable, with immutable object reference */
            public boolean isImmutable() { return this == IMMUTABLE; }
            /** has a stateful pojo, with immutable object reference */
            public boolean isStateful() { return this == STATEFUL; }
            /** has a stateful pojo, with mutable object reference */
            public boolean isRefetchable() { return this == REFETCHABLE; }
            /** creates an unmodifiable collection of pojos (lazily);
             * supports unpacking into a {@link Can} of {@link ManagedObject}s;*/
            public boolean isPacked() { return this == PACKED; }
        }

        private final TypePolicy typePolicy;
        private final BookmarkPolicy bookmarkPolicy;
        private final PojoPolicy pojoPolicy;

        /**
         * UNSPECIFIED
         * @see TypePolicy#NO_TYPE
         * @see BookmarkPolicy#NO_BOOKMARK
         * @see PojoPolicy#NO_POJO
         */
        public boolean isUnspecified() { return this == UNSPECIFIED; }
        public boolean isSpecified() { return this != UNSPECIFIED; }
        /**
         * EMPTY
         * @see TypePolicy#ABSTRACT_TYPE_ALLOWED
         * @see BookmarkPolicy#NO_BOOKMARK
         * @see PojoPolicy#NO_POJO
         */
        public boolean isEmpty() { return this == EMPTY; }
        /**
         * VALUE
         * @see TypePolicy#EXACT_TYPE_REQUIRED
         * @see BookmarkPolicy#IMMUTABLE
         * @see PojoPolicy#IMMUTABLE
         */
        public boolean isValue() { return this == VALUE; }
        /**
         * SERVICE
         * @see TypePolicy#EXACT_TYPE_REQUIRED
         * @see BookmarkPolicy#IMMUTABLE
         * @see PojoPolicy#IMMUTABLE
         */
        public boolean isService() { return this == SERVICE; }
        /**
         * VIEWMODEL
         * @see TypePolicy#EXACT_TYPE_REQUIRED
         * @see BookmarkPolicy#REFRESHABLE
         * @see PojoPolicy#STATEFUL
         */
        public boolean isViewmodel() { return this == VIEWMODEL; }
        /**
         * ENTITY
         * @see TypePolicy#EXACT_TYPE_REQUIRED
         * @see BookmarkPolicy#IMMUTABLE
         * @see PojoPolicy#REFETCHABLE
         */
        public boolean isEntity() { return this == ENTITY; }
        /**
         * MIXIN
         * @see TypePolicy#EXACT_TYPE_REQUIRED
         * @see BookmarkPolicy#NO_BOOKMARK
         * @see PojoPolicy#STATEFUL
         */
        public boolean isMixin() { return this == MIXIN; }
        /**
         * OTHER
         * @see TypePolicy#EXACT_TYPE_REQUIRED
         * @see BookmarkPolicy#NO_BOOKMARK
         * @see PojoPolicy#STATEFUL
         */
        public boolean isOther() { return this == OTHER; }
        /**
         * PACKED
         * @see TypePolicy#ABSTRACT_TYPE_ALLOWED
         * @see BookmarkPolicy#NO_BOOKMARK
         * @see PojoPolicy#PACKED
         */
        public boolean isPacked() { return this == PACKED; }

        public static Specialization inferFrom(
                final @Nullable ObjectSpecification spec,
                final @Nullable Object pojo) {
            if(spec==null) {
                return UNSPECIFIED;
            }
            if(spec.isNonScalar()) {
                return PACKED;
            }
            if(pojo==null) {
                return EMPTY;
            }
            if(spec.isValue()) {
                return VALUE;
            }
            if(spec.isInjectable()) {
                return SERVICE;
            }
            if(spec.isViewModel()) {
                return VIEWMODEL;
            }
            if(spec.isEntity()) {
                return ENTITY;
            }
            if(spec.isMixin()) {
                return MIXIN;
            }
            if(!spec.isAbstract()) {
                return OTHER;
            }
            log.warn("failed specialization attempt for {}", spec);
            return UNSPECIFIED;
        }

    }

    /**
     * Returns the specific {@link Specialization} this {@link ManagedObject} implements,
     * which governs this object's behavior.
     * @implNote FIXME[ISIS-3167] not fully implemented yet
     */
    Specialization getSpecialization();

    /**
     * Returns the specification that details the structure (meta-model) of this object.
     */
    ObjectSpecification getSpecification();

    /**
     * Returns the adapted domain object, the 'plain old java' object this managed object
     * represents with the framework.
     */
    Object getPojo();

    /**
     * Returns the object's bookmark as identified by the ObjectManager.
     * Bookmarks are considered immutable, hence will be memoized once fetched.
     */
    Optional<Bookmark> getBookmark();

    /**
     * Similar to {@link #getBookmark()}, but invalidates any memoized {@link Bookmark}
     * such that the {@link Bookmark} returned is recreated, reflecting the object's current state.
     * @implNote
     * As this is not required, in fact not recommended for entities,
     * (but might be necessary for viewmodels, when their state has changed),
     * we silently ignore bookmark invalidation attempts for entities.
     */
    Optional<Bookmark> getBookmarkRefreshed();

    /**
     * If the underlying domain object is a viewmodel, refreshes any referenced entities.
     * (Acts as a no-op otherwise.)
     * @apiNote usually should be sufficient to refresh once per interaction.
     */
    void refreshViewmodel(@Nullable Supplier<Bookmark> bookmarkSupplier);

    boolean isBookmarkMemoized();

    Supplier<ManagedObject> asSupplier();

    @Deprecated
    void assertSpecIsInSyncWithPojo();

    // -- TITLE

    public default String titleString(final UnaryOperator<TitleRenderRequest.TitleRenderRequestBuilder> onBuilder) {
        return _InternalTitleUtil
                .titleString(onBuilder.apply(
                        TitleRenderRequest.builder()
                        .object(this))
                        .build());
    }

    public default String titleString() {
        return _InternalTitleUtil.titleString(
                TitleRenderRequest.builder()
                .object(this)
                .build());
    }

    // -- SHORTCUT - ELEMENT SPECIFICATION

    /**
     * Used only for (standalone or parented) collections.
     */
    default Optional<ObjectSpecification> getElementSpecification() {
        return getSpecification().getElementSpecification();
    }

    // -- SHORTCUT - TITLE

    default String getTitle() {
        return MmTitleUtil.titleOf(this);
    }

    // -- SHORTCUT - ICON

    /**
     * Returns the name of an icon to use if this object is to be displayed
     * graphically.
     * <p>
     * May return <code>null</code> if no icon is specified.
     */
    default String getIconName() {
        return getSpecification().getIconName(this);
    }

    default ObjectIcon getIcon() {
        return getSpecification().getIcon(this);
    }

    // -- FACTORIES

    /**
     * Factory for Specialization#UNSPECIFIED.
     * @see ManagedObject.Specialization.TypePolicy#NO_TYPE
     * @see ManagedObject.Specialization.BookmarkPolicy#NO_BOOKMARK
     * @see ManagedObject.Specialization.PojoPolicy#NO_POJO
     */
    static ManagedObject unspecified() {
        return _ManagedObjectUnspecified.INSTANCE;
    }
    /**
     * EMPTY
     * @see ManagedObject.Specialization.TypePolicy#ABSTRACT_TYPE_ALLOWED
     * @see ManagedObject.Specialization.BookmarkPolicy#NO_BOOKMARK
     * @see ManagedObject.Specialization.PojoPolicy#NO_POJO
     */
    static ManagedObject empty(final @NonNull ObjectSpecification spec) {
        return new _ManagedObjectEmpty(spec);
    }
    /**
     * VALUE
     * @param pojo
     * @param spec
     * @see ManagedObject.Specialization.TypePolicy#EXACT_TYPE_REQUIRED
     * @see ManagedObject.Specialization.BookmarkPolicy#IMMUTABLE
     * @see ManagedObject.Specialization.PojoPolicy#IMMUTABLE
     */
    static ManagedObject value(
            final @NonNull ObjectSpecification spec,
            final @Nullable Object pojo) {
        return pojo != null
                ? new _ManagedObjectWithEagerSpec(spec, pojo) //FIXME
                //new _ManagedObjectValue(spec, pojo)
                : empty(spec);
    }
    /**
     * SERVICE
     * @param pojo
     * @param spec
     * @see ManagedObject.Specialization.TypePolicy#EXACT_TYPE_REQUIRED
     * @see ManagedObject.Specialization.BookmarkPolicy#IMMUTABLE
     * @see ManagedObject.Specialization.PojoPolicy#IMMUTABLE
     */
    static ManagedObject service(
            final @NonNull ObjectSpecification spec,
            final @NonNull Object pojo) {
        return new _ManagedObjectWithEagerSpec(spec, pojo); //FIXME
    }
    /**
     * VIEWMODEL
     * @param pojo
     * @param spec
     * @see ManagedObject.Specialization.TypePolicy#EXACT_TYPE_REQUIRED
     * @see ManagedObject.Specialization.BookmarkPolicy#REFRESHABLE
     * @see ManagedObject.Specialization.PojoPolicy#STATEFUL
     */
    static ManagedObject viewmodel(
            final @NonNull ObjectSpecification spec,
            final @Nullable Object pojo) {
        return pojo != null
                ? new _ManagedObjectWithEagerSpec(spec, pojo) //FIXME
                : empty(spec);
    }
    /**
     * ENTITY
     * @param pojo
     * @param spec
     * @see ManagedObject.Specialization.TypePolicy#EXACT_TYPE_REQUIRED
     * @see ManagedObject.Specialization.BookmarkPolicy#IMMUTABLE
     * @see ManagedObject.Specialization.PojoPolicy#REFETCHABLE
     */
    static ManagedObject entity(
            final @NonNull ObjectSpecification spec,
            final @Nullable Object pojo) {
        return pojo != null
                ? new _ManagedObjectWithEagerSpec(spec, pojo) //FIXME
                : empty(spec);
    }
    /**
     * MIXIN
     * @param pojo
     * @param spec
     * @see ManagedObject.Specialization.TypePolicy#EXACT_TYPE_REQUIRED
     * @see ManagedObject.Specialization.BookmarkPolicy#NO_BOOKMARK
     * @see ManagedObject.Specialization.PojoPolicy#STATEFUL
     */
    static ManagedObject mixin(
            final @NonNull ObjectSpecification spec,
            final @Nullable Object pojo) {
        return pojo != null
                ? new _ManagedObjectWithEagerSpec(spec, pojo) //FIXME
                : empty(spec);
    }
    /**
     * OTHER
     * @param pojo
     * @param spec
     * @see ManagedObject.Specialization.TypePolicy#EXACT_TYPE_REQUIRED
     * @see ManagedObject.Specialization.BookmarkPolicy#NO_BOOKMARK
     * @see ManagedObject.Specialization.PojoPolicy#STATEFUL
     */
    static ManagedObject other(
            final @NonNull ObjectSpecification spec,
            final @Nullable Object pojo) {
        return pojo != null
                ? new _ManagedObjectWithEagerSpec(spec, pojo) //FIXME
                : empty(spec);
    }
    /**
     * PACKED
     * @see ManagedObject.Specialization.TypePolicy#ABSTRACT_TYPE_ALLOWED
     * @see ManagedObject.Specialization.BookmarkPolicy#NO_BOOKMARK
     * @see ManagedObject.Specialization.PojoPolicy#PACKED
     */
    static PackedManagedObject packed(
            final @NonNull ObjectSpecification elementSpec,
            final @Nullable Can<ManagedObject> nonScalar) {
        return new _ManagedObjectPacked(elementSpec, nonScalar);
    }

    /**
     * For cases, when the pojo's specification is not available and needs to be looked up.
     * <p>
     * Fails if the pojo is non-scalar.
     * @param specLoader - required
     * @param pojo - required, required non-scalar
     */
    static ManagedObject wrapScalar(
            final @NonNull SpecificationLoader specLoader,
            final @NonNull Object pojo) {
        if(pojo instanceof ManagedObject) {
            return (ManagedObject)pojo;
        }
        _Assert.assertFalse(_Collections.isCollectionOrArrayOrCanType(pojo.getClass()));

        val spec = specLoader.specForType(pojo.getClass()).orElse(null);
        val specialization = spec!=null
                ? Specialization.inferFrom(spec, pojo)
                : Specialization.UNSPECIFIED;

        switch(specialization) {
        case UNSPECIFIED:
            return unspecified();
        case VALUE:
            return value(spec, pojo);
        case SERVICE:
            return service(spec, pojo);
        case VIEWMODEL:
            return viewmodel(spec, pojo);
        case ENTITY:
            return entity(spec, pojo);
        case MIXIN:
            return mixin(spec, pojo);
        case OTHER:
            return other(spec, pojo);
        // unreachable (in this context)
        case EMPTY:
        case PACKED:
            throw _Exceptions.unexpectedCodeReach();
        }
        throw _Exceptions.unmatchedCase(specialization);
    }

    // -- FACTORIES LEGACY

    @Deprecated
    static ManagedObject notBookmarked(
            final ObjectSpecification spec,
            final Object pojo) {
        return new _ManagedObjectWithEagerSpec(spec, pojo);
    }

    /**
     * Optimized for cases, when the pojo's specification is already available.
     * If {@code pojo} is an entity, automatically memoizes its bookmark.
     * @param spec
     * @param pojo - might also be a collection of pojos (null-able)
     */
    @Deprecated
    static ManagedObject of(
            final @NonNull ObjectSpecification spec,
            final @Nullable Object pojo) {

        ManagedObjects.assertPojoNotWrapped(pojo);

        //ISIS-2430 Cannot assume Action Param Spec to be correct when eagerly loaded
        //actual type in use (during runtime) might be a sub-class of the above, so re-adapt with hinting spec
        val adapter = spec.getMetaModelContext().getObjectManager().adapt(pojo, spec);
        adapter.assertSpecIsInSyncWithPojo();
        return adapter;
    }

    /**
     * Optimized for cases, when the pojo's specification and bookmark are already available.
     */
    @Deprecated
    static ManagedObject bookmarked(
            final @NonNull ObjectSpecification spec,
            final @NonNull Object pojo,
            final @NonNull Bookmark bookmark) {

        if(pojo!=null) {
            _Assert.assertFalse(_Collections.isCollectionOrArrayOrCanType(pojo.getClass()));
        }

        if(!spec.getCorrespondingClass().isAssignableFrom(pojo.getClass())) {
            throw _Exceptions.illegalArgument(
                    "Pojo not compatible with ObjectSpecification, " +
                    "objectSpec.correspondingClass = %s, " +
                    "pojo.getClass() = %s, " +
                    "pojo.toString() = %s",
                    spec.getCorrespondingClass(), pojo.getClass(), pojo.toString());
        }
        ManagedObjects.assertPojoNotWrapped(pojo);
        return _ManagedObjectWithEagerSpec.identified(spec, pojo, bookmark);
    }

}
