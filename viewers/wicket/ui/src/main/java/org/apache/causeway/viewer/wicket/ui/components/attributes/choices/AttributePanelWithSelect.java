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
package org.apache.causeway.viewer.wicket.ui.components.attributes.choices;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.attrib.UiParameter;
import org.apache.causeway.viewer.commons.model.components.UiString;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributePanelWithFormField;
import org.apache.causeway.viewer.wicket.ui.components.widgets.select2.ChoiceProviderRecord;
import org.apache.causeway.viewer.wicket.ui.components.widgets.select2.Select2;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.Wkt.EventTopic;

public abstract class AttributePanelWithSelect
extends AttributePanelWithFormField<ManagedObject> {

    private static final long serialVersionUID = 1L;

    public static interface ChoiceTitleHandler extends Serializable {
        void clearTitleAttribute();
        void setTitleAttribute(@Nullable String titleAttribute);
    }

    protected Select2 select2;

    protected AttributePanelWithSelect(
            final String id,
            final UiAttributeWkt attributeModel) {
        super(id, attributeModel, ManagedObject.class);
        setOutputMarkupId(true);
    }

    protected final Select2 createSelect2(
            final String id) {
        var attributeModel = attributeModel();
        var select2 = Select2.create(id, attributeModel,
                new ChoiceProviderRecord(attributeModel),
                getAttributeModelChangeDispatcher());
        var settings = select2.settings();
        settings.setPlaceholder(attributeModel.getFriendlyName());

        switch(attributeModel.getChoiceProviderSort()) {
        case CHOICES:
            break;
        case AUTO_COMPLETE:
            settings.setMinimumInputLength(attributeModel.getAutoCompleteMinLength());
            break;
        case OBJECT_AUTO_COMPLETE:
            Facets.autoCompleteMinLength(attributeModel.getElementType())
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
    protected final void installModelChangeBehavior() {
        /* no-op, as we already have the Select2OnSelect behavior
         * (directly) installed with the Select2 form component
         */
    }

    protected final boolean isEditable() {
        var attributeModel = attributeModel();
        // cannot edit if in table or is view-mode
        return !attributeModel.getRenderingHint().isInTable()
                && !attributeModel.isViewingMode();
    }

    protected final boolean hasAnyChoices() {
        return attributeModel().getChoiceProviderSort().isChoicesAny();
    }

    public final boolean checkSelect2Required() {
        return select2.isRequired();
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
