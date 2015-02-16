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
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.commons.lang.ClassUtil;
import org.apache.isis.core.commons.lang.Closure;
import org.apache.isis.core.commons.lang.IterableExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;
import org.apache.isis.viewer.wicket.model.mementos.CollectionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

/**
 * Model representing a collection of entities, either {@link Type#STANDALONE
 * standalone} (eg result of invoking an action) or {@link Type#PARENTED
 * parented} (contents of the collection of an entity).
 * 
 * <p>
 * So that the model is {@link Serializable}, the {@link ObjectAdapter}s within
 * the collection are stored as {@link ObjectAdapterMemento}s.
 */
public class EntityCollectionModel extends ModelAbstract<List<ObjectAdapter>> implements LinksProvider {

    private static final long serialVersionUID = 1L;

    private static final int PAGE_SIZE_DEFAULT_FOR_PARENTED = 12;
    private static final int PAGE_SIZE_DEFAULT_FOR_STANDALONE = 25;

    public enum Type {
        /**
         * A simple list of object mementos, eg the result of invoking an action
         * 
         * <p>
         * This deals with both persisted and transient objects.
         */
        STANDALONE {
            @Override
            List<ObjectAdapter> load(final EntityCollectionModel entityCollectionModel) {
                return Lists.transform(entityCollectionModel.mementoList, ObjectAdapterMemento.Functions.fromMemento(ConcurrencyChecking.NO_CHECK));
            }

            @Override
            void setObject(final EntityCollectionModel entityCollectionModel, final List<ObjectAdapter> list) {
                entityCollectionModel.mementoList = Lists.newArrayList(Lists.transform(list, ObjectAdapterMemento.Functions.toMemento()));
            }

            @Override
            public String getName(final EntityCollectionModel model) {
                PluralFacet facet = model.getTypeOfSpecification().getFacet(PluralFacet.class);
                return facet.value();
            }

            @Override
            public int getCount(final EntityCollectionModel model) {
                return model.mementoList.size();
            }

        },
        /**
         * A collection of an entity (eg Order/OrderDetail).
         */
        PARENTED {
            @Override
            List<ObjectAdapter> load(final EntityCollectionModel entityCollectionModel) {
                final ObjectAdapter adapter = entityCollectionModel.parentObjectAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
                final OneToManyAssociation collection = entityCollectionModel.collectionMemento.getCollection();
                final ObjectAdapter collectionAsAdapter = collection.get(adapter);

                final List<Object> objectList = asIterable(collectionAsAdapter);

                final Class<? extends Comparator<?>> sortedBy = entityCollectionModel.sortedBy;
                if(sortedBy != null) {
                    @SuppressWarnings("unchecked")
                    final Comparator<Object> comparator = (Comparator<Object>) InstanceUtil.createInstance(sortedBy);
                    getServicesInjector().injectServicesInto(comparator);
                    Collections.sort(objectList, comparator);
                }

                final Iterable<ObjectAdapter> adapterIterable = Iterables.transform(objectList, ObjectAdapter.Functions.adapterForUsing(getAdapterManagerStatic()));
                final List<ObjectAdapter> adapterList = Lists.newArrayList(adapterIterable);

                return adapterList;
            }

            @SuppressWarnings("unchecked")
            private List<Object> asIterable(final ObjectAdapter collectionAsAdapter) {
                final Iterable<Object> objects = (Iterable<Object>) collectionAsAdapter.getObject();
                return Lists.newArrayList(objects);
            }

            @Override
            void setObject(EntityCollectionModel entityCollectionModel, List<ObjectAdapter> list) {
                // no-op
                throw new UnsupportedOperationException();
            }

            @Override
            public String getName(EntityCollectionModel model) {
                return model.getCollectionMemento().getName();
            }

            @Override
            public int getCount(EntityCollectionModel model) {
                return load(model).size();
            }
        };

        abstract List<ObjectAdapter> load(EntityCollectionModel entityCollectionModel);

        abstract void setObject(EntityCollectionModel entityCollectionModel, List<ObjectAdapter> list);

        public abstract String getName(EntityCollectionModel entityCollectionModel);

        public abstract int getCount(EntityCollectionModel entityCollectionModel);
    }

    static class LowestCommonSuperclassClosure implements Closure<Class<?>>{
        private Class<?> common;
        @Override
        public Class<?> execute(final Class<?> value) {
            if(common == null) {
                common = value;
            } else {
                Class<?> current = common;
                while(!current.isAssignableFrom(value)) {
                    current = current.getSuperclass();
                }
                common = current;
            }
            return common;
        }
        Class<?> getLowestCommonSuperclass() { 
            return common; 
        }
    }

    /**
     * Factory.
     */
    public static EntityCollectionModel createStandalone(final ObjectAdapter collectionAsAdapter) {
        final Iterable<Object> pojos = EntityCollectionModel.asIterable(collectionAsAdapter);
        
        final List<ObjectAdapterMemento> mementoList = 
                Lists.newArrayList(Iterables.transform(pojos, ObjectAdapterMemento.Functions.fromPojo(getAdapterManagerStatic())));

        
        final ObjectSpecification elementSpec;
        if(!Iterables.isEmpty(pojos)) {
            // dynamically determine the spec of the elements
            // (ie so a List<Object> can be rendered according to the runtime type of its elements, 
            // rather than the compile-time type
            final LowestCommonSuperclassClosure closure = new LowestCommonSuperclassClosure();
            Function<Object, Class<?>> function = new Function<Object, Class<?>>(){
                @Override
                public Class<?> apply(Object obj) {
                    return obj.getClass();
                }
            };
            IterableExtensions.fold(Iterables.transform(pojos,  function), closure);
            elementSpec = getSpecificationLoaderStatic().loadSpecification(closure.getLowestCommonSuperclass());
        } else {
            elementSpec = collectionAsAdapter.getElementSpecification();
        }

        final Class<?> elementType;
        int pageSize = PAGE_SIZE_DEFAULT_FOR_STANDALONE;
        if (elementSpec != null) {
            elementType = elementSpec.getCorrespondingClass();
            pageSize = pageSize(elementSpec.getFacet(PagedFacet.class), PAGE_SIZE_DEFAULT_FOR_STANDALONE);
        } else {
            elementType = Object.class;
        }
        
        return new EntityCollectionModel(elementType, mementoList, pageSize);
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

    /**
     * Factory.
     */
    public static EntityCollectionModel createParented(final EntityModel model, final OneToManyAssociation collection) {
        return new EntityCollectionModel(model, collection);
    }

    /**
     * Factory.
     */
    public static EntityCollectionModel createParented(final ObjectAdapter adapter, final OneToManyAssociation collection) {
        return new EntityCollectionModel(adapter, collection);
    }

    private final Type type;

    private final Class<?> typeOf;
    private transient ObjectSpecification typeOfSpec;

    /**
     * Populated only if {@link Type#STANDALONE}.
     */
    private List<ObjectAdapterMemento> mementoList;

    /**
     * Populated only if {@link Type#STANDALONE}.
     */
    private List<ObjectAdapterMemento> toggledMementosList;

    /**
     * Populated only if {@link Type#PARENTED}.
     */
    private ObjectAdapterMemento parentObjectAdapterMemento;

    /**
     * Populated only if {@link Type#PARENTED}.
     */
    private CollectionMemento collectionMemento;

    private final int pageSize;

    /**
     * Additional links to render (if any)
     */
    private List<LinkAndLabel> entityActions = Lists.newArrayList();

    /**
     * Optionally populated only if {@link Type#PARENTED}.
     */
    private Class<? extends Comparator<?>> sortedBy;

    /**
     * Optionally populated, only if {@link Type#STANDALONE} (ie called from an action).
     */
    private ActionModel actionModelHint;

    private EntityCollectionModel(final Class<?> typeOf, final List<ObjectAdapterMemento> mementoList, final int pageSize) {
        this.type = Type.STANDALONE;
        this.typeOf = typeOf;
        this.mementoList = mementoList;
        this.pageSize = pageSize;
        this.toggledMementosList = Lists.newArrayList();
    }

    private EntityCollectionModel(final ObjectAdapter adapter, final OneToManyAssociation collection) {
        this(ObjectAdapterMemento.createOrNull(adapter), collection);
    }

    private EntityCollectionModel(final EntityModel model, final OneToManyAssociation collection) {
        this(model.getObjectAdapterMemento(), collection);
    }

    private EntityCollectionModel(final ObjectAdapterMemento parentAdapterMemento, final OneToManyAssociation collection) {
        this.type = Type.PARENTED;
        this.typeOf = forName(collection.getSpecification());
        this.parentObjectAdapterMemento = parentAdapterMemento;
        this.collectionMemento = new CollectionMemento(collection);
        this.pageSize = pageSize(collection.getFacet(PagedFacet.class), PAGE_SIZE_DEFAULT_FOR_PARENTED);
        final SortedByFacet sortedByFacet = collection.getFacet(SortedByFacet.class);
        this.sortedBy = sortedByFacet != null?sortedByFacet.value(): null;
    }
    
    private static Class<?> forName(final ObjectSpecification objectSpec) {
        final String fullName = objectSpec.getFullIdentifier();
        return ClassUtil.forName(fullName);
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


    @Override
    protected List<ObjectAdapter> load() {
        return type.load(this);
    }

    public ObjectSpecification getTypeOfSpecification() {
        if (typeOfSpec == null) {
            typeOfSpec = getSpecificationLoaderStatic().loadSpecification(typeOf);
        }
        return typeOfSpec;
    }

    @Override
    public void setObject(List<ObjectAdapter> list) {
        super.setObject(list);
        type.setObject(this, list);
    }
    
    /**
     * Not API, but to refresh the model list.
     */
    public void setObjectList(ObjectAdapter resultAdapter) {
        final Iterable<Object> pojos = EntityCollectionModel.asIterable(resultAdapter);
        this.mementoList = Lists.newArrayList(
                Iterables.transform(pojos, ObjectAdapterMemento.Functions.fromPojo(getAdapterManagerStatic())));
    }

    /**
     * Populated only if {@link Type#PARENTED}.
     */
    public ObjectAdapterMemento getParentObjectAdapterMemento() {
        return parentObjectAdapterMemento;
    }

    /**
     * Populated only if {@link Type#PARENTED}.
     */
    public CollectionMemento getCollectionMemento() {
        return collectionMemento;
    }

    @SuppressWarnings("unchecked")
    private static Iterable<Object> asIterable(final ObjectAdapter resultAdapter) {
        return (Iterable<Object>) resultAdapter.getObject();
    }

    
    public void toggleSelectionOn(ObjectAdapter selectedAdapter) {
        ObjectAdapterMemento selectedAsMemento = ObjectAdapterMemento.createOrNull(selectedAdapter);
        
        // try to remove; if couldn't, then mustn't have been in there, in which case add.
        boolean removed = toggledMementosList.remove(selectedAsMemento);
        if(!removed) {
            toggledMementosList.add(selectedAsMemento);
        }
    }
    
    public List<ObjectAdapterMemento> getToggleMementosList() {
        return Collections.unmodifiableList(this.toggledMementosList);
    }

    public void clearToggleMementosList() {
        this.toggledMementosList.clear();
    }

    public void addEntityActions(List<LinkAndLabel> entityActions) {
        this.entityActions.addAll(entityActions);
    }

    @Override
    public List<LinkAndLabel> getLinks() {
        return Collections.unmodifiableList(entityActions);
    }

    public EntityCollectionModel asDummy() {
        return new EntityCollectionModel(typeOf, Collections.<ObjectAdapterMemento>emptyList(), pageSize);
    }
    
    // //////////////////////////////////////

    private static AdapterManager getAdapterManagerStatic() {
        return getPersistenceSessionStatic().getAdapterManager();
    }

    private static ServicesInjectorSpi getServicesInjector() {
        return getPersistenceSessionStatic().getServicesInjector();
    }

    private static PersistenceSession getPersistenceSessionStatic() {
        return IsisContext.getPersistenceSession();
    }

    private static SpecificationLoaderSpi getSpecificationLoaderStatic() {
        return IsisContext.getSpecificationLoader();
    }

}
