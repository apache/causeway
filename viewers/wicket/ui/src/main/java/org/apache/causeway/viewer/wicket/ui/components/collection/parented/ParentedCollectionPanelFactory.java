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
package org.apache.causeway.viewer.wicket.ui.components.collection.parented;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.components.object.ObjectComponentFactoryAbstract;

/**
 * {@link ComponentFactory} for {@link ParentedCollectionPanel}.
 */
public class ParentedCollectionPanelFactory extends ObjectComponentFactoryAbstract {

    /**
     * Helper class, used to call this factory.
     */
    public record CollectionOwnerAndLayout(
            @NonNull UiObjectWkt owner,
            @NonNull CollectionLayoutData layout) implements Serializable {
    }

    public ParentedCollectionPanelFactory() {
        super(UiComponentType.PARENTED_COLLECTION, ParentedCollectionPanel.class);
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
        return new ParentedCollectionPanel(id, collectionOwnerAndId.owner(), collectionOwnerAndId.layout());
    }
}
