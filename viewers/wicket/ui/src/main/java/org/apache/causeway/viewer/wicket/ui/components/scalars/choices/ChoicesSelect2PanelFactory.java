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
import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ComponentFactoryScalarAbstract;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.causeway.viewer.wicket.ui.components.scalars.string.ScalarTitleBadgePanel;

import lombok.val;

public class ChoicesSelect2PanelFactory
extends ComponentFactoryScalarAbstract {

    private static final long serialVersionUID = 1L;

    private static enum ComponentSort {
        TITLE_BADGE,
        VALUE_CHOICES,
        OBJECT_CHOICES;
        static ComponentSort valueOf(final PopModel popModel) {
            if(popModel.getScalarTypeSpec().isValue()
                    && popModel.getChoiceProviderSort().isChoicesAny()) {
                return popModel.isViewingMode()
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
    protected ScalarPanelAbstract createComponent(final String id, final PopModel popModel) {
        val componentSort = ComponentSort.valueOf(popModel);
        switch(componentSort) {
        case TITLE_BADGE:
            val valueType = popModel.getScalarTypeSpec().getCorrespondingClass();
            return new ScalarTitleBadgePanel<>(id, popModel, valueType);
        case VALUE_CHOICES:
            return new ValueChoicesSelect2Panel(id, popModel);
        case OBJECT_CHOICES:
            return new ObjectChoicesSelect2Panel(id, popModel);
        default:
            throw _Exceptions.unmatchedCase(componentSort);
        }
    }

    @Override
    protected ApplicationAdvice appliesTo(final PopModel popModel) {
        return ApplicationAdvice.APPLIES; //XXX depends on registration order, can we do better?
    }

}
