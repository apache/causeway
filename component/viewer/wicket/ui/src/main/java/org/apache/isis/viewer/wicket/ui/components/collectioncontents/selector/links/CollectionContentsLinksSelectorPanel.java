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

import com.google.common.collect.Lists;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.core.metamodel.facets.members.resolve.RenderFacet;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.collection.CollectionCountProvider;
import org.apache.isis.viewer.wicket.ui.components.collection.CollectionPanel;
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
public class CollectionContentsLinksSelectorPanel extends LinksSelectorPanelAbstract<EntityCollectionModel> implements CollectionCountProvider {

    private static final long serialVersionUID = 1L;

    public CollectionContentsLinksSelectorPanel(final String id, final EntityCollectionModel model, final ComponentFactory factory) {
        super(id, ComponentType.COLLECTION_CONTENTS.toString(), model, factory);
    }

    /* (non-Javadoc)
     * @see org.apache.isis.viewer.wicket.ui.selector.links.LinksSelectorPanelAbstract#onInitialize()
     */
    @Override
    public void onInitialize() {
        super.onInitialize();
        applyCssVisibility(additionalLinks, selectedComponent instanceof CollectionCountProvider);
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
        if(!hasRenderEagerlyFacet(model)) {
            for(int i=0; i<componentFactories.size(); i++) {
                if(componentFactories.get(i) instanceof CollectionContentsAsUnresolvedPanelFactory) {
                    return i;
                }
            }
        }
        int ajaxTableIdx = findAjaxTable(componentFactories);
        if(ajaxTableIdx>=0) {
            return ajaxTableIdx;
        }
        return 0;
    }

    @Override
    protected List<ComponentFactory> ordered(List<ComponentFactory> componentFactories) {
        return orderAjaxTableToEnd(componentFactories);
    }

    static List<ComponentFactory> orderAjaxTableToEnd(List<ComponentFactory> componentFactories) {
        int ajaxTableIdx = findAjaxTable(componentFactories);
        if(ajaxTableIdx>=0) {
            List<ComponentFactory> orderedFactories = Lists.newArrayList(componentFactories);
            ComponentFactory ajaxTableFactory = orderedFactories.remove(ajaxTableIdx);
            orderedFactories.add(ajaxTableFactory);
            return orderedFactories;
        } else {
            return componentFactories;
        }
    }
    
    private static int findAjaxTable(List<ComponentFactory> componentFactories) {
        for(int i=0; i<componentFactories.size(); i++) {
            if(componentFactories.get(i) instanceof CollectionContentsAsAjaxTablePanelFactory) {
                return i;
            }
        }
        return -1;
    }


    private static boolean hasRenderEagerlyFacet(IModel<?> model) {
        if(!(model instanceof EntityCollectionModel)) {
            return false;
        }
        final EntityCollectionModel entityCollectionModel = (EntityCollectionModel) model;
        if(!entityCollectionModel.isParented()) {
            return false;
        }

        final OneToManyAssociation collection = 
                entityCollectionModel.getCollectionMemento().getCollection();
        RenderFacet renderFacet = collection.getFacet(RenderFacet.class);
        return renderFacet != null && renderFacet.value() == Type.EAGERLY;
    }

    /**
     * Iterates up the component hierarchy looking for a parent
     * {@link CollectionPanel}, and if so adds to ajax target so that it'll
     * be repainted.
     * 
     * <p>
     * Yeah, agreed, it's a little bit hacky doing it this way, because it bakes
     * in knowledge that this component is created, somehow, by a parent {@link CollectionPanel}.
     * Perhaps it could be refactored to use a more general purpose observer pattern?
     * 
     * <p>
     * In fact, I've since discovered that Wicket has an event bus, which is used by the 
     * {@link UiHintContainer hinting mechanism}.  So this ought to be relatively easy to do.
     */
    protected void onSelect(AjaxRequestTarget target) {
        super.onSelect(target);
        Component component = this;
        while(component != null) {
            if(component instanceof CollectionPanel) {
                CollectionPanel collectionPanel = (CollectionPanel) component;
                boolean hasCount = collectionPanel.hasCount();
                if(hasCount) {
                    collectionPanel.updateLabel(target);
                }
                if(additionalLinks != null) {
                    applyCssVisibility(additionalLinks, hasCount);
                }
                return;
            }
            component = component.getParent();
        }
    }

    @Override
    public Integer getCount() {
        if(selectedComponent instanceof CollectionCountProvider) {
            final CollectionCountProvider collectionCountProvider = (CollectionCountProvider) selectedComponent;
            return collectionCountProvider.getCount();
        } else {
            return null;
        }
    }


}
