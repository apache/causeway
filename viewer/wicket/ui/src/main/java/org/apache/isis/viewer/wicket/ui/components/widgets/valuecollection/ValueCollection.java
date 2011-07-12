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

import org.apache.log4j.Logger;
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
import org.apache.isis.viewer.wicket.model.util.Mementos;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.dropdownchoices.DropDownChoicesForValueMementos;

/**
 * Initial skeleton - trying to add support for value choices.
 * 
 * @version $Rev$ $Date$
 */
public class ValueCollection extends ScalarPanelAbstract { // ScalarPanelTextFieldAbstract
    private static final Logger LOG = Logger.getLogger(ValueCollection.class);

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_IF_REGULAR = "scalarIfRegular";
    private static final String ID_SCALAR_IF_COMPACT = "scalarIfCompact";
    private static final String ID_FEEDBACK = "feedback";

    private static final String ID_SCALAR_NAME = "scalarName";
    private static final String ID_SCALAR_VALUE_CHOICES = "scalarChoices";

    private static final String ID_VALUE_ID = "valueId";

    private TextField<ObjectAdapterMemento> valueField;
    private ObjectAdapterMemento pending;

    public ValueCollection(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        pending = scalarModel.getObjectAdapterMemento();
    }

    @Override
    protected FormComponentLabel addComponentForRegular() {
        valueField = createField();
        valueField.setEnabled(false); // the value field is never directly editable.

        addStandardSemantics();
        // addSemantics();

        syncWithInput(true);

        final FormComponentLabel labelIfRegular = createFormComponentLabel();
        addOrReplace(labelIfRegular);

        addOrReplace(dropDownChoicesForValueMementos);

        addOrReplace(new ComponentFeedbackPanel(ID_FEEDBACK, valueField));
        return labelIfRegular;
    }

    private TextField<ObjectAdapterMemento> createField() {

        return new TextField<ObjectAdapterMemento>(ID_VALUE_ID, new Model<ObjectAdapterMemento>() {

            private static final long serialVersionUID = 1L;

            @Override
            public ObjectAdapterMemento getObject() {
                if (pending != null) {
                    LOG.info("TextField: pending not null: " + pending.toString());
                    return pending;
                }
                LOG.info("TextField: pending is null");
                final ObjectAdapter adapter = ValueCollection.this.getModelValue();
                return ObjectAdapterMemento.createOrNull(adapter);
            }

            @Override
            public void setObject(final ObjectAdapterMemento adapterMemento) {
                if (adapterMemento != null) {
                    LOG.info("TextField: setting to: " + adapterMemento.toString());
                    pending = adapterMemento;
                }
                if (scalarModel != null && pending != null) {
                    LOG.info("TextField: setting to pending: " + pending.toString());
                    scalarModel.setObject(pending.getObjectAdapter());
                }

            }

        }) {
            private static final long serialVersionUID = 1L;
        };
    }

    protected void addStandardSemantics() {
        setRequiredIfSpecified();
        setTextFieldSizeIfSpecified();
    }

    private void setRequiredIfSpecified() {
        final ScalarModel scalarModel = getModel();
        final boolean required = scalarModel.isRequired();
        valueField.setRequired(required);
    }

    private void setTextFieldSizeIfSpecified() {
        final int size = determineSize();
        if (size != -1) {
            valueField.add(new AttributeModifier("size", true, new Model<String>("" + size)));
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
        valueField.setLabel(Model.of(name));

        final FormComponentLabel scalarNameAndValue = new FormComponentLabel(ID_SCALAR_IF_REGULAR, valueField);

        final Label scalarName = new Label(ID_SCALAR_NAME, getFormat().getLabelCaption(valueField));
        scalarNameAndValue.add(scalarName);
        scalarNameAndValue.add(valueField);

        // scalarNameAndValue.add(dropDownChoicesForValueMementos);

        return scalarNameAndValue;
    }

    @Override
    protected Component addComponentForCompact() {
        final Label labelIfCompact = new Label(ID_SCALAR_IF_COMPACT, getModel().getObjectAsString());
        addOrReplace(labelIfCompact);
        return labelIfCompact;
    }

    protected ObjectAdapter getModelValue() {
        pending = scalarModel.getObjectAdapterMemento();
        return scalarModel.getObject();
    }

    DropDownChoicesForValueMementos dropDownChoicesForValueMementos;

    private void syncWithInput(boolean readonlyMode) {

        // choices drop-down
        final IModel<List<? extends ObjectAdapterMemento>> choicesMementos = getChoicesModel();

        final IModel<ObjectAdapterMemento> modelObject = valueField.getModel();

        dropDownChoicesForValueMementos =
            new DropDownChoicesForValueMementos(ID_SCALAR_VALUE_CHOICES, modelObject, choicesMementos);

    }

    @Override
    protected void onBeforeRenderWhenViewMode() { // View: Read only
        // show value as (disabled) string
        dropDownChoicesForValueMementos.setVisible(false);
        valueField.setVisible(true);
    }

    @Override
    protected void onBeforeRenderWhenEnabled() { // Edit: read/write
        // Show drop-down list of values
        dropDownChoicesForValueMementos.setVisible(true);
        valueField.setVisible(false);
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
