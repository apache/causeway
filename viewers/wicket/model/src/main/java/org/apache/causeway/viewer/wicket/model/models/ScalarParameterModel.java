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
import org.apache.causeway.core.metamodel.interactions.managed.ManagedValue;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.commons.model.scalar.HasUiParameter;
import org.apache.causeway.viewer.wicket.model.models.interaction.act.UiParameterWkt;

import lombok.Getter;
import lombok.NonNull;

public class ScalarParameterModel
extends ScalarModel
implements HasUiParameter {

    private static final long serialVersionUID = 1L;

    public static ScalarParameterModel wrap(final UiParameterWkt delegate) {
        return new ScalarParameterModel(delegate);
    }

    @Getter(onMethod_={@Override})
    private final UiParameterWkt uiParameter;

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    private ScalarParameterModel(
            final UiParameterWkt uiParameter) {
        super(UiObjectWkt.ofAdapter(uiParameter.getMetaModelContext(), uiParameter.getOwner()));
        this.uiParameter = uiParameter;
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

}
