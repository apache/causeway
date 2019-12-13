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

package org.apache.isis.viewer.wicket.ui.components.entity.selector.links;

import java.util.List;
import java.util.function.Predicate;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.commons.StringExtensions;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import lombok.val;

/**
 * Provides a list of links for selecting other views that support
 * {@link ComponentType#ENTITY} with a backing {@link EntityModel}.
 *
 * <p>
 *     TODO: this code could be simplified
 *     (pushed down common code here and for the CollectionsSelectorPanel in order to do so);
 *     haven't simplified this yet because currently there is only one view, so the markup
 *     rendered by this component 'collapses' to just show that underlying view.
 * </p>
 */
public class EntityLinksSelectorPanel extends PanelAbstract<EntityModel>  {


    private static final long serialVersionUID = 1L;

    private static final int MAX_NUM_UNDERLYING_VIEWS = 10;

    private static final String ID_ADDITIONAL_LINKS = "additionalLinks";

    private static final String ID_VIEWS = "views";
    private static final String ID_VIEW_LIST = "viewList";
    private static final String ID_VIEW_LINK = "viewLink";
    private static final String ID_VIEW_ITEM = "viewItem";
    private static final String ID_VIEW_ITEM_TITLE = "viewItemTitle";
    private static final String ID_VIEW_ITEM_ICON = "viewItemIcon";

    private static final String UIHINT_VIEW = "view";
    private static final String ID_VIEW_BUTTON_TITLE = "viewButtonTitle";
    private static final String ID_VIEW_BUTTON_ICON = "viewButtonIcon";

    private final ComponentType componentType;
    private final String underlyingIdPrefix;

    private ComponentFactory selectedComponentFactory;
    protected Component selectedComponent;


    public EntityLinksSelectorPanel(
            final String id,
            final EntityModel model,
            final ComponentFactory factory) {
        super(id, model);
        this.underlyingIdPrefix = ComponentType.ENTITY.toString();
        this.componentType = factory.getComponentType();
    }


    protected int determineInitialFactory(
            final List<ComponentFactory> componentFactories,
            final IModel<?> model) {
        return 0;
    }

    @Override
    public UiHintContainer getUiHintContainer() {
        // disables hinting by this component
        return null;
    }


    /**
     * Build UI only after added to parent.
     */
    @Override
    public void onInitialize() {
        super.onInitialize();
        ComponentFactory componentFactory = getComponentFactoryRegistry().findComponentFactoryElseFailFast(getComponentType(), getModel());
        addAdditionalLinks(getModel());
        addUnderlyingViews(underlyingIdPrefix, getModel(), componentFactory);
    }

    protected void addAdditionalLinks(final EntityModel model) {
        if(!(model instanceof LinksProvider)) {
            permanentlyHide(ID_ADDITIONAL_LINKS);
            return;
        }
        LinksProvider linksProvider = (LinksProvider) model;
        List<LinkAndLabel> links = linksProvider.getLinks();

        addAdditionalLinks(this, links);
    }

    protected void addAdditionalLinks(MarkupContainer markupContainer, List<LinkAndLabel> linkAndLabels) {
        if(linkAndLabels == null || linkAndLabels.isEmpty()) {
            Components.permanentlyHide(markupContainer, ID_ADDITIONAL_LINKS);
            return;
        }
        linkAndLabels = _Lists.newArrayList(linkAndLabels); // copy, to serialize any lazy evaluation

        AdditionalLinksPanel.addAdditionalLinks(
                markupContainer, ID_ADDITIONAL_LINKS,
                linkAndLabels,
                AdditionalLinksPanel.Style.INLINE_LIST);
    }

    private void addUnderlyingViews(final String underlyingIdPrefix, final EntityModel model, final ComponentFactory factory) {
        final List<ComponentFactory> componentFactories = findOtherComponentFactories(model, factory);

        final int selected = honourViewHintElseDefault(componentFactories, model);

        final EntityLinksSelectorPanel selectorPanel = this;

        // create all, hide the one not selected
        final Component[] underlyingViews = new Component[MAX_NUM_UNDERLYING_VIEWS];
        int i = 0;
        final EntityModel emptyModel = dummyOf(model);
        for (ComponentFactory componentFactory : componentFactories) {
            final String underlyingId = underlyingIdPrefix + "-" + i;

            Component underlyingView = componentFactory.createComponent(underlyingId,i==selected? model: emptyModel);
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
            final Model<ComponentFactory> componentFactoryModel = new Model<>();

            selectorPanel.selectedComponentFactory = componentFactories.get(selected);
            componentFactoryModel.setObject(selectorPanel.selectedComponentFactory);

            final WebMarkupContainer views = new WebMarkupContainer(ID_VIEWS);

            final Label viewButtonTitle = new Label(ID_VIEW_BUTTON_TITLE, "Hidden");
            views.addOrReplace(viewButtonTitle);

            final Label viewButtonIcon = new Label(ID_VIEW_BUTTON_ICON, "");
            views.addOrReplace(viewButtonIcon);

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
                            EntityLinksSelectorPanel linksSelectorPanel = EntityLinksSelectorPanel.this;
                            linksSelectorPanel.setViewHintAndBroadcast(underlyingViewNum, target);

                            final EntityModel dummyModel = dummyOf(model);
                            for(int i=0; i<MAX_NUM_UNDERLYING_VIEWS; i++) {
                                final Component component = underlyingViews[i];
                                if(component == null) {
                                    continue;
                                }
                                final boolean isSelected = i == underlyingViewNum;
                                PanelAbstract.setVisible(component, isSelected);
                                component.setDefaultModel(isSelected? model: dummyModel);
                            }

                            selectorPanel.selectedComponentFactory = componentFactory;
                            selectorPanel.selectedComponent = underlyingViews[underlyingViewNum];
                            selectorPanel.onSelect(target);
                            target.add(selectorPanel, views);
                        }

                        @Override
                        protected void onComponentTag(ComponentTag tag) {
                            super.onComponentTag(tag);
                            Buttons.fixDisabledState(this, tag);
                        }
                    };

                    IModel<String> title = nameFor(componentFactory);
                    Label viewItemTitleLabel = new Label(ID_VIEW_ITEM_TITLE, title);
                    link.add(viewItemTitleLabel);

                    Label viewItemIcon = new Label(ID_VIEW_ITEM_ICON, "");
                    link.add(viewItemIcon);

                    boolean isEnabled = componentFactory != selectorPanel.selectedComponentFactory;
                    if (!isEnabled) {
                        viewButtonTitle.setDefaultModel(title);
                        IModel<String> cssClass = cssClassFor(componentFactory, viewButtonIcon);
                        viewButtonIcon.add(AttributeModifier.replace("class", "ViewLinkItem " + cssClass.getObject()));
                        link.setVisible(false);
                    } else {
                        IModel<String> cssClass = cssClassFor(componentFactory, viewItemIcon);
                        viewItemIcon.add(new CssClassAppender(cssClass));
                    }

                    item.add(link);
                }

                private IModel<String> cssClassFor(final ComponentFactory componentFactory, Label viewIcon) {
                    IModel<String> cssClass = null;
                    if (componentFactory instanceof CollectionContentsAsFactory) {
                        CollectionContentsAsFactory collectionContentsAsFactory = (CollectionContentsAsFactory) componentFactory;
                        cssClass = collectionContentsAsFactory.getCssClass();
                        viewIcon.setDefaultModelObject("");
                        viewIcon.setEscapeModelStrings(true);
                    }
                    if (cssClass == null) {
                        String name = componentFactory.getName();
                        cssClass = Model.of(StringExtensions.asLowerDashed(name));
                        // Small hack: if there is no specific CSS class then we assume that background-image is used
                        // the span.ViewItemLink should have some content to show it
                        // FIX: find a way to do this with CSS (width and height don't seems to help)
                        viewIcon.setDefaultModelObject("&#160;&#160;&#160;&#160;&#160;");
                        viewIcon.setEscapeModelStrings(false);
                    }
                    return cssClass;
                }

                private IModel<String> nameFor(final ComponentFactory componentFactory) {
                    IModel<String> name = null;
                    if (componentFactory instanceof CollectionContentsAsFactory) {
                        CollectionContentsAsFactory collectionContentsAsFactory = (CollectionContentsAsFactory) componentFactory;
                        name = collectionContentsAsFactory.getTitleLabel();
                    }
                    if (name == null) {
                        name = Model.of(componentFactory.getName());
                    }
                    return name;
                }
            };
            container.add(listView);
            addOrReplace(views);
        }

        for(i=0; i<MAX_NUM_UNDERLYING_VIEWS; i++) {
            Component component = underlyingViews[i];
            if(component != null) {
                if(i != selected) {
                    super.setVisible(component, /*visible*/ false);
                } else {
                    selectedComponent = component;
                }
            }
        }
    }



    protected void setViewHintAndBroadcast(int viewNum, AjaxRequestTarget target) {
        final UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) {
            return;
        }
        uiHintContainer.setHint(this, UIHINT_VIEW, ""+viewNum);
    }

    /**
     * Overrideable hook.
     */
    protected void onSelect(AjaxRequestTarget target) {
    }

    /**
     * Ask for a dummy (empty) {@link Model} to pass into those components that are rendered but will be
     * made invisible using CSS styling.
     */
    protected EntityModel dummyOf(EntityModel model) {
        return model;
    }

    protected int honourViewHintElseDefault(final List<ComponentFactory> componentFactories, final IModel<?> model) {
        // honour hints ...
        final UiHintContainer hintContainer = getUiHintContainer();
        if(hintContainer != null) {
            String viewStr = hintContainer.getHint(this, UIHINT_VIEW);
            if(viewStr != null) {
                try {
                    int view = Integer.parseInt(viewStr);
                    if(view >= 0 && view < componentFactories.size()) {
                        return view;
                    }
                } catch(NumberFormatException ex) {
                    // ignore
                }
            }
        }

        // ... else default
        int initialFactory = determineInitialFactory(componentFactories, model);
        if(hintContainer != null) {
            hintContainer.setHint(this, UIHINT_VIEW, ""+initialFactory);
            // don't broadcast (no AjaxRequestTarget, still configuring initial setup)
        }
        return initialFactory;
    }


    private List<ComponentFactory> findOtherComponentFactories(final EntityModel model, final ComponentFactory ignoreFactory) {
        final List<ComponentFactory> componentFactories = getComponentFactoryRegistry().findComponentFactories(componentType, model);
        val otherFactories = _Lists.filter(componentFactories, new Predicate<ComponentFactory>() {
            @Override
            public boolean test(final ComponentFactory input) {
                return input != ignoreFactory;
            }
        });
        return ordered(otherFactories);
    }

    protected List<ComponentFactory> ordered(List<ComponentFactory> otherFactories) {
        return otherFactories;
    }



}
