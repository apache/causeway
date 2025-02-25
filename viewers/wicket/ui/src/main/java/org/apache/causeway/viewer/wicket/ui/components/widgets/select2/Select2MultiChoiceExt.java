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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.wicket.model.IModel;
import org.wicketstuff.select2.Select2MultiChoice;

import org.apache.causeway.applib.id.HasLogicalType;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;

import lombok.Getter;
import lombok.experimental.Accessors;

public class Select2MultiChoiceExt
extends Select2MultiChoice<ObjectMemento>
implements HasLogicalType {

    private static final long serialVersionUID = 1L;

    public static Select2MultiChoiceExt create(
            final String id,
            final IModel<ArrayList<ObjectMemento>> modelObject,
            final UiAttributeWkt attributeModel,
            final ChoiceProviderRecord choiceProvider) {

        return new Select2MultiChoiceExt(id, _Casts.uncheckedCast(modelObject), attributeModel, choiceProvider);
    }

    @Getter(onMethod_ = {@Override}) @Accessors(fluent=true) private final LogicalType logicalType;

    Select2MultiChoiceExt(
            final String id,
            final IModel<Collection<ObjectMemento>> model,
            final UiAttributeWkt attributeModel,
            final ChoiceProviderRecord choiceProvider) {

        super(id, model, choiceProvider.toSelect2ChoiceProvider());
        logicalType = attributeModel.getElementType().logicalType();

        getSettings().setCloseOnSelect(true);
        getSettings().setWidth("auto");
        getSettings().setDropdownAutoWidth(true);

        setOutputMarkupPlaceholderTag(true);
    }

    public ObjectMemento getPackedModelObject() {
        return ObjectMemento.packed(this.logicalType(), this.getModelObject());
    }

    public ObjectMemento getPackedConvertedInput() {
        return ObjectMemento.packed(this.logicalType(), this.getConvertedInput());
    }

    // -- bug in wicket 8.8.0 -------------------------------------------

    private boolean workaround;

    @Override
    public void updateModel() {
        workaround = true;
        super.updateModel();
        workaround = false;
    }

    @Override
    public Collection<ObjectMemento> getModelObject() {
        var modelObj = super.getModelObject();
        if(workaround) {
            return modelObj==null
                    ? null
                    : new ArrayList<>(modelObj);
        }
        return modelObj;
    }

    // ------------------------------------------------------------------

}
