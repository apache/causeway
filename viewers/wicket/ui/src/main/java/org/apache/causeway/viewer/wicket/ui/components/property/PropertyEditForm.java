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
package org.apache.causeway.viewer.wicket.ui.components.property;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.PropertyModel;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributePanel;
import org.apache.causeway.viewer.wicket.ui.panels.PromptFormAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

final class PropertyEditForm
extends PromptFormAbstract<PropertyModel> {

    private static final long serialVersionUID = 1L;

    public PropertyEditForm(
            final String id,
            final Component parentPanel,
            final PropertyModel propertyModel) {
        super(id, parentPanel, propertyModel);
    }

    private PropertyModel propertyModel() {
        return (PropertyModel) super.getModel();
    }

    @Override
    protected void addParameters() {
        var propertyModel = propertyModel();
        var container = Wkt.containerAdd(this, PropertyEditFormPanel.ID_PROPERTY);

        var component = getComponentFactoryRegistry()
                .addOrReplaceComponent(container, UiComponentType.ATTRIBUTE_NAME_AND_VALUE, propertyModel);

        _Casts.castTo(AttributePanel.class, component)
        .ifPresent(propertyModelSubscriber->
            propertyModelSubscriber.addChangeListener(this)); // handling onUpdate and onError
    }

    @Override
    public void onUpdate(
            final AjaxRequestTarget target, final AttributePanel scalarPanel) {
    }

    @Override
    protected Either<ActionModel, PropertyModel> getMemberModel() {
        return Either.right(propertyModel());
    }

}
