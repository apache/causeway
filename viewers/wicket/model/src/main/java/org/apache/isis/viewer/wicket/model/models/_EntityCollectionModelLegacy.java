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

package org.apache.isis.viewer.wicket.model.models;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.factory._InstanceUtil;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.commons.ClassUtil;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.memento.CollectionMemento;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.memento.ObjectMemento;
import org.apache.isis.core.runtime.memento.ObjectMementoService;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.Getter;
import lombok.val;

/**
 * Model representing a collection of entities, either {@link Variant#STANDALONE
 * standalone} (eg result of invoking an action) or {@link Variant#PARENTED
 * parented} (contents of the collection of an entity).
 *
 * <p>
 * So that the model is {@link Serializable}, the {@link ManagedObject}s within
 * the collection are stored as {@link ObjectMemento}s.
 */
@Deprecated
class _EntityCollectionModelLegacy
extends ModelAbstract<List<ManagedObject>>
implements
    LinksProvider,
    UiHintContainer {

    private static final long serialVersionUID = 1L;

    private static final int PAGE_SIZE_DEFAULT_FOR_PARENTED = 12;
    private static final int PAGE_SIZE_DEFAULT_FOR_STANDALONE = 25;

    // -- FACTORIES

    public static _EntityCollectionModelLegacy createParented(EntityModel entityModel) {

        val oneToManyAssociation = collectionFor(entityModel);
        val typeOf = forName(oneToManyAssociation.getSpecification());
        final int pageSize = PagedFacet.pageSizeOrDefault(
                oneToManyAssociation.getFacet(PagedFacet.class), PAGE_SIZE_DEFAULT_FOR_PARENTED);
        val sortedByFacet = oneToManyAssociation.getFacet(SortedByFacet.class);

        val entityCollectionModel = new _EntityCollectionModelLegacy(
                entityModel.getCommonContext(),
                Variant.PARENTED,
                oneToManyAssociation.getIdentifier(),
                entityModel,
                typeOf,
                pageSize);
        entityCollectionModel.collectionMemento = CollectionMemento.forCollection(oneToManyAssociation);
        entityCollectionModel.sortedBy = (sortedByFacet != null)
                ? sortedByFacet.value()
                : null;
        return entityCollectionModel;
    }

    public static _EntityCollectionModelLegacy createStandalone(
            ManagedObject collectionAsAdapter,
            ModelAbstract<?> model) {

        // dynamically determine the spec of the elements
        // (ie so a List<Object> can be rendered according to the runtime type of its elements,
        // rather than the compile-time type
        val commonSuperClassFinder = new ClassExtensions.CommonSuperclassFinder();

        val mementoService = model.getMementoService();

        final List<ObjectMemento> mementoList = streamElementsOf(collectionAsAdapter) // pojos
                .filter(_NullSafe::isPresent)
                .peek(commonSuperClassFinder::collect)
                .map(mementoService::mementoForPojo)
                .collect(Collectors.toList());

        val specificationLoader = model.getSpecificationLoader();

        val elementSpec = commonSuperClassFinder.getCommonSuperclass()
                .flatMap(specificationLoader::specForType)
                .orElseGet(()->collectionAsAdapter.getSpecification().getElementSpecification().orElse(null));

        final int pageSize = (elementSpec != null)
                ? PagedFacet.pageSizeOrDefault(elementSpec.getFacet(PagedFacet.class), PAGE_SIZE_DEFAULT_FOR_STANDALONE)
                : PAGE_SIZE_DEFAULT_FOR_STANDALONE;

        val elementType = (elementSpec != null)
                ? elementSpec.getCorrespondingClass()
                : Object.class;

        val entityCollectionModel = new _EntityCollectionModelLegacy(
                model.getCommonContext(),
                Variant.STANDALONE,
                /*Identifier*/ null,
                /*EntityModel*/null,
                elementType,
                pageSize);
        entityCollectionModel.mementoList = mementoList;
        return entityCollectionModel;

    }

    // -- VARIANTS

    public enum Variant {
        /**
         * A simple list of object mementos, eg the result of invoking an action
         *
         * <p>
         * This deals with both persisted and transient objects.
         */
        STANDALONE {
            @Override
            List<ManagedObject> load(_EntityCollectionModelLegacy colModel) {

                return loadElementsOneByOne(colModel).collect(Collectors.toList());
            }


            private Stream<ManagedObject> loadElementsOneByOne(final _EntityCollectionModelLegacy model) {

                return stream(model.mementoList)
                        .map(model.getCommonContext()::reconstructObject)
                        .filter(_NullSafe::isPresent);
            }

            @Override
            void setObject(_EntityCollectionModelLegacy colModel, List<ManagedObject> adapterList) {

                //XXX lombok issue, cannot use val here
                final ObjectMementoService mementoService = colModel.getMementoService();

                colModel.mementoList = _NullSafe.stream(adapterList)
                        .map(mementoService::mementoForObject)
                        .filter(_NullSafe::isPresent)
                        .collect(Collectors.toList());
            }

            @Override
            public String getId(_EntityCollectionModelLegacy colModel) {
                return null;
            }

            @Override
            protected String getName(_EntityCollectionModelLegacy colModel) {
                PluralFacet facet = colModel.getTypeOfSpecification().getFacet(PluralFacet.class);
                return facet.value();
            }

            @Override
            public int getCount(_EntityCollectionModelLegacy colModel) {
                return colModel.mementoList.size();
            }

            @Override
            public EntityModel.RenderingHint renderingHint() {
                return EntityModel.RenderingHint.STANDALONE_PROPERTY_COLUMN;
            }

        },

        /**
         * A collection of an entity (eg Order/OrderDetail).
         */
        PARENTED {
            @Override
            List<ManagedObject> load(_EntityCollectionModelLegacy colModel) {

                final ManagedObject adapter = colModel.getCommonContext()
                        .reconstructObject(colModel.getParentObjectAdapterMemento());

                final OneToManyAssociation collection = colModel.collectionMemento
                        .getCollection(colModel.getSpecificationLoader());

                final ManagedObject collectionAsAdapter = collection.get(adapter, InteractionInitiatedBy.USER);

                final List<Object> objectList = asIterable(collectionAsAdapter);

                final Class<? extends Comparator<?>> sortedBy = colModel.sortedBy;
                if(sortedBy != null) {
                    @SuppressWarnings("unchecked")
                    final Comparator<Object> comparator = (Comparator<Object>) _InstanceUtil.createInstance(sortedBy);
                    colModel.getCommonContext().injectServicesInto(comparator);
                    Collections.sort(objectList, comparator);
                }

                final List<ManagedObject> adapterList =
                        _Lists.map(objectList, x-> colModel.getObjectManager().adapt(x));

                return adapterList;
            }

            @SuppressWarnings("unchecked")
            private List<Object> asIterable(ManagedObject collectionAsAdapter) {
                if(collectionAsAdapter==null) {
                    return Collections.emptyList();
                }
                final Iterable<Object> objects = (Iterable<Object>) collectionAsAdapter.getPojo();
                return stream(objects).collect(Collectors.toList());
            }

            @Override
            void setObject(_EntityCollectionModelLegacy colModel, List<ManagedObject> list) {
                // no-op
                throw new UnsupportedOperationException();
            }

            @Override public String getId(_EntityCollectionModelLegacy colModel) {
                return colModel.getCollectionMemento().getCollectionId();
            }

            @Override
            protected String getName(_EntityCollectionModelLegacy colModel) {
                return colModel.getCollectionMemento().getCollectionName();
            }

            @Override
            public int getCount(_EntityCollectionModelLegacy colModel) {
                return load(colModel).size();
            }

            @Override
            public EntityModel.RenderingHint renderingHint() {
                return EntityModel.RenderingHint.PARENTED_PROPERTY_COLUMN;
            }

        };

        abstract List<ManagedObject> load(_EntityCollectionModelLegacy entityCollectionModel);

        abstract void setObject(_EntityCollectionModelLegacy entityCollectionModel, List<ManagedObject> list);

        public abstract String getId(_EntityCollectionModelLegacy entityCollectionModel);
        protected abstract String getName(_EntityCollectionModelLegacy entityCollectionModel);

        public abstract int getCount(_EntityCollectionModelLegacy entityCollectionModel);

        public abstract EntityModel.RenderingHint renderingHint();
    }


    /**
     * The {@link ActionModel model} of the {@link ObjectAction action}
     * that generated this {@link _EntityCollectionModelLegacy}.
     *
     * <p>
     * Populated only for {@link Variant#STANDALONE standalone} collections.
     *
     * @see #setActionHint(ActionModel)
     */
    public ActionModel getActionModelHint() {
        return actionModelHint;
    }
    /**
     * Called only for {@link Variant#STANDALONE standalone} collections.
     *
     * @see #getActionModelHint()
     */
    public void setActionHint(ActionModel actionModelHint) {
        this.actionModelHint = actionModelHint;
    }

    @Getter private final Variant variant;

    private final Class<?> typeOf;
    private transient Optional<ObjectSpecification> typeOfSpec;

    /**
     * Populated only if {@link Variant#STANDALONE}.
     */
    private List<ObjectMemento> mementoList;

    /**
     * Populated only if {@link Variant#STANDALONE}.
     */
    private Map<String, ObjectMemento> toggledMementos;

    /**
     * Populated only if {@link Variant#PARENTED}.
     */
    private final EntityModel entityModel;

    /**
     * Populated only if {@link Variant#PARENTED}.
     * <p>
     * This collection's <i>feature</i> {@link Identifier}.
     * @see Identifier
     */
    @Getter private final Identifier identifier;


    /**
     * Populated only if {@link Variant#PARENTED}.
     */
    private CollectionMemento collectionMemento;

    @Getter private final int pageSize;

    /**
     * Additional links to render (if any)
     */
    private List<LinkAndLabel> linkAndLabels = _Lists.newArrayList();

    /**
     * Optionally populated only if {@link Variant#PARENTED}.
     */
    private Class<? extends Comparator<?>> sortedBy;

    /**
     * Optionally populated, only if {@link Variant#STANDALONE} (ie called from an action).
     */
    private ActionModel actionModelHint;

    private _EntityCollectionModelLegacy(
            IsisAppCommonContext commonContext,
            Variant type,
            Identifier identifier,
            EntityModel entityModel,
            Class<?> typeOf,
            int pageSize) {

        super(commonContext);
        this.variant = type;
        this.identifier = identifier;
        this.entityModel = entityModel;
        this.typeOf = typeOf;
        this.pageSize = pageSize;
        this.toggledMementos = _Maps.<String, ObjectMemento>newLinkedHashMap();
    }


    private static OneToManyAssociation collectionFor(EntityModel entityModel) {

        val collectionLayoutData = entityModel.getCollectionLayoutData();
        if(collectionLayoutData == null) {
            throw new IllegalArgumentException("EntityModel must have a CollectionLayoutMetadata");
        }

        val collectionId = collectionLayoutData.getId();
        val spec = entityModel.getTypeOfSpecification();

        return (OneToManyAssociation) spec.getAssociationElseFail(collectionId);
    }

    private static Class<?> forName(final ObjectSpecification objectSpec) {
        final String fullName = objectSpec.getFullIdentifier();
        return ClassUtil.forNameElseFail(fullName);
    }


    public boolean isParented() {
        return variant == Variant.PARENTED;
    }

    public boolean isStandalone() {
        return variant == Variant.STANDALONE;
    }

    /**
     * The name of the collection (if has an entity, ie, if
     * {@link #isParented() is parented}.)
     *
     * <p>
     * If {@link #isStandalone()}, returns the {@link PluralFacet} of the {@link #getTypeOfSpecification() specification}
     * (eg 'Customers').
     */
    public String getName() {
        return variant.getName(this);
    }

    public int getCount() {
        return this.variant.getCount(this);
    }

    /**
     * Populated only if {@link Variant#PARENTED}.
     */
    public CollectionLayoutData getLayoutData() {
        return entityModel != null
                ? entityModel.getCollectionLayoutData()
                : null;
    }

    @Override
    protected List<ManagedObject> load() {
        return variant.load(this);
    }

    public @Nullable ObjectSpecification getTypeOfSpecification() {
        if (typeOfSpec == null) {
            typeOfSpec = getSpecificationLoader().specForType(typeOf);
        }
        return typeOfSpec.orElse(null);
    }

    @Override
    public void setObject(List<ManagedObject> list) {
        super.setObject(list);
        variant.setObject(this, list);
    }

    /**
     * Not API, but to refresh the model list.
     */
    public void setObjectList(ManagedObject resultAdapter) {
        this.mementoList = streamElementsOf(resultAdapter)
                .map(super.getMementoService()::mementoForPojo)
                .collect(Collectors.toList());
    }

    /**
     * Populated only if {@link Variant#PARENTED}.
     */
    public EntityModel getEntityModel() {
        return entityModel;
    }

    /**
     * Populated only if {@link Variant#PARENTED}.
     */
    public Optional<Bookmark> getParentObjectBookmark() {
        return entityModel != null
                ? entityModel.getManagedObject().getBookmark()
                : Optional.empty();
    }

    /**
     * Populated only if {@link Variant#PARENTED}.
     */
    public Optional<ObjectSpecification> getParentObjectSpecification() {
        return getParentObjectBookmark()
                .flatMap(bookmark->getCommonContext().getSpecificationLoader().specForBookmark(bookmark));
    }

    /**
     * Populated only if {@link Variant#PARENTED}.
     */
    @Deprecated // don't expose this implementation detail, use getParentObjectBookmark() instead
    public ObjectMemento getParentObjectAdapterMemento() {
        return entityModel != null? entityModel.memento(): null;
    }

    /**
     * Populated only if {@link Variant#PARENTED}.
     */
    @Deprecated // don't expose this implementation detail
    public CollectionMemento getCollectionMemento() {
        return collectionMemento;
    }

    private static Stream<?> streamElementsOf(ManagedObject resultAdapter) {
        return _NullSafe.streamAutodetect(resultAdapter.getPojo());
    }

    public boolean toggleSelectionOn(ManagedObject selectedAdapter) {
        //XXX lombok issue, cannot use val here
        final ObjectMemento selectedAsMemento = super.getMementoService().mementoForObject(selectedAdapter);
        final String selectedKey = selectedAsMemento.asString();

        final boolean isSelected = _Maps.toggleElement(toggledMementos, selectedKey, selectedAsMemento);
        return isSelected;

    }

    public Can<ObjectMemento> getToggleMementosList() {
        return Can.ofCollection(this.toggledMementos.values());
    }

    public void clearToggleMementosList() {
        this.toggledMementos.clear();
    }

    public void addLinkAndLabels(List<LinkAndLabel> linkAndLabels) {
        this.linkAndLabels.clear();
        this.linkAndLabels.addAll(linkAndLabels);
    }

    @Override
    public Can<LinkAndLabel> getLinks() {
        return Can.ofCollection(linkAndLabels);
    }

    public _EntityCollectionModelLegacy asDummy() {
        final _EntityCollectionModelLegacy dummy = new _EntityCollectionModelLegacy(
                super.getCommonContext(), Variant.STANDALONE, null, null, typeOf, pageSize);
        dummy.mementoList = Collections.<ObjectMemento>emptyList();
        return dummy;
    }

    // //////////////////////////////////////

    public static final String HINT_KEY_SELECTED_ITEM = "selectedItem";

    /**
     * Just delegates to the {@link #getEntityModel() entity model} (if parented, else no-op).
     */
    @Override
    public String getHint(final Component component, final String attributeName) {
        if(getEntityModel() == null) {
            return null;
        }
        return getEntityModel().getHint(component, attributeName);
    }

    /**
     * Just delegates to the {@link #getEntityModel() entity model} (if parented, else no-op).
     */
    @Override
    public void setHint(final Component component, final String attributeName, final String attributeValue) {
        if(getEntityModel() == null) {
            return;
        }
        getEntityModel().setHint(component, attributeName, attributeValue);
    }

    /**
     * Just delegates to the {@link #getEntityModel() entity model} (if parented, else no-op).
     */
    @Override
    public void clearHint(final Component component, final String attributeName) {
        if(getEntityModel() == null) {
            return;
        }
        getEntityModel().clearHint(component, attributeName);
    }




}
