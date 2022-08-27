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
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.isis.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.NonNull;
import lombok.val;

/**
 * Represents an instance of some element of the meta-model managed by the framework,
 * that is IoC-container provided beans, persistence-stack provided entities or view-models.
 *
 */
public interface ManagedObject extends HasMetaModelContext {

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

    // -- HTML

    public default String htmlString(
            final @Nullable ObjectFeature feature) {
        return _InternalTitleUtil.htmlString(this, feature);
    }

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
        return ManagedObjects.titleOf(this);
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

    public static ManagedObject notBookmarked(
            final ObjectSpecification spec,
            final Object pojo) {
        return _ManagedObjectWithEagerSpec.of(spec, pojo);
    }

    /**
     * Optimized for cases, when the pojo's specification is already available.
     * If {@code pojo} is an entity, automatically memoizes its bookmark.
     * @param spec
     * @param pojo - might also be a collection of pojos (null-able)
     */
    public static ManagedObject of(
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
    public static ManagedObject bookmarked(
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

    /**
     * For cases, when the pojo's specification is not available and needs to be looked up.
     * @param specLoader
     * @param pojo
     */
    public static ManagedObject lazy(
            final SpecificationLoader specLoader,
            final Object pojo) {

        if(pojo!=null) {
            _Assert.assertFalse(_Collections.isCollectionOrArrayOrCanType(pojo.getClass()));
        }

        ManagedObjects.assertPojoNotWrapped(pojo);
        val adapter = new _ManagedObjectWithLazySpec(cls->specLoader.specForType(cls).orElse(null), pojo);
        //ManagedObjects.warnIfAttachedEntity(adapter, "consider using ManagedObject.identified(...) for entity");
        return adapter;
    }

    /** has no ObjectSpecification and no value (pojo) */
    static ManagedObject unspecified() {
        return _ManagedObjectAbstract.UNSPECIFIED;
    }

    /** has an ObjectSpecification, but no value (pojo) */
    static ManagedObject empty(final @NonNull ObjectSpecification spec) {
        return _ManagedObjectWithEagerSpec.of(spec, null);
    }

}
