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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.springframework.lang.Nullable;
import org.wicketstuff.select2.ChoiceProvider;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.Select2;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.Wkt.EventTopic;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class ScalarPanelSelectAbstract
extends ScalarPanelFormFieldAbstract<ManagedObject> {

    private static final long serialVersionUID = 1L;

    @Getter
    protected Select2 select2;

    public ScalarPanelSelectAbstract(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel, ManagedObject.class);
        setOutputMarkupId(true);
    }

    protected final Select2 createSelect2(final String id) {
        val scalarModel = scalarModel();

        val select2 = Select2.createSelect2(id, scalarModel());
        select2.setLabel(Model.of(scalarModel.getFriendlyName()));

        updateChoices(select2);
        return select2;
    }

    /**
     * Mandatory hook (is called by {@link #createSelect2(String)})
     */
    protected abstract ChoiceProvider<ObjectMemento> buildChoiceProvider();

    // //////////////////////////////////////

    /**
     * Automatically "opens" the select2.
     */
    @Override
    protected void onSwitchFormForInlinePrompt(
            final WebMarkupContainer inlinePromptForm,
            final AjaxRequestTarget target) {
        Wkt.javaScriptAdd(target, EventTopic.OPEN_SELECT2, inlinePromptForm.getMarkupId());
    }

    // //////////////////////////////////////

    /**
     * Hook method to refresh choices when changing.
     *
     * <p>
     * called from onUpdate callback
     */
    @Override
    public Repaint updateIfNecessary(
            final @NonNull ParameterUiModel paramModel,
            final @NonNull Optional<AjaxRequestTarget> target) {

        val repaint = super.updateIfNecessary(paramModel, target);
        final boolean choicesUpdated = updateChoices(this.select2);

        if (repaint == Repaint.NOTHING) {
            if (choicesUpdated) {
                return Repaint.PARAM_ONLY;
            } else {
                return Repaint.NOTHING;
            }
        } else {
            return repaint;
        }
    }

    private boolean updateChoices(final @Nullable Select2 select2) {
        if (select2 == null) {
            return false;
        }

        final ChoiceProvider<ObjectMemento> choiceProvider = buildChoiceProvider();
        select2.setProvider(choiceProvider);

        //sets up the choices, also ensuring that any currently held value is compatible.

        _Casts.castTo(ObjectAdapterMementoProviderAbstract.class, choiceProvider)
        .ifPresent(mementoProvider->{
            if(mementoProvider.dependsOnPreviousArgs()){

                _Debug.log("ChoiceProvider with DependsOnPreviousArgs while scalarModel() %s",
                        scalarModel().isEmpty()? "is empty" : "is not empty");

                System.err.printf("ChoiceProvider with DependsOnPreviousArgs while scalarModel() %s%n",
                        scalarModel().isEmpty()? "is empty" : "is not empty");

                //XXX what to do?
//                if(scalarModel().isScalar()) {
//                    if(select2.isEmpty()) {
//                        select2.clear(); // why?
//                        getModel().setObject(null);
//                    }
//                }
//
//                if(scalarModel().isEmpty()) {
//                    select2.clear();
//                }

            }
        });

        return true;
    }

    /**
     * Repaints just the Select2 component
     *
     * @param target The Ajax request handler
     */
    @Override
    public void repaint(final AjaxRequestTarget target) {
        target.add(this);
    }

}
