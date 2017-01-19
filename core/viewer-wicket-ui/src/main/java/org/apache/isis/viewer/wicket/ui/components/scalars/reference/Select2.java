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
package org.apache.isis.viewer.wicket.ui.components.scalars.reference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.IModel;
import org.wicketstuff.select2.ChoiceProvider;
import org.wicketstuff.select2.Select2Choice;
import org.wicketstuff.select2.Select2MultiChoice;
import org.wicketstuff.select2.Settings;

import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

public class Select2 implements Serializable {

    private static final long serialVersionUID = 1L;

    final Select2Choice<ObjectAdapterMemento> select2Choice;
    final Select2MultiChoice<ObjectAdapterMemento> select2MultiChoice;

    public static Select2 with(final Select2Choice<ObjectAdapterMemento> select2Choice) {
        return new Select2(select2Choice, null);
    }

    public static Select2 with(final Select2MultiChoice<ObjectAdapterMemento> select2MultiChoice) {
        return new Select2(null, select2MultiChoice);
    }

    private Select2(
            final Select2Choice<ObjectAdapterMemento> select2Choice,
            final Select2MultiChoice<ObjectAdapterMemento> select2MultiChoice) {
        this.select2Choice = select2Choice;
        this.select2MultiChoice = select2MultiChoice;
    }

    public void add(final Behavior behavior) {
        component().add(behavior);
    }

    HiddenField<?> component() {
        return select2Choice != null
                ? (HiddenField<ObjectAdapterMemento>) select2Choice
                : (HiddenField<Collection<ObjectAdapterMemento>>) select2MultiChoice;
    }

    public Settings getSettings() {
        return select2Choice != null ? select2Choice.getSettings() : select2MultiChoice.getSettings();
    }

    public void clearInput() {
        component().clearInput();
    }

    public void setEnabled(final boolean mutability) {
        component().setEnabled(mutability);
    }

    public boolean checkRequired() {
        return component().checkRequired();
    }

    public void setProvider(final ChoiceProvider<ObjectAdapterMemento> providerForChoices) {
        if (select2Choice != null)
            select2Choice.setProvider(providerForChoices);
        else
            select2MultiChoice.setProvider(providerForChoices);
    }

    public ObjectAdapterMemento getModelObject() {
        if (select2Choice != null) {
            return select2Choice.getModelObject();
        } else {
            final Collection<ObjectAdapterMemento> modelObject = select2MultiChoice.getModelObject();

            return ObjectAdapterMemento.createForList(modelObject);
        }
    }

    public IModel<ObjectAdapterMemento> getModel() {
        if (select2Choice != null) {
            return select2Choice.getModel();
        } else {
            final IModel<Collection<ObjectAdapterMemento>> model = select2MultiChoice.getModel();
            final Collection<ObjectAdapterMemento> modelObject = model.getObject();

            final ObjectAdapterMemento memento = ObjectAdapterMemento.createForList(modelObject);
            return new IModel<ObjectAdapterMemento>() {
                @Override
                public ObjectAdapterMemento getObject() {
                    return memento;
                }

                @Override
                public void setObject(final ObjectAdapterMemento memento) {

                    final ArrayList<ObjectAdapterMemento> mementos = memento.getList();
                    model.setObject(mementos);
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

            return ObjectAdapterMemento.createForList(convertedInput);
        }
    }
}
