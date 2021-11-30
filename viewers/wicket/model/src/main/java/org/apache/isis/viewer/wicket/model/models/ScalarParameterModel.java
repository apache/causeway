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
import org.apache.isis.core.metamodel.interactions.managed.ManagedValue;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.models.interaction.act.ParameterUiModelWkt;

import lombok.NonNull;

public class ScalarParameterModel
extends ScalarModel
implements ParameterUiModel {

    private static final long serialVersionUID = 1L;

    public static ScalarParameterModel wrap(final ParameterUiModelWkt delegate) {
        return new ScalarParameterModel(delegate);
    }

    private final ParameterUiModelWkt delegate;

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    private ScalarParameterModel(
            final ParameterUiModelWkt delegate) {
        super(EntityModel.ofAdapter(delegate.getCommonContext(), delegate.getOwner()));
        this.delegate = delegate;
    }

    @Override
    public ObjectActionParameter getMetaModel() {
        return delegate.getMetaModel();
    }

    @Override
    public ObjectSpecification getScalarTypeSpec() {
        return getMetaModel().getElementType();
    }

    @Override
    public String getIdentifier() {
        return "" + getParameterIndex();
    }

    @Override
    public String getCssClass() {
        return getMetaModel().getCssClass("isis-");
    }

    @Override
    public String disableReasonIfAny() {
        return getParameterNegotiationModel().getUsabilityConsent(getParameterIndex()).getReason();
    }

    @Override
    public boolean whetherHidden() {
        return getParameterNegotiationModel().getVisibilityConsent(getParameterIndex()).isVetoed();
    }

    @Override
    public String validate(final ManagedObject proposedValue) {
        return getParameterNegotiationModel().getObservableParamValidation(getParameterIndex()).getValue();
    }

    @Override
    public String toStringOf() {
        return getFriendlyName() + ": " + getParameterIndex();
    }

    @Override
    protected Can<ObjectAction> calcAssociatedActions() {
        return getScalarTypeSpec().streamActions(ActionScope.ANY, MixedIn.INCLUDED)
                .collect(Can.toCan());
    }

    @Override
    public @NonNull ManagedObject getValue() {
        return getObject();
    }

    @Override
    public void setValue(final ManagedObject paramValue) {
        setObject(paramValue);
    }

    @Override
    public ManagedValue proposedValue() {
        return getParameterNegotiationModel().getParamModels().getElseFail(getParameterIndex());
    }

    @Override
    public ParameterNegotiationModel getParameterNegotiationModel() {
        return delegate.getParameterNegotiationModel();
    }

    // -- HELPER

    @Override
    public IsisAppCommonContext getCommonContext() {
        return delegate.getCommonContext();
    }


}
