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
package org.apache.causeway.extensions.fullcalendar.wkt.viewer;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;

public abstract class CalendaredCollectionFactoryAbstract<T>
extends ComponentFactoryAbstract
implements
    CollectionContentsAsFactory {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "calendar";

    private Class<T> type;

    protected CalendaredCollectionFactoryAbstract(final Class<T> type) {
        super(UiComponentType.COLLECTION_CONTENTS, NAME);
        this.type = type;
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if(!(model instanceof EntityCollectionModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        final EntityCollectionModel entityCollectionModel = (EntityCollectionModel) model;

        final ObjectSpecification elementSpec = entityCollectionModel.getElementType();
        final Class<?> correspondingClass = elementSpec.getCorrespondingClass();

        return appliesIf(type.isAssignableFrom(correspondingClass));
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final EntityCollectionModel collectionModel = (EntityCollectionModel) model;
        return newComponent(id, collectionModel);
    }

    protected abstract Component newComponent(final String id, final EntityCollectionModel collectionModel);


    @Override
    public IModel<String> getTitleLabel() {
        return Model.of("Calendar");
    }

    @Override
    public IModel<String> getCssClass() {
        return Model.of("fa fa-calendar");
    }
}
