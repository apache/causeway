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

import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.interactions.managed.PropertyNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.common.model.HasParentUiModel;
import org.apache.isis.viewer.common.model.feature.PropertyUiModel;
import org.apache.isis.viewer.wicket.model.models.interaction.InteractionHolderAbstract;
import org.apache.isis.viewer.wicket.model.models.interaction.ObjectUiModelWkt;

/**
 * <i>Object Property</i> model bound to its container {@link PropertyInteractionModelWkt}.
 * @see PropertyInteractionModelWkt
 */
public final class PropertyUiModelWkt
extends InteractionHolderAbstract<PropertyInteraction, PropertyInteractionModelWkt>
implements
    HasParentUiModel<ObjectUiModelWkt>,
    PropertyUiModel {

    private static final long serialVersionUID = 1L;

    PropertyUiModelWkt(
            final PropertyInteractionModelWkt model) {
        super(model);
    }

    public final PropertyInteraction propertyInteraction() {
        return getObject();
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
    public OneToOneAssociation getMetaModel() {
        return propertyInteraction().getManagedProperty().get().getMetaModel();
    }

    @Override
    public PropertyNegotiationModel getPendingPropertyModel() {
        return containerModel().propertyNegotiationModel().get();
    }

}
