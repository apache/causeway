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
import java.util.List;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidator;
import org.wicketstuff.select2.AbstractSelect2Choice;
import org.wicketstuff.select2.ChoiceProvider;
import org.wicketstuff.select2.Select2Choice;
import org.wicketstuff.select2.Select2MultiChoice;
import org.wicketstuff.select2.Settings;

import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModelWithMultiChoice;
import org.apache.isis.viewer.wicket.model.models.ScalarModelWithSingleChoice;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;

import lombok.NonNull;
import lombok.val;

/**
 * Wrapper around either a {@link Select2Choice} or a {@link Select2MultiChoice}.
 */
public class Select2
implements
    Serializable,
    HasCommonContext {

    private static final long serialVersionUID = 1L;

    final _Either<Select2ChoiceExt, Select2MultiChoiceExt> select2Choice;

    public static Select2 createSelect2(final String id, final ScalarModel scalarModel) {
        return new Select2(!scalarModel.isCollection()
                ? _Either.left(
                        Select2ChoiceExt.create(id,
                                ScalarModelWithSingleChoice.chain(scalarModel), scalarModel))
                : _Either.right(
                        Select2MultiChoiceExt.create(id,
                                ScalarModelWithMultiChoice.chain(scalarModel), scalarModel)));
    }

    private Select2(final @NonNull _Either<Select2ChoiceExt, Select2MultiChoiceExt> select2Choice) {
        this.select2Choice = select2Choice;
    }

    public void setProvider(final ChoiceProvider<ObjectMemento> providerForChoices) {
        asChoiceExt().setProvider(providerForChoices);
    }

    public AbstractSelect2Choice<ObjectMemento, ?> asComponent() {
        return select2Choice.fold(
                single->single,
                multi->multi);
    }

    public ChoiceExt asChoiceExt() {
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

    public void setRequired(final boolean required) {
        // previously this was commented out because causing more severe issues with the select2 drop-down;
        // but recent changes (possibly that setOutputMarkupId(true) is called) now seem to have resolved the issue.
        asComponent().setRequired(required);
    }
    public boolean checkRequired() {
        return asComponent().checkRequired();
    }

    public Settings getSettings() {
        return asChoiceExt().getSettings();
    }

    public ManagedObject getConvertedInputValue() {
        return getCommonContext().reconstructObject(convertedInput());
    }

    public void setLabel(final Model<String> model) {
        asComponent().setLabel(model);
    }

    public void add(final Behavior behavior) {
        asComponent().add(behavior);
    }

    public final Select2 add(final IValidator<Object> validator) {
        asComponent().add(validator);
        return this;
    }

    public <M extends Behavior> List<M> getBehaviors(final Class<M> behaviorClass) {
        return asComponent().getBehaviors(behaviorClass);
    }

    public void remove(final Behavior behavior) {
        asComponent().remove(behavior);
    }

    public void syncIfNull(final ScalarModel model) {
        if(!model.isCollection()) {
            if(memento() == null) {
                this.mementoModel().setObject(null);
                model.setObject(null);
            }
        }
    }

    public boolean isEmpty() {
        final ObjectMemento curr = this.memento();
        return curr == null;
    }

    public void clear() {
        mementoModel().setObject(null);
    }

    public IModel<String> obtainInlinePromptModel() {
        return LambdaModel.<String>of(()->{
            val memento = mementoModel().getObject();
            if(memento == null) {
                return null;
            }
            val adapter = getCommonContext().reconstructObject(memento);
            return adapter != null ? adapter.titleString() : null;
        });
    }

    public IModel<String> obtainInlinePromptModel2() {
        return LambdaModel.<String>of(()->{
            final ObjectMemento inlinePromptMemento = this.memento();
            return inlinePromptMemento != null ? inlinePromptMemento.asString(): null;
        });
    }

    // -- HELPER

    private ObjectMemento memento() {
        return select2Choice.fold(
                single->single.getModelObject(),
                multi->multi.getPackedModelObject());
    }

    private IModel<ObjectMemento> mementoModel() {
        return select2Choice.fold(
                single->single.getModel(),
                multi->multi.getPackingAdapterModel());
    }

    private ObjectMemento convertedInput() {
        final ObjectMemento convertedInput = select2Choice.fold(
                single->single.getConvertedInput(),
                multi->multi.getPackedConvertedInput());
        this.mementoModel().setObject(convertedInput);
        return convertedInput;
    }

    // -- DEPENDENCIES

    private transient IsisAppCommonContext commonContext;

    @Override
    public IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }


}
