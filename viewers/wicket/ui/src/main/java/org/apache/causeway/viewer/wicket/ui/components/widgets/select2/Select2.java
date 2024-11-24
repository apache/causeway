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

import java.io.Serializable;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.select2.AbstractSelect2Choice;
import org.wicketstuff.select2.Select2Choice;
import org.wicketstuff.select2.Select2MultiChoice;

import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.models.AttributeModelWithMultiChoice;
import org.apache.causeway.viewer.wicket.model.models.AttributeModelWithSingleChoice;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeModelChangeDispatcher;
import org.apache.causeway.viewer.wicket.ui.components.widgets.select2.providers.ChoiceProviderAbstract;

import lombok.NonNull;

/**
 * Wrapper around either a {@link Select2Choice} or a {@link Select2MultiChoice}.
 */
public class Select2
implements
    Serializable,
    HasCommonContext {

    private static final long serialVersionUID = 1L;

    final Either<Select2ChoiceExt, Select2MultiChoiceExt> select2Choice;

    public static Select2 createSelect2(
            final String id,
            final UiAttributeWkt attributeModel,
            final ChoiceProviderAbstract choiceProvider,
            final AttributeModelChangeDispatcher select2ChangeDispatcher) {
        var select2 = new Select2(attributeModel.isSingular()
                ? Either.left(
                        Select2ChoiceExt.create(id,
                                AttributeModelWithSingleChoice.chain(attributeModel),
                                attributeModel,
                                choiceProvider))
                : Either.right(
                        Select2MultiChoiceExt.create(id,
                                AttributeModelWithMultiChoice.chain(attributeModel),
                                attributeModel,
                                choiceProvider)));

        select2.setLabel(Model.of(attributeModel.getFriendlyName()));
        select2.getSettings().setWidth("100%");

        // listen on select2:select/unselect events (client-side)
        select2.add(new Select2OnSelect(attributeModel, select2ChangeDispatcher));

        return select2;
    }

    private Select2(final @NonNull Either<Select2ChoiceExt, Select2MultiChoiceExt> select2Choice) {
        this.select2Choice = select2Choice;
        asComponent().setOutputMarkupId(true);
    }

    public org.wicketstuff.select2.Settings getSettings() {
        return select2Choice.fold(
                Select2ChoiceExt::getSettings,
                Select2MultiChoiceExt::getSettings);
    }

    public AbstractSelect2Choice<ObjectMemento, ?> asComponent() {
        return select2Choice.fold(
                single->single,
                multi->multi);
    }

    public void clearInput() {
        asComponent().clearInput();
    }

    public void setEnabled(final boolean mutability) {
        asComponent().setEnabled(mutability);
    }

    public boolean checkRequired() {
        return asComponent().checkRequired();
    }

    public ManagedObject getConvertedInputValue() {
        return getObjectManager().demementify(convertedInput());
    }

    public IModel<String> obtainOutputFormatModel() {
        return LambdaModel.<String>of(()->{
            var memento = memento();
            return memento!=null
                    ? memento.title()
                    : null;
        });
    }

    // -- HELPER

    private void setLabel(final Model<String> model) {
        asComponent().setLabel(model);
    }

    private void add(final Behavior behavior) {
        asComponent().add(behavior);
    }

    private ObjectMemento memento() {
        return select2Choice.fold(
                single->single.getModelObject(),
                multi->multi.getPackedModelObject());
    }

    private ObjectMemento convertedInput() {
        final ObjectMemento convertedInput = select2Choice.fold(
                single->single.getConvertedInput(),
                multi->multi.getPackedConvertedInput());
        return convertedInput;
    }

}
