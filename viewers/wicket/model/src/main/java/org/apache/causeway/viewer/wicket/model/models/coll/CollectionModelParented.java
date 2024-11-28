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
package org.apache.causeway.viewer.wicket.model.models.coll;

import org.apache.wicket.Component;

import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;

import lombok.Getter;
import lombok.NonNull;

public final class CollectionModelParented
extends CollectionModelAbstract
implements
    UiHintContainer {

    private static final long serialVersionUID = 1L;

    // TODO parent object model, maybe should not be exposed
    // maybe could be resolved in the process of decoupling the ActionModel from Wicket
    @Getter private final @NonNull UiObjectWkt objectModel;
    @Getter private final @NonNull CollectionLayoutData layoutData;

    // -- FACTORIES

    public static CollectionModelParented forParentObjectModel(
            final @NonNull UiObjectWkt objectModel, final @NonNull CollectionLayoutData layoutData) {

        var coll = objectModel
                        .getTypeOfSpecification()
                        .getCollectionElseFail(layoutData.getId()); // collection's member-id

        return new CollectionModelParented(
                DataTableHolderFactory.forCollection(objectModel.bookmarkedObjectModel(), coll),
                objectModel,
                layoutData);
    }

    // -- CONSTRUCTOR

    protected CollectionModelParented(
            final @NonNull DataTableHolderFactory factory,
            final @NonNull UiObjectWkt parentObjectModel,  //TODO maybe instead use the delegate (?)
            final @NonNull CollectionLayoutData layoutData) {
        super(factory, Variant.PARENTED);
        this.objectModel = parentObjectModel;
        this.layoutData = layoutData;
    }

    // -- UI HINT CONTAINER

    public static final String HINT_KEY_SELECTED_ITEM = "selectedItem";

    @Override
    public String getHint(final Component component, final String attributeName) {
        return objectModel.getHint(component, attributeName);
    }

    @Override
    public void setHint(final Component component, final String attributeName, final String attributeValue) {
        objectModel.setHint(component, attributeName, attributeValue);
    }

    @Override
    public void clearHint(final Component component, final String attributeName) {
        objectModel.clearHint(component, attributeName);
    }

    @Override
    public OneToManyAssociation getMetaModel() {
        return (OneToManyAssociation) super.getMetaModel();
    }

    public Bookmark asHintingBookmark() {
        return objectModel.getOwnerBookmark();
    }

}
