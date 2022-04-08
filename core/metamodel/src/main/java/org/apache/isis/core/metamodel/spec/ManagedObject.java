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
package org.apache.isis.core.metamodel.spec;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.debug._XrayEvent;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.isis.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;

/**
 * Represents an instance of some element of the meta-model managed by the framework,
 * that is IoC-container provided beans, persistence-stack provided entities or view-models.
 *
 */
public interface ManagedObject {

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
     * Introduced, so we can re-fetch detached entity pojos in place.
     * @apiNote should be package private, and not publicly exposed
     * (but the <i>Java</i> language is not there yet)
     */
    void replacePojo(UnaryOperator<Object> replacer);

    void replaceBookmark(UnaryOperator<Bookmark> replacer);

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
     * Reload current viewmodel object from memoized bookmark, otherwise does nothing.
     */
    default void reloadViewmodelFromMemoizedBookmark() {
        val spec = getSpecification();
        if(isBookmarkMemoized()
                && spec.isViewModel()) {

            val bookmark = getBookmark().get();
            val viewModelClass = spec.getCorrespondingClass();

            val recreatedViewmodel =
                    getMetaModelContext().getFactoryService().viewModel(viewModelClass, bookmark);

            _XrayEvent.event("Viewmodel '%s' recreated from memoized bookmark.", viewModelClass.getName());

            replacePojo(old->recreatedViewmodel);
        }
    }

    default void reloadViewmodelFromBookmark(final @NonNull Bookmark bookmark) {
        val spec = getSpecification();
        if(spec.isViewModel()) {
            val viewModelClass = spec.getCorrespondingClass();

            val recreatedViewmodel =
                    getMetaModelContext().getFactoryService().viewModel(viewModelClass, bookmark);

            _XrayEvent.event("Viewmodel '%s' recreated from provided bookmark.", viewModelClass.getName());

            replacePojo(old->recreatedViewmodel);
            replaceBookmark(old->bookmark);
        }
    }

    boolean isBookmarkMemoized();

    default Supplier<ManagedObject> asProvider() {
        return ()->this;
    }

    /** debug */
    default void assertSpecIsInSyncWithPojo() {
//        val pojo = getPojo();
//        val spec = getSpecification();
//        if(pojo==null
//                || spec==null) {
//            return;
//        }
//        val actualSpec = spec.getSpecificationLoader().specForType(pojo.getClass()).orElse(null);
//        if(!Objects.equals(spec,  actualSpec)) {
//            System.err.printf("spec mismatch %s %s%n", spec, actualSpec);
//        }
        //_Assert.assertEquals(spec, actualSpec);
    }

    // -- HTML

    public default String htmlString(
            final @Nullable ObjectFeature feature) {

        if(getSpecification()==null) {
            return "";
        }

        val spec = getSpecification();
        val valueFacet = spec.valueFacet().orElse(null);

        if(valueFacet==null) {
            return String.format("missing ValueFacet %s", spec.getCorrespondingClass());
        }

        val renderer = (Renderer<Object>) valueFacet.selectRendererForFeature(feature).orElse(null);
        if(renderer==null) {
            return String.format("missing Renderer %s", spec.getCorrespondingClass());
        }

        return renderer.htmlPresentation(valueFacet.createValueSemanticsContext(feature), this.getPojo());
    }

    // -- TITLE

    public default String titleString(final UnaryOperator<TitleRenderRequest.TitleRenderRequestBuilder> onBuilder) {
        return ManagedObjects.TitleUtil
                .titleString(onBuilder.apply(
                        TitleRenderRequest.builder()
                        .object(this))
                        .build());
    }

    public default String titleString() {
        return ManagedObjects.TitleUtil.titleString(
                TitleRenderRequest.builder()
                .object(this)
                .build());
    }

    // -- SHORTCUTS - MM CONTEXT

    default MetaModelContext getMetaModelContext() {
        return ManagedObjects.spec(this)
                .map(ObjectSpecification::getMetaModelContext)
                .orElseThrow(()->_Exceptions
                        .illegalArgument("Can only retrieve MetaModelContext from ManagedObjects "
                                + "that have an ObjectSpecification."));
    }

    default ObjectManager getObjectManager() {
        return getMetaModelContext().getObjectManager();
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
        return SimpleManagedObject.of(spec, pojo);
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
        return SimpleManagedObject.identified(spec, pojo, bookmark);
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
        val adapter = new LazyManagedObject(cls->specLoader.specForType(cls).orElse(null), pojo);
        //ManagedObjects.warnIfAttachedEntity(adapter, "consider using ManagedObject.identified(...) for entity");
        return adapter;
    }

    // -- EMPTY

    /** has no ObjectSpecification and no value (pojo) */
    static ManagedObject unspecified() {
        return ManagedObjects.UNSPECIFIED;
    }

    /** has an ObjectSpecification, but no value (pojo) */
    static ManagedObject empty(final @NonNull ObjectSpecification spec) {
        return SimpleManagedObject.of(spec, null);
    }

    // -- LAZY BOOKMARK HANDLING

    static abstract class ManagedObjectWithBookmark
    implements ManagedObject {

        protected final _Lazy<Optional<Bookmark>> bookmarkLazy =
                _Lazy.threadSafe(()->bookmark(this));

        @Override
        public final Optional<Bookmark> getBookmark() {
            return bookmarkLazy.get();
        }

        @Override
        public final boolean isBookmarkMemoized() {
            return bookmarkLazy.isMemoized();
        }

        @Override
        public final Optional<Bookmark> getBookmarkRefreshed() {
            // silently ignore invalidation, when the pojo is an entity
            if(!getSpecification().isEntity()) {
                bookmarkLazy.clear();
            }
            return getBookmark();
        }

        @Override
        public final void replaceBookmark(final UnaryOperator<Bookmark> replacer) {
            final Bookmark old = bookmarkLazy.isMemoized()
                    ? bookmarkLazy.get().orElse(null)
                    : null;
            bookmarkLazy.clear();
            bookmarkLazy.set(Optional.ofNullable(replacer.apply(old)));
        }

        // guards against non-identifiable objects;
        // historically, we allowed non-identifiable to be handled by the objectManager,
        // which as a fallback creates 'random' UUIDs
        private Optional<Bookmark> bookmark(final @Nullable ManagedObject adapter) {

            if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)
                    || adapter.getSpecification().isValue()
                    || !ManagedObjects.isIdentifiable(adapter)) {
                return Optional.empty();
            }

            return ManagedObjects.spec(adapter)
                    .map(ObjectSpecification::getMetaModelContext)
                    .map(MetaModelContext::getObjectManager)
                    .map(objectManager->objectManager.bookmarkObject(adapter));
        }

    }

    // -- SIMPLE

    //@Value
    //@RequiredArgsConstructor(staticName="of", access = AccessLevel.PRIVATE)
    @AllArgsConstructor(staticName="of", access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(of = "pojo", callSuper = false)
    @ToString(of = {"specification", "pojo"}) //ISIS-2317 make sure toString() is without side-effects
    @Getter
    static final class SimpleManagedObject
    extends ManagedObjectWithBookmark {

        public static ManagedObject identified(
                final @NonNull  ObjectSpecification spec,
                final @Nullable Object pojo,
                final @NonNull  Bookmark bookmark) {

            if(pojo!=null) {
                _Assert.assertFalse(_Collections.isCollectionOrArrayOrCanType(pojo.getClass()));
            }

            val managedObject = SimpleManagedObject.of(spec, pojo);
            managedObject.bookmarkLazy.set(Optional.of(bookmark));
            return managedObject;
        }

        @NonNull private final ObjectSpecification specification;
        @Nullable private /*final*/ Object pojo;

        @Override
        public void replacePojo(final UnaryOperator<Object> replacer) {
            pojo = replacer.apply(pojo);
            assertSpecIsInSyncWithPojo();
        }

    }

    // -- LAZY

    @EqualsAndHashCode(of = "pojo", callSuper = false)
    static final class LazyManagedObject
    extends ManagedObjectWithBookmark {

        @NonNull private final Function<Class<?>, ObjectSpecification> specLoader;

        @Getter @NonNull private /*final*/ Object pojo;

        private final _Lazy<ObjectSpecification> specification = _Lazy.threadSafe(this::loadSpec);

        public LazyManagedObject(
                final @NonNull Function<Class<?>, ObjectSpecification> specLoader,
                final @NonNull Object pojo) {
            this.specLoader = specLoader;
            this.pojo = pojo;
        }

        @Override
        public ObjectSpecification getSpecification() {
            return specification.get();
        }

        @Override //ISIS-2317 make sure toString() is without side-effects
        public String toString() {
            if(specification.isMemoized()) {
                return String.format("ManagedObject[spec=%s, pojo=%s]",
                        ""+getSpecification(),
                        ""+getPojo());
            }
            return String.format("ManagedObject[spec=%s, pojo=%s]",
                    "[lazy not loaded]",
                    ""+getPojo());
        }

        private ObjectSpecification loadSpec() {
            return specLoader.apply(pojo.getClass());
        }

        @Override
        public void replacePojo(final UnaryOperator<Object> replacer) {
            pojo = replacer.apply(pojo);
            if(specification.isMemoized()) {
                assertSpecIsInSyncWithPojo();
            }
        }

    }


}
