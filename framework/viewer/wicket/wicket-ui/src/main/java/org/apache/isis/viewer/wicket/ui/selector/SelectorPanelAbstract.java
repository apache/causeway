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

package org.apache.isis.viewer.wicket.ui.selector;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.facets.members.commonlyused.CommonlyUsedFacet;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.unresolved.CollectionContentsAsUnresolvedFactory;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public abstract class SelectorPanelAbstract<T extends IModel<?>> extends PanelAbstract<T> {

    private static final long serialVersionUID = 1L;

    private static final String ID_VIEWS = "views";
    private static final String ID_VIEWS_DROP_DOWN = "viewsDropDown";

    private final ComponentType componentType;

    public SelectorPanelAbstract(final String id, final String underlyingId, final T model, final ComponentFactory factory) {
        super(id, model);

        componentType = factory.getComponentType();

        addUnderlyingViews(underlyingId, model, factory);
    }

    private void addUnderlyingViews(final String underlyingId, final T model, final ComponentFactory factory) {
        final List<ComponentFactory> componentFactories = findOtherComponentFactories(model, factory);

        final ComponentFactory selectedComponentFactory = Iterables.find(componentFactories, determineInitialFactory(model));
        if (componentFactories.size() > 1) {
            final Model<ComponentFactory> componentFactoryModel = new Model<ComponentFactory>();
            
            componentFactoryModel.setObject(selectedComponentFactory);

            final WebMarkupContainer views = new WebMarkupContainer(ID_VIEWS);
            final DropDownChoiceComponentFactory viewsDropDown = new DropDownChoiceComponentFactory(ID_VIEWS_DROP_DOWN, componentFactoryModel, componentFactories, this, underlyingId, model);
            views.addOrReplace(viewsDropDown);
            addOrReplace(views);
        } else {
            permanentlyHide(ID_VIEWS);
        }
        addOrReplace(selectedComponentFactory.createComponent(underlyingId, model));
    }
    
    private static Predicate<ComponentFactory> determineInitialFactory(IModel<?> model) {
        return isCommonlyUsed(model) 
                ? new Predicate<ComponentFactory>() {
                    @Override
                    public boolean apply(@Nullable ComponentFactory input) {
                        return !(input instanceof CollectionContentsAsUnresolvedFactory);
                    }
                }
                : Predicates.<ComponentFactory>alwaysTrue();
    }

    private static boolean isCommonlyUsed(IModel<?> model) {
        if(!(model instanceof EntityCollectionModel)) {
            return false;
        }
        final EntityCollectionModel entityCollectionModel = (EntityCollectionModel) model;
        if(!entityCollectionModel.isParented()) {
            return false;
        }

        final OneToManyAssociation collection = 
                entityCollectionModel.getCollectionMemento().getCollection();
        return collection.containsDoOpFacet(CommonlyUsedFacet.class);
    }

    private List<ComponentFactory> findOtherComponentFactories(final T model, final ComponentFactory ignoreFactory) {
        final List<ComponentFactory> componentFactories = getComponentFactoryRegistry().findComponentFactories(componentType, model);
        return Lists.newArrayList(Collections2.filter(componentFactories, new Predicate<ComponentFactory>() {
            @Override
            public boolean apply(final ComponentFactory input) {
                return input != ignoreFactory;
            }
        }));
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        renderHead(response, SelectorPanelAbstract.class);
    }

}
