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
package org.apache.isis.viewer.wicket.ui.components.scalars.valuechoices;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.select2.ChoiceProvider;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelSelect2Abstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.Select2;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderForValueChoices;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;

public class ValueChoicesSelect2Panel extends ScalarPanelSelect2Abstract {


    private static final long serialVersionUID = 1L;

    public ValueChoicesSelect2Panel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }


    // ///////////////////////////////////////////////////////////////////

    @Override
    protected Component createComponentForCompact() {
        return new Label(ID_SCALAR_IF_COMPACT, getModel().getObjectAsString());
    }

    @Override
    protected MarkupContainer createComponentForRegular() {

        if(select2 == null) {
            this.select2 = createSelect2(ID_SCALAR_VALUE);
        } else {
            select2.clearInput();
        }

        FormComponent<?> formComponent = select2.component();

        return createFormGroup(formComponent);
    }


    private List<ObjectAdapterMemento> getChoiceMementos(final ObjectAdapter[] argumentsIfAvailable) {
        final List<ObjectAdapter> choices =
                scalarModel.getChoices(argumentsIfAvailable, getAuthenticationSession());

        return _Lists.map(choices, ObjectAdapterMemento.Functions.fromAdapter());
    }

    // ///////////////////////////////////////////////////////////////////


    // ///////////////////////////////////////////////////////////////////

    @Override
    protected InlinePromptConfig getInlinePromptConfig() {
        return InlinePromptConfig.supportedAndHide(select2.component());
    }

    @Override
    protected IModel<String> obtainInlinePromptModel() {
        ObjectAdapterMemento modelObject = select2.getModelObject();
        String str = modelObject != null ? modelObject.asString(): null;
        return Model.of(str);
    }


    // ///////////////////////////////////////////////////////////////////


    @Override
    protected void onInitializeWhenViewMode() {
        // View: Read only
        select2.setEnabled(false);
    }

    @Override
    protected void onInitializeWhenEnabled() {
        // Edit: read/write
        select2.setEnabled(true);

        // TODO: should the title AttributeModifier installed in onBeforeWhenDisabled be removed here?
    }

    @Override
    protected void onInitializeWhenDisabled(final String disableReason) {
        super.onInitializeWhenDisabled(disableReason);
        setTitleAttribute(disableReason);
        select2.setEnabled(false);
    }

    private void setTitleAttribute(final String titleAttribute) {
        Tooltips.addTooltip(getComponentForRegular(), titleAttribute);
    }


    // //////////////////////////////////////



    // in corresponding code in ReferencePanelFactory, these is a branch for different types of providers
    // (choice vs autoComplete).  Here though - because values don't currently support autoComplete - no branch is required
    @Override
    protected ChoiceProvider<ObjectAdapterMemento> buildChoiceProvider(final ObjectAdapter[] argsIfAvailable) {
        final List<ObjectAdapterMemento> choicesMementos = getChoiceMementos(argsIfAvailable);
        return new ObjectAdapterMementoProviderForValueChoices(scalarModel, choicesMementos, wicketViewerSettings);
    }

    @Override
    protected void syncIfNull(final Select2 select2, final List<ObjectAdapterMemento> choicesMementos) {
        final ObjectAdapterMemento curr = getModel().getObjectAdapterMemento();

        if(curr == null) {
            select2.getModel().setObject(null);
        }
    }


    public ScalarModel getScalarModel() {
        return scalarModel;
    }

    @com.google.inject.Inject
    WicketViewerSettings wicketViewerSettings;

    @Override
    protected String getScalarPanelType() {
        return "valueChoicesSelect2Panel";
    }

}
