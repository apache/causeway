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
package org.apache.causeway.viewer.wicket.ui.components.scalars.choices;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.causeway.viewer.wicket.ui.components.scalars.string.ScalarTitleBadgePanel;

import lombok.val;

public class ChoicesSelect2PanelFactory extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    private static enum ComponentSort {
        TITLE_BADGE,
        VALUE_CHOICES,
        OBJECT_CHOICES;
        static ComponentSort valueOf(final ScalarModel scalarModel) {
            if(scalarModel.getScalarTypeSpec().isValue()
                    && scalarModel.getChoiceProviderSort().isChoicesAny()) {
                return scalarModel.isViewMode()
                    ? TITLE_BADGE
                    : VALUE_CHOICES;
            }
            return OBJECT_CHOICES;
        }
    }

    public ChoicesSelect2PanelFactory() {
        super(UiComponentType.SCALAR_NAME_AND_VALUE);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        return appliesIf(_Casts.castTo(ScalarModel.class, model)
                .isPresent());
    }

    @Override
    public final Component createComponent(final String id, final IModel<?> model) {
        val scalarModel = (ScalarModel) model;
        val componentSort = ComponentSort.valueOf(scalarModel);
        switch(componentSort) {
        case TITLE_BADGE:
            val valueType = scalarModel.getScalarTypeSpec().getCorrespondingClass();
            return new ScalarTitleBadgePanel<>(id, scalarModel, valueType);
        case VALUE_CHOICES:
            return new ValueChoicesSelect2Panel(id, scalarModel);
        case OBJECT_CHOICES:
            return new ObjectChoicesSelect2Panel(id, scalarModel);
        default:
            throw _Exceptions.unmatchedCase(componentSort);
        }
    }

}
