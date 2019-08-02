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
package org.apache.isis.viewer.wicket.ui.components.widgets.select2;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidator;
import org.wicketstuff.select2.AbstractSelect2Choice;
import org.wicketstuff.select2.ChoiceProvider;
import org.wicketstuff.select2.Select2Choice;
import org.wicketstuff.select2.Select2MultiChoice;
import org.wicketstuff.select2.Settings;

import org.apache.isis.runtime.memento.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModelWithMultiPending;
import org.apache.isis.viewer.wicket.model.models.ScalarModelWithPending;

/**
 * Wrapper around either a {@link Select2Choice} or a {@link Select2MultiChoice}.
 */
public class Select2 implements Serializable {

    private static final long serialVersionUID = 1L;

    final Select2ChoiceExt select2Choice;
    final Select2MultiChoiceExt select2MultiChoice;

    public static Select2 createSelect2(final String id, final ScalarModel scalarModel) {
        return scalarModel.isCollection()
                ? new Select2(
                        null,
                        Select2MultiChoiceExt.create(id,
                                ScalarModelWithMultiPending.Util.createModel(scalarModel), scalarModel))
                        : new Select2(
                                Select2ChoiceExt.create(id,
                                        ScalarModelWithPending.Util.createModel(scalarModel), scalarModel),
                                null);
    }

    private Select2(
            final Select2ChoiceExt select2Choice,
            final Select2MultiChoiceExt select2MultiChoice) {
        this.select2Choice = select2Choice;
        this.select2MultiChoice = select2MultiChoice;
    }

    public AbstractSelect2Choice component() {
        return select2Choice != null
                ? select2Choice
                        : select2MultiChoice;
    }

    public ChoiceExt choiceExt() {
        return select2Choice != null
                ? select2Choice
                        : select2MultiChoice;
    }

    public void clearInput() {
        component().clearInput();
    }

    public void setEnabled(final boolean mutability) {
        component().setEnabled(mutability);
    }

    public void setRequired(final boolean required) {
        // previously this was commented out because causing more severe issues with the select2 drop-down;
        // but recent changes (possibly that setOutputMarkupId(true) is called) now seem to have resolved the issue.
        component().setRequired(required);
    }
    public boolean checkRequired() {
        return component().checkRequired();
    }

    public Settings getSettings() {
        return choiceExt().getSettings();
    }

    public void setProvider(final ChoiceProvider<ObjectAdapterMemento> providerForChoices) {
        choiceExt().setProvider(providerForChoices);
    }

    public ObjectAdapterMemento getModelObject() {
        if (select2Choice != null) {
            return select2Choice.getModelObject();
        } else {
            final Collection<ObjectAdapterMemento> modelObject = select2MultiChoice.getModelObject();

            return ObjectAdapterMemento.wrapMementoList(modelObject, select2MultiChoice.getSpecId());
        }
    }

    public IModel<ObjectAdapterMemento> getModel() {
        if (select2Choice != null) {
            return select2Choice.getModel();
        } else {
            final IModel<Collection<ObjectAdapterMemento>> model = select2MultiChoice.getModel();
            final Collection<ObjectAdapterMemento> modelObject = model.getObject();

            final ObjectAdapterMemento memento = ObjectAdapterMemento.wrapMementoList(modelObject, select2MultiChoice.getSpecId());
            return new IModel<ObjectAdapterMemento>() {
                private static final long serialVersionUID = 1L;

                @Override
                public ObjectAdapterMemento getObject() {
                    return memento;
                }

                @Override
                public void setObject(final ObjectAdapterMemento memento) {
                    model.setObject(ObjectAdapterMemento.unwrapList(memento)
                            .orElse(null));
                }

                @Override
                public void detach() {
                }
            };
        }
    }

    public ObjectAdapterMemento getConvertedInput() {
        if (select2Choice != null) {
            return select2Choice.getConvertedInput();
        } else {
            final Collection<ObjectAdapterMemento> convertedInput = select2MultiChoice.getConvertedInput();
            return ObjectAdapterMemento.wrapMementoList(convertedInput, select2MultiChoice.getSpecId());
        }
    }

    public void setLabel(final Model<String> model) {
        component().setLabel(model);
    }

    public void add(final Behavior behavior) {
        component().add(behavior);
    }

    public final Select2 add(final IValidator<Object> validator) {
        component().add(validator);
        return this;
    }

    public <M extends Behavior> List<M> getBehaviors(Class<M> behaviorClass) {
        return component().getBehaviors(behaviorClass);
    }

    public void remove(final Behavior behavior) {
        component().remove(behavior);
    }


}
