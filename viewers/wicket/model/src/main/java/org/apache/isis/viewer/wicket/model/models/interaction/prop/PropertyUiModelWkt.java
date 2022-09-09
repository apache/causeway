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
package org.apache.isis.viewer.wicket.model.models.interaction.prop;

import org.apache.wicket.model.ChainingModel;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.interactions.managed.PropertyNegotiationModel;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.commons.model.object.HasUiParentObject;
import org.apache.isis.viewer.commons.model.scalar.UiProperty;
import org.apache.isis.viewer.wicket.model.models.HasCommonContext;
import org.apache.isis.viewer.wicket.model.models.interaction.ObjectUiModelWkt;

/**
 * <i>Property Interaction</i> model bound to its owner {@link PropertyInteractionWkt}.
 *
 * @apiNote a single <i>Property Interaction</i> could in theory provide a compound of multiple
 * {@link UiProperty}(s).
 *
 * @see PropertyInteractionWkt
 * @see ChainingModel
 */
public final class PropertyUiModelWkt
extends ChainingModel<PropertyInteraction>
implements
    HasCommonContext,
    HasUiParentObject<ObjectUiModelWkt>,
    UiProperty {

    private static final long serialVersionUID = 1L;

    final int tupleIndex; //future extension

    PropertyUiModelWkt(
            final PropertyInteractionWkt model,
            final int tupleIndex) {
        super(model);
        this.tupleIndex = tupleIndex;
    }

    public final PropertyInteraction propertyInteraction() {
        return getObject();
    }

    public final PropertyInteractionWkt propertyInteractionModel() {
        return (PropertyInteractionWkt) getChainedModel();
    }

    @Override
    public final ObjectUiModelWkt getParentUiModel() {
        return ()->getOwner();
    }

    @Override
    public final ManagedObject getOwner() {
        return propertyInteraction().getManagedProperty().get().getOwner();
    }

    @Override
    public final OneToOneAssociation getMetaModel() {
        return propertyInteraction().getManagedProperty().get().getMetaModel();
    }

    @Override
    public final PropertyNegotiationModel getPendingPropertyModel() {
        return propertyInteractionModel().propertyNegotiationModel();
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return propertyInteractionModel().getMetaModelContext();
    }

}
