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
import org.apache.isis.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.interactions.managed.ManagedValue;
import org.apache.isis.core.metamodel.interactions.managed.PropertyNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.spec.feature.memento.PropertyMemento;
import org.apache.isis.viewer.common.model.feature.PropertyUiModel;
import org.apache.isis.viewer.wicket.model.models.interaction.prop.PropertyUiModelWkt;

import lombok.val;

public class ScalarPropertyModel
extends ScalarModel
implements PropertyUiModel {

    private static final long serialVersionUID = 1L;

    @Deprecated
    private final PropertyMemento propertyMemento;

    private PropertyUiModelWkt delegate;

    public static ScalarPropertyModel wrap(
            final PropertyUiModelWkt delegate,
            final EntityModel.EitherViewOrEdit viewOrEdit,
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
            final EntityModel.EitherViewOrEdit viewOrEdit,
            final EntityModel.RenderingHint renderingHint) {
        super(EntityModel.ofAdapter(delegate.getCommonContext(), delegate.getOwner()),
                delegate.getMetaModel().getMemento(),
                viewOrEdit, renderingHint);
        this.delegate = delegate;
        this.propertyMemento = delegate.getMetaModel().getMemento();
        reset();
    }

    /** @return new instance bound to the same delegate */
    public ScalarPropertyModel copyHaving(
            final EntityModel.EitherViewOrEdit viewOrEdit,
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
        return getMetaModel().getSpecification();
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
    public String whetherDisabled() {
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

    public void reset() {
        //pendingPropertyModel = null; // invalidate
        val propertyValue = getManagedProperty().getPropertyValue();
        val presentedValue = ManagedObjects.isNullOrUnspecifiedOrEmpty(propertyValue)
                ? null
                : propertyValue;

        this.setObject(presentedValue);
    }

    @Override
    public ManagedObject load() {
        return loadFromSuper();
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public String toStringOf() {
        return getFriendlyName() + ": " + propertyMemento.toString();
    }

    public String getReasonInvalidIfAny() {
        return getPendingPropertyModel().getValidationMessage().getValue();
    }

    /**
     * Apply changes to the underlying adapter (possibly returning a new adapter).
     *
     * @return adapter, which may be different from the original
     */
    public ManagedObject applyValue() {
        val proposedNewValue = getObject();
        getManagedProperty().modifyProperty(proposedNewValue);
        return getManagedProperty().getOwner();
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
