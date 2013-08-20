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
package org.apache.isis.viewer.wicket.ui.components.widgets.valuechoices;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Select2Choice;
import com.vaynberg.wicket.select2.TextChoiceProvider;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ModelAbstract;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.util.Mementos;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * Initial skeleton - trying to add support for value choices.
 */
public class ValueChoicesSelect2Panel extends ScalarPanelAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(ValueChoicesSelect2Panel.class);

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_IF_REGULAR = "scalarIfRegular";
    private static final String ID_SCALAR_IF_COMPACT = "scalarIfCompact";

    private static final String ID_SCALAR_NAME = "scalarName";

    private static final String ID_VALUE_ID = "valueId";

    private Select2Choice<ObjectAdapterMemento> select2ChoiceField;
    private ObjectAdapterMemento pending;

    public ValueChoicesSelect2Panel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        pending = scalarModel.getObjectAdapterMemento();
    }

    @Override
    protected FormComponentLabel addComponentForRegular() {

        final IModel<ObjectAdapterMemento> modelObject = createModel();
        
        select2ChoiceField = new Select2Choice<ObjectAdapterMemento>(ID_VALUE_ID, modelObject);
        select2ChoiceField.setProvider(newChoiceProviderForArgs(null));

        addStandardSemantics();

        final FormComponentLabel labelIfRegular = createFormComponentLabel();
        if(getModel().isRequired()) {
            labelIfRegular.add(new CssClassAppender("mandatory"));
        }
        
        addOrReplace(labelIfRegular);
        addFeedbackTo(labelIfRegular, select2ChoiceField);
        addAdditionalLinksTo(labelIfRegular);
        
        return labelIfRegular;
    }

    private ChoiceProvider<ObjectAdapterMemento> newChoiceProviderForArgs(final ObjectAdapter[] argumentsIfAvailable) {
        final List<ObjectAdapterMemento> choicesMementos = getChoiceMementos(argumentsIfAvailable);
        final IModel<List<ObjectAdapterMemento>> choicesModel = newModel(choicesMementos);
        final ChoiceProvider<ObjectAdapterMemento> choiceProvider = newChoiceProvider(choicesModel);
        return choiceProvider;
    }

    private IModel<List<ObjectAdapterMemento>> newModel(final List<ObjectAdapterMemento> choicesMementos) {
        return new ModelAbstract<List<ObjectAdapterMemento>>(choicesMementos){
            private static final long serialVersionUID = 1L;

            @Override
            protected List<ObjectAdapterMemento> load() {
                return getObject();
            }};
    }

    private List<ObjectAdapterMemento> getChoiceMementos(final ObjectAdapter[] argumentsIfAvailable) {
        final List<ObjectAdapter> choices = scalarModel.getChoices(argumentsIfAvailable);
        
        // take a copy otherwise is only lazily evaluated
        final List<ObjectAdapterMemento> choicesMementos = Lists.newArrayList(Lists.transform(choices, Mementos.fromAdapter()));
        
        final ObjectAdapterMemento currentValue = getModel().getObjectAdapterMemento();
        if(currentValue != null && !choicesMementos.contains(currentValue)) {
            choicesMementos.add(currentValue);
        }
        return choicesMementos;
    }

    private Model<ObjectAdapterMemento> createModel() {
        return new Model<ObjectAdapterMemento>() {

            private static final long serialVersionUID = 1L;

            @Override
            public ObjectAdapterMemento getObject() {
                if (pending != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("TextField: pending not null: " + pending.toString());
                    }
                    return pending;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("TextField: pending is null");
                }
                final ObjectAdapter adapter = ValueChoicesSelect2Panel.this.getModelValue();
                return ObjectAdapterMemento.createOrNull(adapter);
            }

            @Override
            public void setObject(final ObjectAdapterMemento adapterMemento) {
                if (adapterMemento != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("TextField: setting to: " + adapterMemento.toString());
                    }
                    pending = adapterMemento;
                }
                if (scalarModel != null && pending != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("TextField: setting to pending: " + pending.toString());
                    }
                    scalarModel.setObject(pending.getObjectAdapter(ConcurrencyChecking.NO_CHECK));
                }
            }
        };
    }

    protected void addStandardSemantics() {
        setRequiredIfSpecified();
    }

    private void setRequiredIfSpecified() {
        final ScalarModel scalarModel = getModel();
        final boolean required = scalarModel.isRequired();
        select2ChoiceField.setRequired(required);
    }

    protected FormComponentLabel createFormComponentLabel() {
        final String name = getModel().getName();
        select2ChoiceField.setLabel(Model.of(name));

        final FormComponentLabel labelIfRegular = new FormComponentLabel(ID_SCALAR_IF_REGULAR, select2ChoiceField);

        final String describedAs = getModel().getDescribedAs();
        if(describedAs != null) {
            labelIfRegular.add(new AttributeModifier("title", Model.of(describedAs)));
        }

        final Label scalarName = new Label(ID_SCALAR_NAME, getRendering().getLabelCaption(select2ChoiceField));
        labelIfRegular.add(scalarName);
        labelIfRegular.add(select2ChoiceField);

        return labelIfRegular;
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

    protected ChoiceProvider<ObjectAdapterMemento> newChoiceProvider(final IModel<List<ObjectAdapterMemento>> choicesMementos) {
        ChoiceProvider<ObjectAdapterMemento> provider = new TextChoiceProvider<ObjectAdapterMemento>() {

            private static final long serialVersionUID = 1L;

            @Override
            protected String getDisplayText(ObjectAdapterMemento choice) {
                
                final ObjectAdapter objectAdapter = choice.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
                return objectAdapter.titleString(null);
            }

            @Override
            protected Object getId(ObjectAdapterMemento choice) {
                final ObjectAdapter objectAdapter = choice.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
                return objectAdapter.getObject().toString(); // toString of each value acts as its the key
            }

            @Override
            public void query(String term, int page, com.vaynberg.wicket.select2.Response<ObjectAdapterMemento> response) {
                response.addAll(choicesMementos.getObject());
            }

            @Override
            public Collection<ObjectAdapterMemento> toChoices(final Collection<String> ids) {
                final List<ObjectAdapterMemento> mementos = choicesMementos.getObject();
                Predicate<ObjectAdapterMemento> predicate = new Predicate<ObjectAdapterMemento>() {

                    @Override
                    public boolean apply(ObjectAdapterMemento input) {
                        final String id = (String) getId(input);
                        return ids.contains(id);
                    }
                };
                return Collections2.filter(mementos, predicate); 
            }

        };
        return provider;
    }

    @Override
    protected void onBeforeRenderWhenViewMode() { // View: Read only
        select2ChoiceField.setEnabled(false);
    }

    @Override
    protected void onBeforeRenderWhenEnabled() { // Edit: read/write
        select2ChoiceField.setEnabled(true);
    }

    
    @Override
    protected void addFormComponentBehaviour(Behavior behavior) {
        select2ChoiceField.add(behavior);
    }


    // //////////////////////////////////////

    @Override
    public void updateChoices(ObjectAdapter[] arguments) {
        select2ChoiceField.setProvider(newChoiceProviderForArgs(arguments));
        getModel().setPending(null);
        select2ChoiceField.clearInput();
    }

}
