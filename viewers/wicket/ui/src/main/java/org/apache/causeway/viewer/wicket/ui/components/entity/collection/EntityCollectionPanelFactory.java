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
package org.apache.causeway.viewer.wicket.ui.components.entity.collection;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.components.entity.EntityComponentFactoryAbstract;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * {@link ComponentFactory} for {@link EntityCollectionPanel}.
 */
public class EntityCollectionPanelFactory extends EntityComponentFactoryAbstract {

    /**
     * Helper class, used to call this factory.
     */
    @RequiredArgsConstructor
    @Getter
    public static class CollectionOwnerAndLayout implements Serializable {
        private static final long serialVersionUID = 1L;
        final @NonNull UiObjectWkt owner;
        final @NonNull CollectionLayoutData layout;
    }

    public EntityCollectionPanelFactory() {
        super(UiComponentType.ENTITY_COLLECTION, EntityCollectionPanel.class);
    }

    @Override
    protected ApplicationAdvice appliesTo(final IModel<?> model) {
        if (!(model instanceof org.apache.wicket.model.Model)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        return (model.getObject() instanceof CollectionOwnerAndLayout)
                ? ApplicationAdvice.APPLIES
                : ApplicationAdvice.DOES_NOT_APPLY;
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        var collectionOwnerAndId = (CollectionOwnerAndLayout) model.getObject();
        return new EntityCollectionPanel(id, collectionOwnerAndId.getOwner(), collectionOwnerAndId.getLayout());
    }
}
