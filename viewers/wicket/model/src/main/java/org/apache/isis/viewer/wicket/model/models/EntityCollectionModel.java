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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.Component;

import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.commons.ClassUtil;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;
import org.apache.isis.viewer.wicket.model.mementos.CollectionMemento;
import org.apache.isis.viewer.wicket.model.models.Util.LowestCommonSuperclassFinder;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.core.webapp.context.memento.ObjectMementoService;

import static org.apache.isis.core.commons.internal.base._NullSafe.stream;

/**
 * Model representing a collection of entities, either {@link Type#STANDALONE
 * standalone} (eg result of invoking an action) or {@link Type#PARENTED
 * parented} (contents of the collection of an entity).
 *
 * <p>
 * So that the model is {@link Serializable}, the {@link ManagedObject}s within
 * the collection are stored as {@link ObjectMemento}s.
 */
public class EntityCollectionModel extends ModelAbstract<List<ManagedObject>> 
implements LinksProvider, UiHintContainer {

    private static final long serialVersionUID = 1L;

    private static final int PAGE_SIZE_DEFAULT_FOR_PARENTED = 12;
    private static final int PAGE_SIZE_DEFAULT_FOR_STANDALONE = 25;

    // -- TOP LEVEL FACTORIES

    public static EntityCollectionModel createParented(EntityModel modelWithCollectionLayoutMetadata) {
        return parentedOf(modelWithCollectionLayoutMetadata);
    }

    public static EntityCollectionModel createStandalone(
            ManagedObject collectionAsAdapter, 
            ModelAbstract<?> model) {

        // dynamically determine the spec of the elements
        // (ie so a List<Object> can be rendered according to the runtime type of its elements,
        // rather than the compile-time type
        final LowestCommonSuperclassFinder lowestCommonSuperclassFinder = new LowestCommonSuperclassFinder();

        //XXX lombok issue, cannot use val here
        final ObjectMementoService mementoService = model.getMementoService();

        final List<ObjectMemento> mementoList = streamElementsOf(collectionAsAdapter) // pojos
                .peek(lowestCommonSuperclassFinder::collect)
                .map(mementoService::mementoForPojo)
                .collect(Collectors.toList());

        final SpecificationLoader specificationLoader = model.getSpecificationLoader();

        final ObjectSpecification elementSpec = lowestCommonSuperclassFinder.getLowestCommonSuperclass()
                .map(specificationLoader::loadSpecification)
                .orElse(collectionAsAdapter.getSpecification().getElementSpecification());

        final Class<?> elementType;
        int pageSize = PAGE_SIZE_DEFAULT_FOR_STANDALONE;
        if (elementSpec != null) {
            elementType = elementSpec.getCorrespondingClass();
            pageSize = pageSize(elementSpec.getFacet(PagedFacet.class), PAGE_SIZE_DEFAULT_FOR_STANDALONE);
        } else {
            elementType = Object.class;
        }

        return standaloneOf(model.getCommonContext(), elementType, mementoList, pageSize);
    }

    // -- LOW LEVEL FACTORIES (PRIVATE)

    private static EntityCollectionModel parentedOf(EntityModel entityModel) {

        final Type type = Type.PARENTED;

        final OneToManyAssociation collection = collectionFor(entityModel);
        final Class<?> typeOf = forName(collection.getSpecification());
        final int pageSize = pageSize(collection.getFacet(PagedFacet.class), PAGE_SIZE_DEFAULT_FOR_PARENTED);
        final SortedByFacet sortedByFacet = collection.getFacet(SortedByFacet.class);

        final EntityCollectionModel colModel = new EntityCollectionModel(
                entityModel.getCommonContext(), type, entityModel, typeOf, pageSize);

        colModel.collectionMemento = new CollectionMemento(collection);
        colModel.sortedBy = sortedByFacet != null ? sortedByFacet.value(): null;
        
        return colModel;
    }

    private static EntityCollectionModel standaloneOf(
            IsisWebAppCommonContext commonContext, 
            Class<?> typeOf, 
            List<ObjectMemento> mementoList, 
            int pageSize) {

        final Type type = Type.STANDALONE;
        final EntityModel entityModel = null;

        final EntityCollectionModel colModel = new EntityCollectionModel(commonContext, type, entityModel, typeOf, pageSize);
        colModel.mementoList = mementoList;
        return colModel;

    }

    // -- VARIANTS

    public enum Type {
        /**
         * A simple list of object mementos, eg the result of invoking an action
         *
         * <p>
         * This deals with both persisted and transient objects.
         */
        STANDALONE {
            @Override
            List<ManagedObject> load(EntityCollectionModel colModel) {

                //XXX lombok issue, cannot use val here 
                boolean isBulkLoad = false;

                return isBulkLoad
                        ? loadElementsInBulk(colModel).collect(Collectors.toList())
                                : loadElementsOneByOne(colModel).collect(Collectors.toList());
            }

            private Stream<ManagedObject> loadElementsInBulk(EntityCollectionModel colModel) {

                //XXX lombok issue, cannot use val here 

                final Stream<RootOid> rootOids = stream(colModel.mementoList)
                        .map(ObjectMemento::asBookmarkIfSupported)
                        .filter(_NullSafe::isPresent)
                        .map(Oid.Factory::ofBookmark);
                
                return ManagedObject._bulkLoadStream(rootOids);
            }

            private Stream<ManagedObject> loadElementsOneByOne(final EntityCollectionModel model) {

                return stream(model.mementoList)
                        .map(model.getCommonContext()::reconstructObject)
                        .filter(_NullSafe::isPresent);
            }

            @Override
            void setObject(EntityCollectionModel colModel, List<ManagedObject> adapterList) {

                //XXX lombok issue, cannot use val here 
                final ObjectMementoService mementoService = colModel.getMementoService();

                colModel.mementoList = _NullSafe.stream(adapterList)
                        .map(mementoService::mementoForObject)
                        .filter(_NullSafe::isPresent)
                        .collect(Collectors.toList());
            }

            @Override
            public String getId(EntityCollectionModel colModel) {
                return null;
            }

            @Override
            public String getName(EntityCollectionModel colModel) {
                PluralFacet facet = colModel.getTypeOfSpecification().getFacet(PluralFacet.class);
                return facet.value();
            }

            @Override
            public int getCount(EntityCollectionModel colModel) {
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
            List<ManagedObject> load(EntityCollectionModel colModel) {

                final ManagedObject adapter = colModel.getCommonContext()
                        .reconstructObject(colModel.getParentObjectAdapterMemento());

                final OneToManyAssociation collection = colModel.collectionMemento
                        .getCollection(colModel.getSpecificationLoader());

                final ManagedObject collectionAsAdapter = collection.get(adapter, InteractionInitiatedBy.USER);

                final List<Object> objectList = asIterable(collectionAsAdapter);

                final Class<? extends Comparator<?>> sortedBy = colModel.sortedBy;
                if(sortedBy != null) {
                    @SuppressWarnings("unchecked")
                    final Comparator<Object> comparator = (Comparator<Object>) InstanceUtil.createInstance(sortedBy);
                    colModel.getCommonContext().injectServicesInto(comparator);
                    Collections.sort(objectList, comparator);
                }

                final List<ManagedObject> adapterList =
                        _Lists.map(objectList, x-> (ManagedObject)colModel.getPojoToAdapter().apply(x));

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
            void setObject(EntityCollectionModel colModel, List<ManagedObject> list) {
                // no-op
                throw new UnsupportedOperationException();
            }

            @Override public String getId(EntityCollectionModel colModel) {
                return colModel.getCollectionMemento().getCollectionId();
            }

            @Override
            public String getName(EntityCollectionModel colModel) {
                return colModel.getCollectionMemento().getCollectionName();
            }

            @Override
            public int getCount(EntityCollectionModel colModel) {
                return load(colModel).size();
            }

            @Override
            public EntityModel.RenderingHint renderingHint() {
                return EntityModel.RenderingHint.PARENTED_PROPERTY_COLUMN;
            }

        };

        abstract List<ManagedObject> load(EntityCollectionModel entityCollectionModel);

        abstract void setObject(EntityCollectionModel entityCollectionModel, List<ManagedObject> list);

        public abstract String getId(EntityCollectionModel entityCollectionModel);
        public abstract String getName(EntityCollectionModel entityCollectionModel);

        public abstract int getCount(EntityCollectionModel entityCollectionModel);

        public abstract EntityModel.RenderingHint renderingHint();
    }


    /**
     * The {@link ActionModel model} of the {@link ObjectAction action}
     * that generated this {@link EntityCollectionModel}.
     *
     * <p>
     * Populated only for {@link Type#STANDALONE standalone} collections.
     *
     * @see #setActionHint(ActionModel)
     */
    public ActionModel getActionModelHint() {
        return actionModelHint;
    }
    /**
     * Called only for {@link Type#STANDALONE standalone} collections.
     *
     * @see #getActionModelHint()
     */
    public void setActionHint(ActionModel actionModelHint) {
        this.actionModelHint = actionModelHint;
    }

    private final Type type;

    public Type getType() {
        return type;
    }

    private final Class<?> typeOf;
    private transient ObjectSpecification typeOfSpec;

    /**
     * Populated only if {@link Type#STANDALONE}.
     */
    private List<ObjectMemento> mementoList;

    /**
     * Populated only if {@link Type#STANDALONE}.
     */
    private Map<String, ObjectMemento> toggledMementos;

    /**
     * Populated only if {@link Type#PARENTED}.
     */
    private final EntityModel entityModel;


    /**
     * Populated only if {@link Type#PARENTED}.
     */
    private CollectionMemento collectionMemento;

    private final int pageSize;

    /**
     * Additional links to render (if any)
     */
    private List<LinkAndLabel> linkAndLabels = _Lists.newArrayList();

    /**
     * Optionally populated only if {@link Type#PARENTED}.
     */
    private Class<? extends Comparator<?>> sortedBy;

    /**
     * Optionally populated, only if {@link Type#STANDALONE} (ie called from an action).
     */
    private ActionModel actionModelHint;

    private EntityCollectionModel(
            IsisWebAppCommonContext commonContext,
            Type type, 
            EntityModel entityModel, 
            Class<?> typeOf, 
            int pageSize) {

        super(commonContext);
        this.type = type;
        this.entityModel = entityModel;
        this.typeOf = typeOf;
        this.pageSize = pageSize;
        this.toggledMementos = _Maps.<String, ObjectMemento>newLinkedHashMap();
    }


    private static OneToManyAssociation collectionFor(EntityModel entityModel) {

        final ObjectMemento parentObjectAdapterMemento = entityModel.getObjectAdapterMemento();
        final CollectionLayoutData collectionLayoutData = entityModel.getCollectionLayoutData();
        final SpecificationLoader specificationLoader = entityModel.getSpecificationLoader();

        if(collectionLayoutData == null) {
            throw new IllegalArgumentException("EntityModel must have a CollectionLayoutMetadata");
        }
        final String collectionId = collectionLayoutData.getId();
        final ObjectSpecId objectSpecId = parentObjectAdapterMemento.getObjectSpecId();
        final ObjectSpecification objectSpec = specificationLoader.lookupBySpecIdElseLoad(objectSpecId);
        final OneToManyAssociation otma = (OneToManyAssociation) objectSpec.getAssociation(collectionId);
        return otma;
    }

    private static Class<?> forName(final ObjectSpecification objectSpec) {
        final String fullName = objectSpec.getFullIdentifier();
        return ClassUtil.forNameElseFail(fullName);
    }


    private static int pageSize(final PagedFacet pagedFacet, final int defaultPageSize) {
        return pagedFacet != null ? pagedFacet.value(): defaultPageSize;
    }

    public boolean isParented() {
        return type == Type.PARENTED;
    }

    public boolean isStandalone() {
        return type == Type.STANDALONE;
    }

    public int getPageSize() {
        return pageSize;
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
        return type.getName(this);
    }

    public int getCount() {
        return this.type.getCount(this);
    }

    /**
     * Populated only if {@link Type#PARENTED}.
     */
    public CollectionLayoutData getLayoutData() {
        return entityModel != null
                ? entityModel.getCollectionLayoutData()
                        : null;
    }

    @Override
    protected List<ManagedObject> load() {
        return type.load(this);
    }

    public ObjectSpecification getTypeOfSpecification() {
        if (typeOfSpec == null) {
            typeOfSpec = getSpecificationLoader().loadSpecification(typeOf);
        }
        return typeOfSpec;
    }

    @Override
    public void setObject(List<ManagedObject> list) {
        super.setObject(list);
        type.setObject(this, list);
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
     * Populated only if {@link Type#PARENTED}.
     */
    public EntityModel getEntityModel() {
        return entityModel;
    }

    /**
     * Populated only if {@link Type#PARENTED}.
     */
    public ObjectMemento getParentObjectAdapterMemento() {
        return entityModel != null? entityModel.getObjectAdapterMemento(): null;
    }

    /**
     * Populated only if {@link Type#PARENTED}.
     */
    public CollectionMemento getCollectionMemento() {
        return collectionMemento;
    }

    private static Iterable<Object> asIterable(ManagedObject resultAdapter) {
        return _Casts.uncheckedCast(resultAdapter.getPojo());
    }

    private static Stream<Object> streamElementsOf(ManagedObject resultAdapter) {
        return _NullSafe.stream(asIterable(resultAdapter));
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
    public List<LinkAndLabel> getLinks() {
        return Collections.unmodifiableList(linkAndLabels);
    }

    public EntityCollectionModel asDummy() {
        return standaloneOf(
                super.getCommonContext(), typeOf, Collections.<ObjectMemento>emptyList(), pageSize);
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
