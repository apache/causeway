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
package org.apache.causeway.viewer.wicket.model.models;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.commons.ScalarRepresentation;
import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedValue;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.commons.model.scalar.HasUiProperty;
import org.apache.causeway.viewer.wicket.model.models.interaction.prop.UiPropertyWkt;

import lombok.Getter;
import lombok.val;

public class ScalarPropertyModel
extends ScalarModel
implements HasUiProperty {

    private static final long serialVersionUID = 1L;

    @Getter(onMethod_={@Override})
    private UiPropertyWkt uiProperty;

    public static ScalarPropertyModel wrap(
            final UiPropertyWkt uiProperty,
            final ScalarRepresentation viewOrEdit,
            final RenderingHint renderingHint) {
        return new ScalarPropertyModel(uiProperty, viewOrEdit, renderingHint);
    }

    /**
     * Creates a model representing a property of a parent object, with the
     * {@link #getObject() value of this model} to be current value of the
     * property.
     */
    private ScalarPropertyModel(
            final UiPropertyWkt uiProperty,
            final ScalarRepresentation viewOrEdit,
            final RenderingHint renderingHint) {
        super(UiObjectWkt.ofAdapter(uiProperty.getMetaModelContext(), uiProperty.getOwner()),
                viewOrEdit, renderingHint);
        this.uiProperty = uiProperty;
    }

    /** @return new instance bound to the same delegate */
    public ScalarPropertyModel copyHaving(
            final ScalarRepresentation viewOrEdit,
            final RenderingHint renderingHint) {
        return wrap(uiProperty, viewOrEdit, renderingHint);
    }

    @Override
    public String validate(final ManagedObject proposedNewValue) {
        return getManagedProperty()
                .checkValidity(proposedNewValue)
                .map(InteractionVeto::getReason)
                .orElse(null);
    }

    @Override
    public String toStringOf() {
        val featureId = uiProperty.getMetaModel().getFeatureIdentifier();
        return getFriendlyName() + ": " +
                featureId.getLogicalTypeName() + "#" + featureId.getMemberLogicalName();

    }

    public String getReasonInvalidIfAny() {
        return getPendingPropertyModel().getValidationMessage().getValue();
    }

    /**
     * Apply changes to the underlying adapter (possibly returning a new adapter).
     *
     * @return adapter, which may be different from the original
     */
    public ManagedObject applyValueThenReturnOwner() {
        getPendingPropertyModel().submit();
        return getOwner();
    }

    @Override
    public ManagedValue proposedValue() {
        return getPendingPropertyModel();
    }

    @Override
    protected Can<ObjectAction> calcAssociatedActions() {
        return getManagedProperty().getAssociatedActions();
    }

}
