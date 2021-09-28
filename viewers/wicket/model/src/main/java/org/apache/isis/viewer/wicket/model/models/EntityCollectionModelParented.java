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

import java.util.Optional;

import org.apache.wicket.Component;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.models.interaction.coll.CollectionInteractionWkt;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class EntityCollectionModelParented
extends EntityCollectionModelAbstract
implements
    UiHintContainer {

    private static final long serialVersionUID = 1L;

    // TODO parent object model, maybe should not be exposed
    // maybe could be resolved in the process of decoupling the ActionModel from Wicket
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

        return new EntityCollectionModelParented(
                collectionMetaModel, entityModel);
    }

    // -- CONSTRUCTOR

    protected EntityCollectionModelParented(
            final @NonNull OneToManyAssociation coll,
            final @NonNull EntityModel parentObjectModel) { //TODO replace with BookmarkedObjectWkt
        super(CollectionInteractionWkt
                .bind(parentObjectModel.bookmarkedObjectModel(), coll, Where.NOT_SPECIFIED),
                Variant.PARENTED);
        this.entityModel = parentObjectModel;
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

//    @Override
//    protected List<ManagedObject> load() {
//
//        final ManagedObject collectionAsAdapter = getManagedCollection().getCollectionValue();
//
//FIXME[ISIS-2871] DataTableModel has no sorting
//        val elements = _NullSafe.streamAutodetect(collectionAsAdapter.getPojo())
//        .filter(_NullSafe::isPresent) // pojos
//        .map(getObjectManager()::adapt)
//        .sorted(super.getElementComparator())
//        .collect(Can.toCan());
//
//        this.count = elements.size();
//
//        return elements.toList();
//    }


    public ManagedCollection getManagedCollection() {
        return ManagedCollection
                .of(entityModel.getManagedObject(), getMetaModel(), Where.NOT_SPECIFIED);
    }

    public CollectionLayoutData getLayoutData() {
        return entityModel.getCollectionLayoutData();
    }

    public Bookmark asHintingBookmark() {
        return entityModel.getOwnerBookmark();
    }

    @Override
    public ManagedObject getParentObject() {
        return getManagedCollection().getOwner();
    }


}
