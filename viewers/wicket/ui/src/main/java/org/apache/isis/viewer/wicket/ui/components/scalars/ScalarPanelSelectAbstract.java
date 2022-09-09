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
import org.springframework.lang.Nullable;
import org.wicketstuff.select2.Settings;

import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.util.Facets;
import org.apache.isis.viewer.commons.model.scalar.UiParameter;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.Select2;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ChoiceProviderAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.Wkt.EventTopic;

import lombok.NonNull;
import lombok.val;

public abstract class ScalarPanelSelectAbstract
extends ScalarPanelFormFieldAbstract<ManagedObject> {

    private static final long serialVersionUID = 1L;

    protected Select2 select2;

    public ScalarPanelSelectAbstract(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel, ManagedObject.class);
        setOutputMarkupId(true);
    }

    protected final Select2 createSelect2(final String id) {
        val select2 = Select2.createSelect2(id, scalarModel(), buildChoiceProvider());
        addSelect2Semantics(select2.getSettings());
        return select2;
    }

    private void addSelect2Semantics(final Settings settings) {
        val scalarModel = scalarModel();

        switch(scalarModel.getChoiceProviderSort()) {
        case CHOICES:
            settings.setPlaceholder(scalarModel.getFriendlyName());
            return;
        case AUTO_COMPLETE:
            settings.setPlaceholder(scalarModel.getFriendlyName());
            settings.setMinimumInputLength(scalarModel.getAutoCompleteMinLength());
            return;
        case OBJECT_AUTO_COMPLETE:
            //TODO render object place holder?
            Facets.autoCompleteMinLength(scalarModel.getScalarTypeSpec())
            .ifPresent(settings::setMinimumInputLength);
            return;
        default:
            // ignore if no choices
        }
    }

    public final boolean checkSelect2Required() {
        return select2.checkRequired();
    }

    /**
     * Mandatory hook (is called by {@link #createSelect2(String)})
     */
    protected abstract ChoiceProviderAbstract buildChoiceProvider();

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
            final @NonNull UiParameter paramModel,
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
        select2.rebuildChoiceProvider();
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
