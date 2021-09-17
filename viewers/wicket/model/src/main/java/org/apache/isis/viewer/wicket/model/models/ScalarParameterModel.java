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

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedValue;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.memento.ActionParameterMemento;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class ScalarParameterModel
extends ScalarModel
implements ParameterUiModel {

    private static final long serialVersionUID = 1L;

    private final ActionParameterMemento paramMemento;

    @Getter(onMethod_ = {@Override})
    @Setter(onMethod_ = {@Override})
    private transient ParameterNegotiationModel pendingParameterModel;

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    public ScalarParameterModel(
            final EntityModel parentEntityModel,
            final ActionParameterMemento paramMemento) {
        super(parentEntityModel, paramMemento);
        this.paramMemento = paramMemento;
    }

    private transient ObjectActionParameter actionParameter;

    @Override
    public ObjectActionParameter getMetaModel() {
        if(actionParameter==null) {
            actionParameter = paramMemento.getActionParameter(this::getSpecificationLoader);
        }
        return actionParameter;
    }

    private transient ManagedAction managedAction;

    public ManagedAction getManagedAction() {
        if(managedAction==null) {
            val actionOwner = getParentUiModel().load();
            // TODO 'where' is not used until whetherDisabled and whetherHidden are implemented
            managedAction = ManagedAction.of(actionOwner, getMetaModel().getAction(), Where.ANYWHERE);
        }
        return managedAction;
    }

    @Override
    public ObjectSpecification getScalarTypeSpec() {
        return getMetaModel().getSpecification();
    }

    @Override
    public String getIdentifier() {
        return "" + getNumber();
    }

    @Override
    public String getCssClass() {
        return getMetaModel().getCssClass("isis-");
    }

    @Override
    public String whetherDisabled() {
        // always enabled TODO this is not true
        return null;
    }

    @Override
    public boolean whetherHidden() {
        // always enabled TODO this is not true
        return false;
    }

    @Override
    public String validate(final ManagedObject proposedValue) {
        final ObjectActionParameter parameter = getMetaModel();

        val action = parameter.getAction();
        try {
            ManagedObject parentAdapter = getParentUiModel().load();

            val head = action.interactionHead(parentAdapter);

            final String invalidReasonIfAny = parameter
                    .isValid(head, proposedValue, InteractionInitiatedBy.USER);
            return invalidReasonIfAny;
        } catch (final Exception ex) {
            return ex.getLocalizedMessage();
        }
    }

    @Override
    public ManagedObject load() {
        return toNonNull(loadFromSuper());
    }

    @Override
    public String toStringOf() {
        return getFriendlyName() + ": " + paramMemento.toString();
    }

    @Override
    protected Can<ObjectAction> calcAssociatedActions() {
        return Can.empty();
    }

    @Override
    public ManagedObject getValue() {
        return toNonNull(getObject());
    }

    @Override
    public void setValue(final ManagedObject paramValue) {
        super.setObject(paramValue);
    }

    @Override
    public ManagedValue proposedValue() {
        return getPendingParameterModel().getParamModels().getElseFail(paramMemento.getNumber());
    }

    // -- HELPER

    private ManagedObject toNonNull(@Nullable ManagedObject adapter) {
        if(adapter == null) {
            adapter = ManagedObject.empty(getMetaModel().getSpecification());
        }
        return ManagedObjects.emptyToDefault(!getMetaModel().isOptional(), adapter);
    }



}
