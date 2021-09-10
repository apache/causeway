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
package org.apache.isis.viewer.wicket.ui.components.collectioncontents.multiple;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;

/**
 * {@link ComponentFactory} for {@link CollectionContentsMultipleViewsPanel}.
 */
public class CollectionContentsMultipleViewsPanelFactory extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public CollectionContentsMultipleViewsPanelFactory() {
        super(ComponentType.COLLECTION_CONTENTS, CollectionContentsMultipleViewsPanel.class);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        return appliesIf(model instanceof EntityCollectionModel);
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final EntityCollectionModel collectionModel = (EntityCollectionModel) model;
        return new CollectionContentsMultipleViewsPanel(id, collectionModel);
    }

}
