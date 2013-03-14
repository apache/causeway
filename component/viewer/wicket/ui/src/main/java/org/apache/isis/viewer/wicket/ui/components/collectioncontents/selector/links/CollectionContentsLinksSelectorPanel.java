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

package org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.links;

import java.util.List;

import org.apache.wicket.model.IModel;

import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.core.metamodel.facets.members.resolve.RenderFacet;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.unresolved.CollectionContentsAsUnresolvedPanelFactory;
import org.apache.isis.viewer.wicket.ui.selector.links.LinksSelectorPanelAbstract;

/**
 * Provides a list of links for selecting other views that support
 * {@link ComponentType#COLLECTION_CONTENTS} with a backing
 * {@link EntityCollectionModel}.
 * 
 * <p>
 * Most of the heavy lifting is factored out into the superclass,
 * {@link LinksSelectorPanelAbstract}.
 */
public class CollectionContentsLinksSelectorPanel extends LinksSelectorPanelAbstract<EntityCollectionModel> {

    private static final long serialVersionUID = 1L;

    public CollectionContentsLinksSelectorPanel(final String id, final EntityCollectionModel model, final ComponentFactory factory) {
        super(id, ComponentType.COLLECTION_CONTENTS.toString(), model, factory);
    }

    @Override
    protected EntityCollectionModel dummyOf(EntityCollectionModel model) {
        return model.asDummy();
    }

    /**
     * return the index of {@link CollectionContentsAsUnresolvedPanelFactory unresolved panel} if present and not eager loading;
     * else the index of {@link CollectionContentsAsAjaxTablePanelFactory ajax table} if present,
     * otherwise first factory.
     */
    protected int determineInitialFactory(final List<ComponentFactory> componentFactories, final IModel<?> model) {
        if(!hasResolveEagerlyFacet(model)) {
            for(int i=0; i<componentFactories.size(); i++) {
                if(componentFactories.get(i) instanceof CollectionContentsAsUnresolvedPanelFactory) {
                    return i;
                }
            }
        }
        for(int i=0; i<componentFactories.size(); i++) {
            if(componentFactories.get(i) instanceof CollectionContentsAsAjaxTablePanelFactory) {
                return i;
            }
        }
        return 0;
    }

    private static boolean hasResolveEagerlyFacet(IModel<?> model) {
        if(!(model instanceof EntityCollectionModel)) {
            return false;
        }
        final EntityCollectionModel entityCollectionModel = (EntityCollectionModel) model;
        if(!entityCollectionModel.isParented()) {
            return false;
        }

        final OneToManyAssociation collection = 
                entityCollectionModel.getCollectionMemento().getCollection();
        RenderFacet resolveFacet = collection.getFacet(RenderFacet.class);
        return resolveFacet != null && resolveFacet.value() == Type.EAGERLY;
    }


}
