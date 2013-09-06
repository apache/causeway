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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.additionallinks.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.unresolved.CollectionContentsAsUnresolvedPanelFactory;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

public abstract class LinksSelectorPanelAbstract<T extends IModel<?>> extends PanelAbstract<T> {

    private static final long serialVersionUID = 1L;

    private static final String INVISIBLE_CLASS = "link-selector-panel-invisible";
    private static final int MAX_NUM_UNDERLYING_VIEWS = 10;

    private static final String ID_ADDITIONAL_LINKS = "additionalLinks";
    public static final String ID_ADDITIONAL_LINK = "additionalLink";

    private static final String ID_VIEWS = "views";
    private static final String ID_VIEW_LIST = "viewList";
    private static final String ID_VIEW_LINK = "viewLink";
    private static final String ID_VIEW_ITEM = "viewItem";
    private static final String ID_VIEW_TITLE = "viewTitle";

    private final ComponentType componentType;
    
    private ComponentFactory selectedComponentFactory;

    public LinksSelectorPanelAbstract(final String id, final String underlyingIdPrefix, final T model, final ComponentFactory factory) {
        super(id, model);

        componentType = factory.getComponentType();

        addAdditionalLinks(model);
        addUnderlyingViews(underlyingIdPrefix, model, factory);
    }

    protected void addAdditionalLinks(final T model) {
        if(!(model instanceof LinksProvider)) {
            permanentlyHide(ID_ADDITIONAL_LINKS);
            return;
        }
        LinksProvider linksProvider = (LinksProvider) model;
        List<LinkAndLabel> links = linksProvider.getLinks();
        
        addAdditionalLinks(this, links);
    }

    protected void addAdditionalLinks(MarkupContainer markupContainer, List<LinkAndLabel> links) {
        if(links == null || links.isEmpty()) {
            Components.permanentlyHide(markupContainer, ID_ADDITIONAL_LINKS);
            return;
        }
        links = Lists.newArrayList(links); // copy, to serialize any lazy evaluation
        
        final WebMarkupContainer views = new AdditionalLinksPanel(ID_ADDITIONAL_LINKS, links);
        markupContainer.addOrReplace(views);
    }

//    protected void addAdditionalLinks(MarkupContainer markupContainer, List<LinkAndLabel> links) {
//        final WebMarkupContainer views = new WebMarkupContainer(ID_ADDITIONAL_LINKS);
//        
//        final WebMarkupContainer container = new WebMarkupContainer(ID_ADDITIONAL_LINK_LIST);
//        
//        views.addOrReplace(container);
//        views.setOutputMarkupId(true);
//        
//        this.setOutputMarkupId(true);
//        
//        final ListView<LinkAndLabel> listView = new ListView<LinkAndLabel>(ID_ADDITIONAL_LINK_ITEM, links) {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            protected void populateItem(ListItem<LinkAndLabel> item) {
//                final LinkAndLabel linkAndLabel = item.getModelObject();
//                
//                final AbstractLink link = linkAndLabel.getLink();
//                        
//                Label viewTitleLabel = new Label(ID_ADDITIONAL_LINK_TITLE, linkAndLabel.getLabel());
//                String disabledReasonIfAny = linkAndLabel.getDisabledReasonIfAny();
//                if(disabledReasonIfAny != null) {
//                    viewTitleLabel.add(new AttributeAppender("title", disabledReasonIfAny));
//                }
//                viewTitleLabel.add(new CssClassAppender(StringUtils.toLowerDashed(linkAndLabel.getLabel())));
//                link.addOrReplace(viewTitleLabel);
//                item.addOrReplace(link);
//            }
//        };
//        container.addOrReplace(listView);
//        markupContainer.addOrReplace(views);
//    }

    private void addUnderlyingViews(final String underlyingIdPrefix, final T model, final ComponentFactory factory) {
        final List<ComponentFactory> componentFactories = findOtherComponentFactories(model, factory);

        final int selected = determineInitialFactory(componentFactories, model);

        final LinksSelectorPanelAbstract<T> selectorPanel = LinksSelectorPanelAbstract.this;
        
        // create all, hide the one not selected
        final Component[] underlyingViews = new Component[MAX_NUM_UNDERLYING_VIEWS];
        int i = 0;
        for (ComponentFactory componentFactory : componentFactories) {
            final String underlyingId = underlyingIdPrefix + "-" + i;
            
            final T emptyModel = dummyOf(model);
            Component underlyingView = componentFactory.createComponent(underlyingId, 
                   i==selected? model: emptyModel);
            underlyingViews[i++] = underlyingView;
            selectorPanel.addOrReplace(underlyingView);
        }

        // hide any unused placeholders
        while(i<MAX_NUM_UNDERLYING_VIEWS) {
            String underlyingId = underlyingIdPrefix + "-" + i;
            permanentlyHide(underlyingId);
            i++;
        }
        
        // selector
        if (componentFactories.size() <= 1) {
            permanentlyHide(ID_VIEWS);
        } else {
            final Model<ComponentFactory> componentFactoryModel = new Model<ComponentFactory>();
            
            selectorPanel.selectedComponentFactory = componentFactories.get(selected);
            componentFactoryModel.setObject(selectorPanel.selectedComponentFactory);

            final WebMarkupContainer views = new WebMarkupContainer(ID_VIEWS);
            
            final WebMarkupContainer container = new WebMarkupContainer(ID_VIEW_LIST);
            
            views.addOrReplace(container);
            views.setOutputMarkupId(true);
            
            this.setOutputMarkupId(true);
            
            final ListView<ComponentFactory> listView = new ListView<ComponentFactory>(ID_VIEW_ITEM, componentFactories) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void populateItem(ListItem<ComponentFactory> item) {
                    
                    final int underlyingViewNum = item.getIndex();
                    
                    final ComponentFactory componentFactory = item.getModelObject();
                    final AbstractLink link = new AjaxLink<Void>(ID_VIEW_LINK) {
                        private static final long serialVersionUID = 1L;
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            
                            for(int i=0; i<MAX_NUM_UNDERLYING_VIEWS; i++) {
                                final Component component = underlyingViews[i];
                                T dummyModel = dummyOf(model);
                                
                                if(component != null) {
                                    final boolean isSelected = i == underlyingViewNum;
                                    applyCssVisibility(component, isSelected);
                                    component.setDefaultModel(isSelected? model: dummyModel);
                                }
                            }
                            selectorPanel.selectedComponentFactory = componentFactory;
                            target.add(selectorPanel, views);
                        }
                    };
                    String name = nameFor(componentFactory);
                    Label viewTitleLabel = new Label(ID_VIEW_TITLE, name);
                    viewTitleLabel.add(new CssClassAppender(StringExtensions.asLowerDashed(name)));
                    link.add(viewTitleLabel);
                    item.add(link);
                    
                    link.setEnabled(componentFactory != selectorPanel.selectedComponentFactory);
                }

                private String nameFor(final ComponentFactory componentFactory) {
                    return componentFactory instanceof CollectionContentsAsUnresolvedPanelFactory ? "hide" : componentFactory.getName();
                }
            };
            container.add(listView);
            addOrReplace(views);
        }
        
        for(i=0; i<MAX_NUM_UNDERLYING_VIEWS; i++) {
            Component component = underlyingViews[i];
            if(component != null) {
                if(i != selected) {
                    component.add(new AttributeAppender("class", " " +
                    		INVISIBLE_CLASS));
                }
            }
        }
    }

    /**
     * Ask for a dummy (empty) {@link Model} to pass into those components that are rendered but will be
     * made invisible using CSS styling.
     */
    protected abstract T dummyOf(T model);

    private static void applyCssVisibility(final Component component, final boolean visible) {
        final AttributeModifier modifier =  
                visible 
                    ? new AttributeModifier("class", String.valueOf(component.getMarkupAttributes().get("class")).replaceFirst(INVISIBLE_CLASS, "")) 
                    : new AttributeAppender("class", " " +
                    		INVISIBLE_CLASS);
        component.add(modifier);
    }

    protected abstract int determineInitialFactory(final List<ComponentFactory> componentFactories, final IModel<?> model);

    private List<ComponentFactory> findOtherComponentFactories(final T model, final ComponentFactory ignoreFactory) {
        final List<ComponentFactory> componentFactories = getComponentFactoryRegistry().findComponentFactories(componentType, model);
        ArrayList<ComponentFactory> otherFactories = Lists.newArrayList(Collections2.filter(componentFactories, new Predicate<ComponentFactory>() {
            @Override
            public boolean apply(final ComponentFactory input) {
                return input != ignoreFactory;
            }
        }));
        return ordered(otherFactories);
    }

    protected List<ComponentFactory> ordered(List<ComponentFactory> otherFactories) {
        return otherFactories;
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        renderHead(response, LinksSelectorPanelAbstract.class);
    }

}
