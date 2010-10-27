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

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.viewer.wicket.model.common.SelectionHandler;
import org.apache.isis.viewer.wicket.model.mementos.CollectionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel.Type;
import org.apache.isis.viewer.wicket.model.util.ClassLoaders;
import org.apache.isis.viewer.wicket.model.util.Mementos;
import org.apache.isis.viewer.wicket.model.util.ObjectAdapters;
import org.apache.wicket.Component;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Model representing a collection of entities, either {@link Type#STANDALONE
 * standalone} (eg result of invoking an action) or {@link Type#PARENTED
 * parented} (contents of the collection of an entity).
 * 
 * <p>
 * So that the model is {@link Serializable}, the {@link ObjectAdapter}s within the 
 * collection are stored as {@link ObjectAdapterMemento}s.
 */
public class EntityCollectionModel extends ModelAbstract<List<ObjectAdapter>> {

	private static final long serialVersionUID = 1L;

	public enum Type {
		/**
		 * A simple list of object mementos, eg the result of invoking an action
		 * 
		 * <p>
		 * This deals with both persisted and transient objects.
		 */
		STANDALONE {
			@Override
			List<ObjectAdapter> load(
					EntityCollectionModel entityCollectionModel) {
				return Lists.transform(entityCollectionModel.mementoList,
						ObjectAdapters.fromMemento());
			}
		},
		/**
		 * A collection of an entity (eg Order/OrderDetail).
		 */
		PARENTED {
			@Override
			List<ObjectAdapter> load(
					EntityCollectionModel entityCollectionModel) {
				ObjectAdapter adapter = entityCollectionModel.parentObjectAdapterMemento
						.getObjectAdapter();
				OneToManyAssociation collection = entityCollectionModel.collectionMemento
						.getCollection();
				ObjectAdapter collectionAsAdapter = collection.get(adapter);

				Iterable<Object> objectList = asIterable(collectionAsAdapter);

				Iterable<ObjectAdapter> adapterIterable = Iterables.transform(
						objectList, ObjectAdapters.fromPojo());
				List<ObjectAdapter> adapterList = Lists
						.newArrayList(adapterIterable);

				return adapterList;
			}

			@SuppressWarnings("unchecked")
			private Iterable<Object> asIterable(ObjectAdapter collectionAsAdapter) {
				return (Iterable<Object>) collectionAsAdapter.getObject();
			}
		};

		abstract List<ObjectAdapter> load(
				EntityCollectionModel entityCollectionModel);
	}

	/**
	 * Factory.
	 */
	public static EntityCollectionModel createStandalone(
			ObjectAdapter collectionAsAdapter) {
		Iterable<Object> iterable = EntityCollectionModel
				.asIterable(collectionAsAdapter);
		TypeOfFacet typeOfFacet = collectionAsAdapter.getTypeOfFacet();

		Class<?> cls = typeOfFacet.value();
		Iterable<ObjectAdapterMemento> oidIterable = Iterables.transform(
				iterable, Mementos.fromPojo());
		List<ObjectAdapterMemento> mementoList = Lists.newArrayList(oidIterable);
		return new EntityCollectionModel(cls, mementoList);
	}

	/**
	 * Factory.
	 */
	public static EntityCollectionModel createParented(EntityModel model,
			OneToManyAssociation collection) {
		return new EntityCollectionModel(model, collection);
	}

	/**
	 * Factory.
	 */
	public static EntityCollectionModel createParented(ObjectAdapter adapter,
			OneToManyAssociation collection) {
		return new EntityCollectionModel(adapter, collection);
	}

	private final Type type;

	private Class<?> typeOf;
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

	private EntityCollectionModel(Class<?> typeOf,
			List<ObjectAdapterMemento> mementoList) {
		this.type = Type.STANDALONE;
		this.typeOf = typeOf;
		this.mementoList = mementoList;
	}

	private EntityCollectionModel(ObjectAdapter adapter,
			OneToManyAssociation collection) {
		this(ObjectAdapterMemento.createOrNull(adapter), collection);
	}

	private EntityCollectionModel(EntityModel model,
			OneToManyAssociation collection) {
		this(model.getObjectAdapterMemento(), collection);
	}

	private EntityCollectionModel(ObjectAdapterMemento parentAdapterMemento,
			OneToManyAssociation collection) {
		this.type = Type.PARENTED;
		this.typeOf = ClassLoaders.forName(collection.getSpecification());
		this.parentObjectAdapterMemento = parentAdapterMemento;
		this.collectionMemento = new CollectionMemento(collection);
	}

    public boolean isParented() {
        return type == Type.PARENTED;
    }

    public boolean isStandalone() {
        return type == Type.STANDALONE;
    }

    /**
     * The name of the collection (if has an entity, ie, if {@link #isParented() is parented}.)
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
			typeOfSpec = IsisContext.getSpecificationLoader()
					.loadSpecification(typeOf);
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

	public void setSelectionHandler(SelectionHandler selectionHandler) {
		this.selectionHandler = selectionHandler;
	}

	public boolean hasSelectionHandler() {
		return getSelectionHandler() != null;
	}



	@SuppressWarnings("unchecked")
	public static Iterable<Object> asIterable(ObjectAdapter resultAdapter) {
		return (Iterable<Object>) resultAdapter.getObject();
	}
}
