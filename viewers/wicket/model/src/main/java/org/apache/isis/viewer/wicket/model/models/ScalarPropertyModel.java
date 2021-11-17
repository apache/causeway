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

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.interactions.managed.ManagedValue;
import org.apache.isis.core.metamodel.interactions.managed.PropertyNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.feature.PropertyUiModel;
import org.apache.isis.viewer.wicket.model.models.interaction.prop.PropertyUiModelWkt;

import lombok.val;

public class ScalarPropertyModel
extends ScalarModel
implements PropertyUiModel {

    private static final long serialVersionUID = 1L;

    private PropertyUiModelWkt delegate;

    public static ScalarPropertyModel wrap(
            final PropertyUiModelWkt delegate,
            final ScalarRepresentation viewOrEdit,
            final EntityModel.RenderingHint renderingHint) {
        return new ScalarPropertyModel(delegate, viewOrEdit, renderingHint);
    }

    /**
     * Creates a model representing a property of a parent object, with the
     * {@link #getObject() value of this model} to be current value of the
     * property.
     */
    private ScalarPropertyModel(
            final PropertyUiModelWkt delegate,
            final ScalarRepresentation viewOrEdit,
            final EntityModel.RenderingHint renderingHint) {
        super(EntityModel.ofAdapter(delegate.getCommonContext(), delegate.getOwner()),
                viewOrEdit, renderingHint);
        this.delegate = delegate;
    }

    /** @return new instance bound to the same delegate */
    public ScalarPropertyModel copyHaving(
            final ScalarRepresentation viewOrEdit,
            final EntityModel.RenderingHint renderingHint) {
        return wrap(delegate, viewOrEdit, renderingHint);
    }

    @Override
    public OneToOneAssociation getMetaModel() {
        return delegate.getMetaModel();
    }

    public ManagedProperty getManagedProperty() {
        return delegate.propertyInteraction().getManagedProperty().get();
    }

    @Override
    public PropertyNegotiationModel getPendingPropertyModel() {
        return delegate.getPendingPropertyModel();
    }

    @Override
    public ObjectSpecification getScalarTypeSpec() {
        return getMetaModel().getElementType();
    }

    @Override
    public String getIdentifier() {
        return getMetaModel().getFeatureIdentifier().getMemberLogicalName();
    }

    @Override
    public String getCssClass() {
        return getMetaModel().getCssClass("isis-");
    }

    @Override
    public boolean whetherHidden() {
        return getManagedProperty()
                .checkVisibility()
                .isPresent();
    }

    @Override
    public String disableReasonIfAny() {
        return getManagedProperty()
                .checkUsability()
                .map(InteractionVeto::getReason)
                .orElse(null);
    }

    @Override
    public String validate(final ManagedObject proposedNewValue) {
        return getManagedProperty()
                .checkValidity(proposedNewValue)
                .map(InteractionVeto::getReason)
                .orElse(null);
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public String toStringOf() {
        val featureId = delegate.getMetaModel().getFeatureIdentifier();
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

    @Override
    public IsisAppCommonContext getCommonContext() {
        return delegate.getCommonContext();
    }

}
