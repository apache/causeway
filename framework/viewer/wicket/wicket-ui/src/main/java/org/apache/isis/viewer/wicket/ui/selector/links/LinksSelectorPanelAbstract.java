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

package org.apache.isis.viewer.wicket.ui.selector.links;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.annotation.Resolve.Type;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.facets.members.resolve.ResolveFacet;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.util.Strings;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.unresolved.CollectionContentsAsUnresolvedPanelFactory;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

public abstract class LinksSelectorPanelAbstract<T extends IModel<?>> extends PanelAbstract<T> {

    private static final long serialVersionUID = 1L;

    private static final String ID_VIEWS = "views";

    private static final String ID_VIEW_LIST = "viewList";
    private static final String ID_VIEW_LINK = "viewLink";
    private static final String ID_VIEW_ITEM = "viewItem";
    private static final String ID_VIEW_TITLE = "viewTitle";

    private final ComponentType componentType;

    private ComponentFactory selectedComponentFactory;

    public LinksSelectorPanelAbstract(final String id, final String underlyingId, final T model, final ComponentFactory factory) {
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
            
            final WebMarkupContainer container = new WebMarkupContainer(ID_VIEW_LIST);
            
            views.addOrReplace(container);
            views.setOutputMarkupId(true);
            
            this.setOutputMarkupId(true);

            final ListView<ComponentFactory> listView = new ListView<ComponentFactory>(ID_VIEW_ITEM, componentFactories) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void populateItem(ListItem<ComponentFactory> item) {
                    final ComponentFactory componentFactory = item.getModelObject();
                    
                    final AbstractLink link = new AjaxLink<Void>(ID_VIEW_LINK) {

                                private static final long serialVersionUID = 1L;

                                @Override
                                public void onClick(AjaxRequestTarget target) {
                                    LinksSelectorPanelAbstract<T> selectorPanel = LinksSelectorPanelAbstract.this;
                                    selectorPanel.select(underlyingId, model, componentFactory);
                                    target.add(selectorPanel, views);
                                }
                            };
                            
                    String name = nameFor(componentFactory);
                    Label viewTitleLabel = new Label(ID_VIEW_TITLE, name);
                    viewTitleLabel.add(new CssClassAppender(StringUtils.toLowerDashed(name)));
                    link.add(viewTitleLabel);
                    item.add(link);
                    LinksSelectorPanelAbstract<T> selectorPanel = LinksSelectorPanelAbstract.this;
                    link.setEnabled(selectorPanel.selectedComponentFactory != componentFactory);
                }

                private String nameFor(final ComponentFactory componentFactory) {
                    return componentFactory instanceof CollectionContentsAsUnresolvedPanelFactory ? "hide" : componentFactory.getName();
                }
            };
            container.add(listView);
            
            addOrReplace(views);
        } else {
            permanentlyHide(ID_VIEWS);
        }
        select(underlyingId, model, selectedComponentFactory);
    }

    private void select(final String underlyingId, final T model, final ComponentFactory selectedComponentFactory) {
        addOrReplace(selectedComponentFactory.createComponent(underlyingId, model));
        this.selectedComponentFactory = selectedComponentFactory;
    }

    private static Predicate<ComponentFactory> determineInitialFactory(IModel<?> model) {
        return hasResolveEagerlyFacet(model) 
                ? new Predicate<ComponentFactory>() {
                    @Override
                    public boolean apply(ComponentFactory input) {
                        return !(input instanceof CollectionContentsAsUnresolvedPanelFactory);
                    }
                }
                : Predicates.<ComponentFactory>alwaysTrue();
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
        ResolveFacet resolveFacet = collection.getFacet(ResolveFacet.class);
        return resolveFacet != null && resolveFacet.value() == Type.EAGERLY;
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
        renderHead(response, LinksSelectorPanelAbstract.class);
    }

}
