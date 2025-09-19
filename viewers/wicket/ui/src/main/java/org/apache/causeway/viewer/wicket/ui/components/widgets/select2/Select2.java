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

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.select2.AbstractSelect2Choice;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.wicket.model.models.MultiChoiceModel;
import org.apache.causeway.viewer.wicket.model.models.SingleChoiceModel;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeModelChangeDispatcher;

public sealed interface Select2 extends Serializable
permits SingleChoice, MultiChoice {

    static Select2 create(
        final String id,
        final UiAttributeWkt attributeModel,
        final AttributeModelChangeDispatcher select2ChangeDispatcher) {
        var choiceProvider = new ChoiceProvider(attributeModel);
        var select2 = attributeModel.isSingular()
                ? new SingleChoice(id,
                                new SingleChoiceModel(attributeModel),
                                attributeModel,
                                choiceProvider)
                : new MultiChoice(id,
                                _Casts.uncheckedCast(new MultiChoiceModel(attributeModel)),
                                attributeModel,
                                choiceProvider);
        var component = select2.component();
        var settings = component.getSettings();
        settings.setCloseOnSelect(true);
        settings.setDropdownAutoWidth(true);
        settings.setWidth("100%");
        settings.setPlaceholder(attributeModel.getFriendlyName());
        // the id string is url-safe base64 encoded JSON coming from ChoiceProvider using ObjectDisplayDto as JSON
        // for UTF8 formatted JSON decoding see https://stackoverflow.com/questions/30106476/using-javascripts-atob-to-decode-base64-doesnt-properly-decode-utf-8-strings
        // TODO perhaps declare this JS else where, then just reference to it
        var template = """
            function(opt) {
                if(!opt) return "undefined";
                if(!opt.id) return "undefined";
                var base64 = opt.id.replace(/-/g, '+').replace(/_/g, '/')
                var json = decodeURIComponent(atob(base64).split('').map(function(c) {
                        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
                    }).join(''));
                var dto = JSON.parse(json);
                if(!dto) return "undefined";
                if(!dto.title) return "undefined";
                if(dto.iconHtml) {
                    return $('<span>' + dto.iconHtml + ' ' + dto.title + '</span>');
                }
                return dto.title;
            }""";

        settings.setTemplateResult(template);
        settings.setTemplateSelection(template);

        switch(attributeModel.getChoiceProviderSort()) {
            case AUTO_COMPLETE->settings.setMinimumInputLength(attributeModel.getAutoCompleteMinLength());
            case OBJECT_AUTO_COMPLETE->Facets.autoCompleteMinLength(attributeModel.getElementType())
                .ifPresent(settings::setMinimumInputLength);
            case CHOICES, NO_CHOICES->{}
        }

        component.setRequired(attributeModel.isRequired());
        // time to wait for the user to stop typing before issuing the ajax request.
        component.getSettings().getAjax(true)
            .setDelay(Math.toIntExact(attributeModel.getConfiguration().viewer().wicket().select2AjaxDelay().toMillis()));

        component.setOutputMarkupPlaceholderTag(true);
        component.setLabel(Model.of(attributeModel.getFriendlyName()));

        // listen on select2:select/unselect events (client-side)
        component.add(new OnSelectBehavior(attributeModel, select2ChangeDispatcher));

        return select2;
    }

    AbstractSelect2Choice<ObjectMemento, ?> component();
    ManagedObject convertedInputValue();
    ObjectMemento objectMemento();

    default IModel<String> obtainOutputFormatModel() {
        return LambdaModel.<String>of(()->{
            var memento = objectMemento();
            return memento!=null
                    ? memento.title()
                    : null;
        });
    }

    // -- SHORTCUTS

    default void clearInput() {
        component().clearInput();
    }

    default boolean checkRequired() {
        return component().checkRequired();
    }

    default void setMutable(final boolean mutability) {
        component().setEnabled(mutability);
    }

}
