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
import org.wicketstuff.select2.Settings;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.viewer.wicket.model.models.AttributeModelWithMultiChoice;
import org.apache.causeway.viewer.wicket.model.models.AttributeModelWithSingleChoice;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeModelChangeDispatcher;

public interface Select2 extends Serializable {

    static Select2 create(
        final String id,
        final UiAttributeWkt attributeModel,
        final ChoiceProviderRecord choiceProvider,
        final AttributeModelChangeDispatcher select2ChangeDispatcher) {
        var select2 = attributeModel.isSingular()
                ? new SingleChoice(id,
                                AttributeModelWithSingleChoice.chain(attributeModel),
                                attributeModel,
                                choiceProvider)
                : new MultiChoice(id,
                                _Casts.uncheckedCast(AttributeModelWithMultiChoice.chain(attributeModel)),
                                attributeModel,
                                choiceProvider);

        var component = select2.component();
        component.setOutputMarkupPlaceholderTag(true);
        component.getSettings().setCloseOnSelect(true);
        component.getSettings().setDropdownAutoWidth(true);
        component.getSettings().setWidth("100%");
        component.setLabel(Model.of(attributeModel.getFriendlyName()));

        // listen on select2:select/unselect events (client-side)
        component.add(new Select2OnSelect(attributeModel, select2ChangeDispatcher));

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

    default Settings settings() {
        return component().getSettings();
    }

    default void clearInput() {
        component().clearInput();
    }

    default boolean isRequired() {
        return component().isRequired();
    }

    default void setMutable(final boolean mutability) {
        component().setEnabled(mutability);
    }

}
