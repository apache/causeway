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

import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class EntityCollectionModelParented
extends EntityCollectionModelAbstract
implements
    UiHintContainer {

    private static final long serialVersionUID = 1L;

    private final @NonNull _EntityCollectionModelLegacy legacy;

    // TODO parent object model, should be private
    @Getter private final @NonNull EntityModel entityModel;

    // -- FACTORIES

    public static EntityCollectionModelParented forParentObjectModel(
            final @NonNull EntityModel entityModel) {

        val collectionMetaModel =
                Optional.ofNullable(entityModel.getCollectionLayoutData())
                .map(collectionLayoutData->
                    entityModel
                        .getTypeOfSpecification()
                        .getCollectionElseFail(collectionLayoutData.getId()))
                .orElseThrow(()->_Exceptions
                        .illegalArgument("EntityModel must have CollectionLayoutMetadata"));

        val typeOfFacet = collectionMetaModel.lookupFacet(TypeOfFacet.class)
                .orElseThrow(()->_Exceptions
                        .illegalArgument("CollectionMetaModel must have a TypeOfFacet"));

        val typeOfSpecification = typeOfFacet.valueSpec();

        final Can<FacetHolder> facetHolders = Can.of(collectionMetaModel, typeOfSpecification);

//      val sortedByFacet = collectionMetaModel.getFacet(SortedByFacet.class);
//
//        entityCollectionModel.collectionMemento = new CollectionMemento(collectionMetaModel);
//        entityCollectionModel.sortedBy = (sortedByFacet != null)
//                ? sortedByFacet.value()
//                : null;

        return new EntityCollectionModelParented(
                collectionMetaModel, typeOfSpecification, entityModel, facetHolders);
    }

    // -- CONSTRUCTOR

    protected EntityCollectionModelParented(
            final @NonNull OneToManyAssociation collectionMetaModel,
            final @NonNull ObjectSpecification typeOfSpecification,
            final @NonNull EntityModel parentObjectModel,
            final @NonNull Can<FacetHolder> facetHolders) {
        super(
                parentObjectModel.getCommonContext(),
                collectionMetaModel.getIdentifier(),
                typeOfSpecification,
                facetHolders);
        this.entityModel = parentObjectModel;
        this.legacy = _EntityCollectionModelLegacy.createParented(entityModel);
    }

    // -- VARIANT SUPPORT

    @Override
    public Variant getVariant() {
        return Variant.PARENTED;
    }

    // -- METAMODEL

    @Override
    public Can<ObjectAction> getAssociatedActions() {
        val managedCollection = getManagedCollection().orElse(null);
        if(managedCollection==null) {
            return Can.empty();
        }
        final OneToManyAssociation collection = managedCollection.getCollection();
        return managedCollection.getOwner().getSpecification()
                .streamRuntimeActions(MixedIn.INCLUDED)
                .filter(ObjectAction.Predicates.associatedWithAndWithCollectionParameterFor(collection))
                .collect(Can.toCan());
    }

    // -- UI HINT CONTAINER

    public static final String HINT_KEY_SELECTED_ITEM = "selectedItem";

    @Override
    public String getHint(final Component component, final String attributeName) {
        return getEntityModel().getHint(component, attributeName);
    }

    @Override
    public void setHint(final Component component, final String attributeName, final String attributeValue) {
        getEntityModel().setHint(component, attributeName, attributeValue);
    }

    @Override
    public void clearHint(final Component component, final String attributeName) {
        getEntityModel().clearHint(component, attributeName);
    }

    @Override
    protected List<ManagedObject> load() {
        return legacy.load();
    }

    @Override
    public Optional<ManagedCollection> getManagedCollection() {
        return Optional.of(ManagedCollection
                .of(entityModel.getManagedObject(), getMetaModel(), Where.NOT_SPECIFIED));
    }

    @Override
    public int getCount() {
        return legacy.getCount();
    }

    @Override
    public String getName() {
        _Assert.assertEquals(getMetaModel().getName(), legacy.getName());
        return getMetaModel().getName();
    }

    @Override
    public OneToManyAssociation getMetaModel() {
        return legacy.getCollectionMemento().getCollection(getSpecificationLoader());
    }

    public CollectionLayoutData getLayoutData() {
        return entityModel.getCollectionLayoutData();
    }

    @Override
    public ObjectMemento getParentObjectAdapterMemento() {
        return entityModel.memento();
    }

    @Override
    public Bookmark asHintingBookmarkIfSupported() {
        return entityModel.asHintingBookmarkIfSupported();
    }


}
