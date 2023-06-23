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
package org.apache.causeway.viewer.wicket.ui.components.widgets.select2;

import org.apache.wicket.model.IModel;
import org.wicketstuff.select2.Select2Choice;

import org.apache.causeway.applib.id.HasLogicalType;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.ui.components.widgets.select2.providers.ChoiceProviderAbstract;

import lombok.Getter;

public class Select2ChoiceExt
extends Select2Choice<ObjectMemento>
implements HasLogicalType {

    private static final long serialVersionUID = 1L;

    public static Select2ChoiceExt create(
            final String id,
            final IModel<ObjectMemento> modelObject,
            final PopModel popModel,
            final ChoiceProviderAbstract choiceProvider) {
        return new Select2ChoiceExt(id, modelObject, popModel, choiceProvider);
    }

    @Getter(onMethod_ = {@Override}) private final LogicalType logicalType;

    private Select2ChoiceExt(
            final String id,
            final IModel<ObjectMemento> model,
            final PopModel popModel,
            final ChoiceProviderAbstract choiceProvider) {
        super(id, model, choiceProvider);

        logicalType = popModel.getScalarTypeSpec().getLogicalType();

        getSettings().setCloseOnSelect(true);
        getSettings().setWidth("auto");
        getSettings().setDropdownAutoWidth(true);

        setOutputMarkupPlaceholderTag(true);
    }

}
