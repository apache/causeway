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
package org.apache.causeway.viewer.wicket.ui.components.object.icontitle;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ProposedValueModel;
import org.apache.causeway.viewer.wicket.model.models.ObjectAdapterModel;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;

/**
 * {@link ComponentFactory} for {@link ObjectIconAndTitlePanel}.
 *
 * @implNote Knows how to deal with {@link ObjectAdapterModel}. And for
 * {@link UiAttributeWkt} we have an adapter {@link ProposedValueModel}
 * that implements {@link ObjectAdapterModel}, such that it can also deal
 * with {@link UiAttributeWkt}.
 *
 */
public class ObjectIconAndTitlePanelFactory extends ComponentFactoryAbstract {

    public ObjectIconAndTitlePanelFactory(
            final UiComponentType uiComponentType,
            final Class<?> componentClass) {
        super(uiComponentType, componentClass);
    }

    public ObjectIconAndTitlePanelFactory(
            final UiComponentType uiComponentType,
            final String name,
            final Class<?> componentClass) {
        super(uiComponentType, name, componentClass);
    }

    public ObjectIconAndTitlePanelFactory() {
        this(UiComponentType.OBJECT_ICON_AND_TITLE, ObjectIconAndTitlePanel.class);
    }

    @Override
    protected ApplicationAdvice appliesTo(final IModel<?> model) {

        final ObjectSpecification spec;

        if (model instanceof ObjectAdapterModel) {
            spec = ((ObjectAdapterModel) model).getTypeOfSpecification();
        } else if (model instanceof UiAttributeWkt) {
            spec = ((UiAttributeWkt) model).getElementType();
        } else {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }

        return spec.isSingular()
                && !spec.isValue()
                        ? ApplicationAdvice.APPLIES
                        : ApplicationAdvice.DOES_NOT_APPLY;
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {

        final ObjectAdapterModel objectAdapterModel;

        if (model instanceof ObjectAdapterModel) {
            objectAdapterModel = (ObjectAdapterModel) model;
        } else if (model instanceof UiAttributeWkt) {
            var attributeModel = (UiAttributeWkt) model;
            // effectively acts as an adapter from UiAttribute to ObjectAdapterModel
            objectAdapterModel = ProposedValueModel.chain(attributeModel);
        } else {
            throw _Exceptions.unexpectedCodeReach();
        }

        return new ObjectIconAndTitlePanel(id, IconSize.MEDIUM, objectAdapterModel);
    }

    /**
     * refactoring hint: go through proper ComponentFactory channels instead
     */
    @Deprecated
    public static Component entityIconAndTitlePanel(
            final String componentId,
            final ObjectAdapterModel objectAdapterModel) {
        return new ObjectIconAndTitlePanel(componentId, IconSize.MEDIUM, objectAdapterModel);
    }

}
