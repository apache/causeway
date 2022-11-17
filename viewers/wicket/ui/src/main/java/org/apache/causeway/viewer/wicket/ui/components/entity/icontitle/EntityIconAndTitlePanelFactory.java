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
package org.apache.causeway.viewer.wicket.ui.components.entity.icontitle;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ChainingObjectModel;
import org.apache.causeway.viewer.wicket.model.models.ObjectAdapterModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;

import lombok.val;

/**
 * {@link ComponentFactory} for {@link EntityIconAndTitlePanel}.
 *
 * @implNote Knows how to deal with {@link ObjectAdapterModel}. And for
 * {@link ScalarModel} we have an adapter {@link ChainingObjectModel}
 * that implements {@link ObjectAdapterModel}, such that it can also deal
 * with {@link ScalarModel}.
 *
 */
public class EntityIconAndTitlePanelFactory extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public EntityIconAndTitlePanelFactory(
            final UiComponentType uiComponentType,
            final Class<?> componentClass) {

        super(uiComponentType, componentClass);
    }

    public EntityIconAndTitlePanelFactory(
            final UiComponentType uiComponentType,
            final String name,
            final Class<?> componentClass) {

        super(uiComponentType, name, componentClass);
    }

    public EntityIconAndTitlePanelFactory() {
        this(UiComponentType.ENTITY_ICON_AND_TITLE, EntityIconAndTitlePanel.class);
    }

    @Override
    protected ApplicationAdvice appliesTo(final IModel<?> model) {

        final ObjectSpecification spec;

        if (model instanceof ObjectAdapterModel) {
            spec = ((ObjectAdapterModel) model).getTypeOfSpecification();
        } else if (model instanceof ScalarModel) {
            spec = ((ScalarModel) model).getScalarTypeSpec();
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
        } else if (model instanceof ScalarModel) {
            val scalarModel = (ScalarModel) model;
            // effectively acts as an adapter from ScalarModel to ObjectAdapterModel
            objectAdapterModel = ChainingObjectModel.chain(scalarModel);
        } else {
            throw _Exceptions.unexpectedCodeReach();
        }

        return new EntityIconAndTitlePanel(id, objectAdapterModel);
    }

}
