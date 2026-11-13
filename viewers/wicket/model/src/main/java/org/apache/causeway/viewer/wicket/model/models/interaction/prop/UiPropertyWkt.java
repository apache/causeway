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
package org.apache.causeway.viewer.wicket.model.models.interaction.prop;

import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.commons.model.attrib.UiProperty;
import org.apache.causeway.viewer.commons.model.object.HasUiParentObject;
import org.apache.causeway.viewer.commons.model.object.UiObject;

/**
 * <i>Property Interaction</i> model bound to its owner {@link PropertyInteractionWkt}.
 *
 * @see PropertyInteractionWkt
 * @see ChainingModel
 */
public record UiPropertyWkt(
    PropertyInteractionWkt delegate)
implements
    IModel<PropertyInteraction>,
    HasMetaModelContext,
    HasUiParentObject<UiObject>,
    UiProperty {

    public PropertyInteraction propertyInteraction() {
        return delegate.getObject();
    }

    public PropertyInteractionWkt propertyInteractionModel() {
        return delegate;
    }

    @Override
    public UiObject getParentUiModel() {
        return ()->getOwner();
    }

    @Override
    public ManagedObject getOwner() {
        return propertyInteraction().getManagedProperty().get().getOwner();
    }

    @Override
    public OneToOneAssociation getMetaModel() {
        return propertyInteraction().getManagedProperty().get().getMetaModel();
    }

    @Override
    public PropertyNegotiationModel getPendingPropertyModel() {
        return propertyInteractionModel().propertyNegotiationModel();
    }

    @Override
    public void detach() {
        delegate.detach();
    }

    @Override
    public void setObject(final PropertyInteraction object) {
        delegate.setObject(object);
    }

    @Override
    public PropertyInteraction getObject() {
        return propertyInteraction();
    }

}
