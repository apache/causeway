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
import java.util.List;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidator;
import org.wicketstuff.select2.AbstractSelect2Choice;
import org.wicketstuff.select2.Select2Choice;
import org.wicketstuff.select2.Select2MultiChoice;

import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarModelWithMultiChoice;
import org.apache.causeway.viewer.wicket.model.models.ScalarModelWithSingleChoice;
import org.apache.causeway.viewer.wicket.model.util.WktContext;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarModelChangeDispatcher;
import org.apache.causeway.viewer.wicket.ui.components.widgets.select2.providers.ChoiceProviderAbstract;
import org.apache.causeway.viewer.wicket.ui.components.widgets.select2.providers.ChoiceProviderAbstractForScalarModel;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

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
            final ScalarModel scalarModel,
            final ChoiceProviderAbstract choiceProvider,
            final ScalarModelChangeDispatcher select2ChangeDispatcher) {
        val select2 = new Select2(scalarModel.isSingular()
                ? Either.left(
                        Select2ChoiceExt.create(id,
                                ScalarModelWithSingleChoice.chain(scalarModel),
                                scalarModel,
                                choiceProvider))
                : Either.right(
                        Select2MultiChoiceExt.create(id,
                                ScalarModelWithMultiChoice.chain(scalarModel),
                                scalarModel,
                                choiceProvider)));

        select2.setLabel(Model.of(scalarModel.getFriendlyName()));
        select2.getSettings().setWidth("100%");

        // listen on select2:select/unselect events (client-side)
        select2.add(new Select2OnSelect(scalarModel, select2ChangeDispatcher));

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

    // not sure if required any more
    @SneakyThrows
    public void rebuildChoiceProvider() {
        val oldProvider = (ChoiceProviderAbstractForScalarModel)
                select2Choice.fold(
                        Select2ChoiceExt::getProvider,
                        Select2MultiChoiceExt::getProvider);
        val scalarModel = oldProvider.scalarModel();
        val constr = oldProvider.getClass().getConstructor(ScalarModel.class);
        val newProvider = constr.newInstance(scalarModel);
        select2Choice.accept(
                single->single.setProvider(newProvider),
                multi->multi.setProvider(newProvider));
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

    public void setRequired(final boolean required) {
        // previously this was commented out because causing more severe issues with the select2 drop-down;
        // but recent changes (possibly that setOutputMarkupId(true) is called) now seem to have resolved the issue.
        asComponent().setRequired(required);
    }
    public boolean checkRequired() {
        return asComponent().checkRequired();
    }

    public ManagedObject getConvertedInputValue() {
        return getObjectManager().demementify(convertedInput());
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
        if(!model.isPlural()) {
            if(memento() == null) {
                this.mementoModel().setObject(null);
                model.setObject(null);
            }
        }
    }

    public boolean isEmpty() {
        return memento() == null;
    }

    public void clear() {
        mementoModel().setObject(null);
    }

    public IModel<String> obtainOutputFormatModel() {
        return LambdaModel.<String>of(()->{
            val memento = memento();
            return memento!=null
                    ? memento.getTitle()
                    : null;
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

    private transient MetaModelContext mmc;
    @Override
    public MetaModelContext getMetaModelContext() {
        return mmc = WktContext.computeIfAbsent(mmc);
    }


}
