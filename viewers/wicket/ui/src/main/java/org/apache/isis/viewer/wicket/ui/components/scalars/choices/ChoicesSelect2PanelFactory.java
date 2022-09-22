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
package org.apache.isis.viewer.wicket.ui.components.scalars.choices;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.viewer.commons.model.components.UiComponentType;
import org.apache.isis.viewer.commons.model.scalar.UiScalar.ChoiceProviderSort;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.string.ScalarTitleBadgePanel;

import lombok.val;

public class ChoicesSelect2PanelFactory extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public ChoicesSelect2PanelFactory() {
        super(UiComponentType.SCALAR_NAME_AND_VALUE);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        return appliesIf(_Casts.castTo(ScalarModel.class, model)
                .map(ScalarModel::getChoiceProviderSort)
                .map(ChoiceProviderSort::isChoicesAny)
                .orElse(false));
    }

    @Override
    public final Component createComponent(final String id, final IModel<?> model) {
        val scalarModel = (ScalarModel) model;

        if(scalarModel.getScalarTypeSpec().isValue()) {

            if(scalarModel.isViewMode()) {
                val valueType = scalarModel.getScalarTypeSpec().getCorrespondingClass();
                return new ScalarTitleBadgePanel<>(id, scalarModel, valueType);
            } else {
                return new ValueChoicesSelect2Panel(id, scalarModel);
            }

        } else {
            return new ObjectChoicesSelect2Panel(id, scalarModel);
        }
    }

}
