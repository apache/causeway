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
package org.apache.causeway.viewer.wicket.ui.components.scalars;

import java.io.Serializable;
import java.util.function.Function;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.springframework.lang.Nullable;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.components.UiString;
import org.apache.causeway.viewer.commons.model.scalar.UiParameter;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.components.widgets.select2.Select2;
import org.apache.causeway.viewer.wicket.ui.components.widgets.select2.providers.ChoiceProviderAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.Wkt.EventTopic;

import lombok.NonNull;

public abstract class ScalarPanelSelectAbstract
extends ScalarPanelFormFieldAbstract<ManagedObject> {

    private static final long serialVersionUID = 1L;

    public static interface ChoiceTitleHandler extends Serializable {
        void clearTitleAttribute();
        void setTitleAttribute(@Nullable String titleAttribute);
    }

    protected Select2 select2;

    protected ScalarPanelSelectAbstract(
            final String id,
            final ScalarModel scalarModel) {
        super(id, scalarModel, ManagedObject.class);
        setOutputMarkupId(true);
    }

    protected final Select2 createSelect2(
            final String id,
            final Function<ScalarModel, ChoiceProviderAbstract> choiceProviderFactory) {
        var scalarModel = scalarModel();
        var select2 = Select2.createSelect2(id, scalarModel,
                choiceProviderFactory.apply(scalarModel),
                getScalarModelChangeDispatcher());
        var settings = select2.getSettings();
        settings.setPlaceholder(scalarModel.getFriendlyName());

        switch(scalarModel.getChoiceProviderSort()) {
        case CHOICES:
            break;
        case AUTO_COMPLETE:
            settings.setMinimumInputLength(scalarModel.getAutoCompleteMinLength());
            break;
        case OBJECT_AUTO_COMPLETE:
            Facets.autoCompleteMinLength(scalarModel.getElementType())
            .ifPresent(settings::setMinimumInputLength);
            break;
        case NO_CHOICES:
        default:
            // ignore if no choices
        }
        return select2;
    }

    // -- CUSTOM UPDATING BEHAVIOR

    @Override
    protected final void installScalarModelChangeBehavior() {
        /* no-op, as we already have the Select2OnSelect behavior
         * (directly) installed with the Select2 form component
         */
    }

    protected final boolean isEditable() {
        var scalarModel = scalarModel();
        // cannot edit if in table or is view-mode
        return !scalarModel.getRenderingHint().isInTable()
                && !scalarModel.isViewingMode();
    }

    protected final boolean hasAnyChoices() {
        return scalarModel().getChoiceProviderSort().isChoicesAny();
    }

    public final boolean checkSelect2Required() {
        return select2.checkRequired();
    }

    @Override
    protected final UiString obtainOutputFormat() {
        return UiString.text(select2.obtainOutputFormatModel().getObject());
    }

    // //////////////////////////////////////

    /**
     * Automatically "opens" the select2.
     */
    @Override
    protected final void onSwitchFormForInlinePrompt(
            final WebMarkupContainer inlinePromptForm,
            final AjaxRequestTarget target) {
        Wkt.javaScriptAdd(target, EventTopic.OPEN_SELECT2, inlinePromptForm.getMarkupId());
    }

    // //////////////////////////////////////

    /**
     * Refresh choices when changing.
     * <p>
     * called from onUpdate callback
     */
    @Override
    public final Repaint updateIfNecessary(final @NonNull UiParameter paramModel) {
        return super.updateIfNecessary(paramModel)
                .max(Repaint.required(this.select2!=null));
    }

}
