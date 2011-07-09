/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.widgets.valuecollection;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.util.Generics;
import org.apache.isis.viewer.wicket.model.util.Mementos;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.dropdownchoices.DropDownChoicesForValueMementos;

/**
 * Initial skeleton - trying to add support for value choices.
 * 
 * @version $Rev$ $Date$
 */
public class ValueCollection extends ScalarPanelAbstract { // ScalarPanelTextFieldAbstract
    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_IF_REGULAR = "scalarIfRegular";
    private static final String ID_SCALAR_IF_COMPACT = "scalarIfCompact";
    private static final String ID_FEEDBACK = "feedback";

    private static final String ID_SCALAR_NAME = "scalarName";
    private static final String ID_SCALAR_VALUE = "scalarValue";

    private static final String ID_VALUE_ID = "valueId";

    public ValueCollection(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        // this.idTextField = ID_SCALAR_VALUE;
    }

    @Override
    protected FormComponentLabel addComponentForRegular() {
        // buildGui);
        valueIdField = createField();
        // }}

        addStandardSemantics();
        // addSemantics();

        final FormComponentLabel labelIfRegular = createFormComponentLabel();
        addOrReplace(labelIfRegular);

        syncWithInput();

        addOrReplace(new ComponentFeedbackPanel(ID_FEEDBACK, valueIdField));
        return labelIfRegular;
    }

    private TextField<ObjectAdapterMemento> createField() {

        return new TextField<ObjectAdapterMemento>(ID_VALUE_ID, new Model<ObjectAdapterMemento>() {

            private static final long serialVersionUID = 1L;

            @Override
            public ObjectAdapterMemento getObject() {
                if (pending != null) {
                    return pending;
                }
                final ObjectAdapter adapter = ValueCollection.this.getModelValue();
                return ObjectAdapterMemento.createOrNull(adapter);
            }

            @Override
            public void setObject(final ObjectAdapterMemento adapterMemento) {
                pending = adapterMemento;
            }

        }) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onModelChanged() {
                super.onModelChanged();
                syncWithInput();
            }

        };
    }

    protected void addStandardSemantics() {
        setRequiredIfSpecified();
        setTextFieldSizeIfSpecified();
    }

    private void setRequiredIfSpecified() {
        final ScalarModel scalarModel = getModel();
        final boolean required = scalarModel.isRequired();
        valueIdField.setRequired(required);
    }

    private void setTextFieldSizeIfSpecified() {
        final int size = determineSize();
        if (size != -1) {
            valueIdField.add(new AttributeModifier("size", true, new Model<String>("" + size)));
        }
    }

    private int determineSize() {
        final ScalarModel scalarModel = getModel();
        final ObjectSpecification noSpec = scalarModel.getTypeOfSpecification();

        final TypicalLengthFacet typicalLengthFacet = noSpec.getFacet(TypicalLengthFacet.class);
        if (typicalLengthFacet != null) {
            return typicalLengthFacet.value();
        }
        final MaxLengthFacet maxLengthFacet = noSpec.getFacet(MaxLengthFacet.class);
        if (maxLengthFacet != null) {
            return maxLengthFacet.value();
        }
        return -1;
    }

    protected FormComponentLabel createFormComponentLabel() {
        final String name = getModel().getName();
        valueIdField.setLabel(Model.of(name));

        final FormComponentLabel scalarNameAndValue = new FormComponentLabel(ID_SCALAR_IF_REGULAR, valueIdField);

        scalarNameAndValue.add(valueIdField);

        final Label scalarName = new Label(ID_SCALAR_NAME, getFormat().getLabelCaption(valueIdField));
        scalarNameAndValue.add(scalarName);

        return scalarNameAndValue;
    }

    @Override
    protected Component addComponentForCompact() {
        final Label labelIfCompact = new Label(ID_SCALAR_IF_COMPACT, getModel().getObjectAsString());
        addOrReplace(labelIfCompact);
        return labelIfCompact;
    }

    /**
     * Builds the parts of the GUI that are not dynamic.
     * 
     * @return
     */
    // private void buildGui() {
    // addOrReplaceIdField();
    // syncWithInput();
    // }

    private TextField<ObjectAdapterMemento> valueIdField;
    private ObjectAdapterMemento pending;

    // private void addOrReplaceIdField() {
    // valueIdField.setType(ObjectAdapterMemento.class);
    // // addOrReplace(valueIdField);
    // valueIdField.setVisible(false);
    // }

    protected ObjectAdapter getModelValue() {
        return scalarModel.getObject();
    }

    private ObjectAdapter getPendingAdapter() {
        final ObjectAdapterMemento memento = valueIdField.getModelObject();
        return memento != null ? memento.getObjectAdapter() : null;
    }

    private void syncWithInput() {
        final ObjectAdapter adapter = Generics.coalesce(getPendingAdapter(), scalarModel.getObject());

        // choices drop-down
        final IModel<List<? extends ObjectAdapterMemento>> choicesMementos = getChoicesModel();

        final IModel<ObjectAdapterMemento> modelObject = valueIdField.getModel();
        final DropDownChoicesForValueMementos dropDownChoicesForValueMementos =
            new DropDownChoicesForValueMementos(ID_SCALAR_VALUE, modelObject, choicesMementos);
        addOrReplace(dropDownChoicesForValueMementos);

        // link
        // syncEntityDetailsButtonWithInput(adapter);
        syncValueDetailsWithInput(adapter);
    }

    private void syncValueDetailsWithInput(final ObjectAdapter adapter) {
        if (adapter != null && scalarModel.isEntityDetailsVisible()) {
            // final ScalarModel entityModel = new ScalarModel(adapter);
            // addOrReplace(new EntityCombinedPanel(ID_VALUE_DETAILS, entityModel));
        }
    }

    private IModel<List<? extends ObjectAdapterMemento>> getChoicesModel() {
        final List<ObjectAdapter> choices = scalarModel.getChoices();
        if (choices.size() == 0) {
            return null;
        }
        // take a copy otherwise is only lazily evaluated
        final List<ObjectAdapterMemento> choicesMementos =
            Lists.newArrayList(Lists.transform(choices, Mementos.fromAdapter()));
        return Model.ofList(choicesMementos);
    }

}
