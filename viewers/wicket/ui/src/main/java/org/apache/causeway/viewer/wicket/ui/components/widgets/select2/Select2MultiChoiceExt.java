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
import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.ui.components.widgets.select2.providers.ChoiceProviderAbstract;

import lombok.Getter;
import lombok.val;

public class Select2MultiChoiceExt
extends Select2MultiChoice<ObjectMemento>
implements HasLogicalType {

    private static final long serialVersionUID = 1L;

    public static Select2MultiChoiceExt create(
            final String id,
            final IModel<ArrayList<ObjectMemento>> modelObject,
            final PopModel popModel,
            final ChoiceProviderAbstract choiceProvider) {

        return new Select2MultiChoiceExt(id, _Casts.uncheckedCast(modelObject), popModel, choiceProvider);
    }

    @Getter(onMethod_ = {@Override}) private final LogicalType logicalType;

    Select2MultiChoiceExt(
            final String id,
            final IModel<Collection<ObjectMemento>> model,
            final PopModel popModel,
            final ChoiceProviderAbstract choiceProvider) {

        super(id, model, choiceProvider);
        logicalType = popModel.getScalarTypeSpec().getLogicalType();

        getSettings().setCloseOnSelect(true);
        getSettings().setWidth("auto");
        getSettings().setDropdownAutoWidth(true);

        setOutputMarkupPlaceholderTag(true);
    }

    public ObjectMemento getPackedModelObject() {
        return ObjectMemento.pack(this.getModelObject(), this.getLogicalType());
    }

    public ObjectMemento getPackedConvertedInput() {
        return ObjectMemento.pack(this.getConvertedInput(), this.getLogicalType());
    }

    public IModel<ObjectMemento> getPackingAdapterModel() {
        if(packingAdapterModel==null) {
            packingAdapterModel = createPackingAdapterModel();
        }
        return packingAdapterModel;
    }

    // -- HELPER

    private transient IModel<ObjectMemento> packingAdapterModel;
    private IModel<ObjectMemento> createPackingAdapterModel() {
        val multi = this;

        return new IModel<ObjectMemento>() {
            private static final long serialVersionUID = 1L;

            final ObjectMemento memento;
            final IModel<Collection<ObjectMemento>> delegate;
            {
                this.delegate = multi.getModel();
                this.memento = ObjectMemento.pack(delegate.getObject(), multi.getLogicalType());
            }

            @Override
            public ObjectMemento getObject() {
                return memento;
            }

            @Override
            public void setObject(final ObjectMemento memento) {
                delegate.setObject(ObjectMemento.unpack(memento).orElse(null));
            }

            @Override
            public void detach() {
            }
        };
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
        val modelObj = super.getModelObject();
        if(workaround) {
            return modelObj==null
                    ? null
                    : new ArrayList<>(modelObj);
        }
        return modelObj;
    }

    // ------------------------------------------------------------------



}
