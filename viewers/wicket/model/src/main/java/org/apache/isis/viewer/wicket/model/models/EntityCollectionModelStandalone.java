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

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.runtime.memento.ObjectMemento;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class EntityCollectionModelStandalone
extends EntityCollectionModelAbstract {

    private static final long serialVersionUID = 1L;

    private final @NonNull _EntityCollectionModelLegacy legacy;

    // parent object model
    @Getter
    private final @NonNull ActionModel actionModel;

    // -- FACTORIES

    public static EntityCollectionModelStandalone forActionModel(
            final @NonNull ManagedObject collectionAsAdapter,
            final @NonNull ActionModel actionModel) {

        val typeOfSpecification = actionModel.getMetaModel().getReturnType().getElementSpecification()
                .orElseThrow(()->_Exceptions
                        .illegalArgument("ActionModel must have an ElementSpecification for its return type"));

        final Can<FacetHolder> facetHolders = Can.of(actionModel.getMetaModel(), typeOfSpecification);

        // take a copy of the actionModel,
        // because the original can get mutated (specifically: its arguments cleared)
        return new EntityCollectionModelStandalone(
                typeOfSpecification, collectionAsAdapter, actionModel.copy(), facetHolders);
    }

    // -- CONSTRUCTOR

    protected EntityCollectionModelStandalone(
            final @NonNull ObjectSpecification typeOfSpecification,
            final @NonNull ManagedObject collectionAsAdapter,
            final @NonNull ActionModel actionModel,
            final @NonNull Can<FacetHolder> facetHolders) {
        super(
                actionModel.getCommonContext(),
                actionModel.getMetaModel().getIdentifier(),
                typeOfSpecification,
                facetHolders);
        this.actionModel = actionModel;
        this.legacy = _EntityCollectionModelLegacy.createStandalone(collectionAsAdapter, actionModel);
    }

    // -- VARIANT SUPPORT

    @Override
    public Variant getVariant() {
        return Variant.STANDALONE;
    }

    // -- INTERACTION SUPPORT

    @Override
    public Optional<ManagedCollection> getManagedCollection() {
        return Optional.empty();
    }

    // --

    @Override
    protected List<ManagedObject> load() {
        return legacy.load();
    }

    @Override
    public int getCount() {
        return legacy.getCount();
    }

    @Override
    public String getName() {
        return getTypeOfSpecification().lookupFacet(PluralFacet.class)
                .map(PluralFacet::value)
                .orElse(getMetaModel().getName());
    }

    @Override
    public ObjectMember getMetaModel() {
        return actionModel.getMetaModel();
    }

    @Override
    public ObjectMemento getParentObjectAdapterMemento() {
        return null;
    }

    @Override
    public Bookmark asHintingBookmarkIfSupported() {
        return null;
    }


}
