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
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.wicket.Component;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.common.SelectionHandler;
import org.apache.isis.viewer.wicket.model.mementos.CollectionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.util.ClassLoaders;
import org.apache.isis.viewer.wicket.model.util.Mementos;
import org.apache.isis.viewer.wicket.model.util.ObjectAdapters;

/**
 * Model representing a collection of entities, either {@link Type#STANDALONE
 * standalone} (eg result of invoking an action) or {@link Type#PARENTED
 * parented} (contents of the collection of an entity).
 * 
 * <p>
 * So that the model is {@link Serializable}, the {@link ObjectAdapter}s within
 * the collection are stored as {@link ObjectAdapterMemento}s.
 */
public class EntityCollectionModel extends ModelAbstract<List<ObjectAdapter>> {

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
                return Lists.transform(entityCollectionModel.mementoList, ObjectAdapters.fromMemento());
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

                final Iterable<Object> objectList = asIterable(collectionAsAdapter);

                final Iterable<ObjectAdapter> adapterIterable = Iterables.transform(objectList, ObjectAdapters.fromPojo());
                final List<ObjectAdapter> adapterList = Lists.newArrayList(adapterIterable);

                return adapterList;
            }

            @SuppressWarnings("unchecked")
            private Iterable<Object> asIterable(final ObjectAdapter collectionAsAdapter) {
                return (Iterable<Object>) collectionAsAdapter.getObject();
            }
        };

        abstract List<ObjectAdapter> load(EntityCollectionModel entityCollectionModel);
    }

    /**
     * Factory.
     */
    public static EntityCollectionModel createStandalone(final ObjectAdapter collectionAsAdapter) {
        final Iterable<Object> iterable = EntityCollectionModel.asIterable(collectionAsAdapter);

        final Iterable<ObjectAdapterMemento> oidIterable = Iterables.transform(iterable, Mementos.fromPojo());
        final List<ObjectAdapterMemento> mementoList = Lists.newArrayList(oidIterable);

        final ObjectSpecification elementSpec = collectionAsAdapter.getElementSpecification();
        final Class<?> elementType = elementSpec.getCorrespondingClass();
        int pageSize = pageSize(elementSpec.getFacet(PagedFacet.class), PAGE_SIZE_DEFAULT_FOR_STANDALONE);
        
        return new EntityCollectionModel(elementType, mementoList, pageSize);
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
     * Populated only if {@link Type#PARENTED}.
     */
    private ObjectAdapterMemento parentObjectAdapterMemento;

    /**
     * Populated only if {@link Type#PARENTED}.
     */
    private CollectionMemento collectionMemento;

    private SelectionHandler selectionHandler;

    private final int pageSize;

    private EntityCollectionModel(final Class<?> typeOf, final List<ObjectAdapterMemento> mementoList, final int pageSize) {
        this.type = Type.STANDALONE;
        this.typeOf = typeOf;
        this.mementoList = mementoList;
        this.pageSize = pageSize;
    }

    private EntityCollectionModel(final ObjectAdapter adapter, final OneToManyAssociation collection) {
        this(ObjectAdapterMemento.createOrNull(adapter), collection);
    }

    private EntityCollectionModel(final EntityModel model, final OneToManyAssociation collection) {
        this(model.getObjectAdapterMemento(), collection);
    }

    private EntityCollectionModel(final ObjectAdapterMemento parentAdapterMemento, final OneToManyAssociation collection) {
        this.type = Type.PARENTED;
        this.typeOf = ClassLoaders.forName(collection.getSpecification());
        this.parentObjectAdapterMemento = parentAdapterMemento;
        this.collectionMemento = new CollectionMemento(collection);
        this.pageSize = pageSize(collection.getFacet(PagedFacet.class), PAGE_SIZE_DEFAULT_FOR_PARENTED);
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
     * Will returns <tt>null</tt> otherwise.
     */
    public String getName() {
        return getCollectionMemento().getName();
    }

    @Override
    protected List<ObjectAdapter> load() {
        return type.load(this);
    }

    public ObjectSpecification getTypeOfSpecification() {
        if (typeOfSpec == null) {
            typeOfSpec = IsisContext.getSpecificationLoader().loadSpecification(typeOf);
        }
        return typeOfSpec;
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

    /**
     * The {@link SelectionHandler}, if any.
     * 
     * <p>
     * If specified, then view {@link Component}s are expected to render the
     * collection so that one of the entities can be selected.
     */
    public SelectionHandler getSelectionHandler() {
        return selectionHandler;
    }

    public void setSelectionHandler(final SelectionHandler selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    public boolean hasSelectionHandler() {
        return getSelectionHandler() != null;
    }

    @SuppressWarnings("unchecked")
    public static Iterable<Object> asIterable(final ObjectAdapter resultAdapter) {
        return (Iterable<Object>) resultAdapter.getObject();
    }
}
