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

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ComponentFactoryScalarAbstract;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;

public class ChoicesSelect2PanelFactory
extends ComponentFactoryScalarAbstract {

    private static enum ComponentSort {
        TITLE_BADGE,
        VALUE_CHOICES,
        OBJECT_CHOICES;
        static ComponentSort valueOf(final UiAttributeWkt attributeModel) {
            if(attributeModel.getElementType().isValue()
                    && attributeModel.getChoiceProviderSort().isChoicesAny()) {
                return attributeModel.isViewingMode()
                    ? TITLE_BADGE
                    : VALUE_CHOICES;
            }
            return OBJECT_CHOICES;
        }
    }

    public ChoicesSelect2PanelFactory() {
        super(ScalarPanelAbstract.class);
    }

    @Override
    protected ScalarPanelAbstract createComponent(final String id, final UiAttributeWkt attributeModel) {
        var componentSort = ComponentSort.valueOf(attributeModel);
        switch(componentSort) {
        case TITLE_BADGE:
            var valueType = attributeModel.getElementType().getCorrespondingClass();
            return new ScalarTitleBadgePanel<>(id, attributeModel, valueType);
        case VALUE_CHOICES:
            return new ValueChoicesSelect2Panel(id, attributeModel);
        case OBJECT_CHOICES:
            return new ObjectChoicesSelect2Panel(id, attributeModel);
        default:
            throw _Exceptions.unmatchedCase(componentSort);
        }
    }

    @Override
    protected ApplicationAdvice appliesTo(final UiAttributeWkt attributeModel) {
        return ApplicationAdvice.APPLIES; //XXX depends on registration order, can we do better?
    }

}
