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

package org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.dropdown;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;

import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.facets.members.render.RenderFacet;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.wicket.model.hints.IsisUiHintEvent;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.hints.UiHintPathSignificant;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.links.CollectionContentsLinksSelectorPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.unresolved.CollectionContentsAsUnresolvedPanelFactory;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.CssClassRemover;

/**
 * Provides a list of links for selecting other views that support
 * {@link org.apache.isis.viewer.wicket.ui.ComponentType#COLLECTION_CONTENTS} with a backing
 * {@link org.apache.isis.viewer.wicket.model.models.EntityCollectionModel}.
 */
public class CollectionContentsSelectorDropdownPanel
        extends PanelAbstract<EntityCollectionModel> implements UiHintPathSignificant /*,  CollectionCountProvider*/ {

    private static final long serialVersionUID = 1L;

    private static final String INVISIBLE_CLASS = "link-selector-panel-invisible";
//    private static final int MAX_NUM_UNDERLYING_VIEWS = 10;

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

    private ComponentFactory selectedComponentFactory;
//    private Component selectedComponent;

//    /**
//     * May be <tt>null</tt>, depending upon the model implementation.
//     */
//    protected WebMarkupContainer additionalLinks;

    public CollectionContentsSelectorDropdownPanel(final String id, final EntityCollectionModel model, final ComponentFactory factory) {
        super(id, model);
        this.componentType = factory.getComponentType();
    }

    /**
     * Build UI only after added to parent.
     */
    public void onInitialize() {
        super.onInitialize();
        ComponentFactory componentFactory = getComponentFactoryRegistry().findComponentFactoryElseFailFast(getComponentType(), getModel());
        addUnderlyingViews(getModel(), componentFactory);
    }


    private void addUnderlyingViews(final EntityCollectionModel model, final ComponentFactory factory) {
        final List<ComponentFactory> componentFactories = findOtherComponentFactories(model, factory);

        final int selected = honourViewHintElseDefault(componentFactories, model);

        final CollectionContentsSelectorDropdownPanel selectorPanel = this;

        // create all, hide the one not selected
//        final Component[] underlyingViews = new Component[MAX_NUM_UNDERLYING_VIEWS];
//        int i = 0;
//        final EntityCollectionModel emptyModel = model.asDummy();
//        for (ComponentFactory componentFactory : componentFactories) {
//            final String underlyingId = underlyingIdPrefix + "-" + i;
//
//            Component underlyingView = componentFactory.createComponent(underlyingId,i==selected? model: emptyModel);
//            underlyingViews[i++] = underlyingView;
//            selectorPanel.addOrReplace(underlyingView);
//        }

//        // hide any unused placeholders
//        while(i<MAX_NUM_UNDERLYING_VIEWS) {
//            String underlyingId = underlyingIdPrefix + "-" + i;
//            permanentlyHide(underlyingId);
//            i++;
//        }

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
                            CollectionContentsSelectorDropdownPanel linksSelectorPanel = CollectionContentsSelectorDropdownPanel.this;
                            linksSelectorPanel.setViewHintAndBroadcast(underlyingViewNum, target);

//                            final EntityCollectionModel dummyModel = model.asDummy();
//                            for(int i=0; i<MAX_NUM_UNDERLYING_VIEWS; i++) {
//                                final Component component = underlyingViews[i];
//                                if(component == null) {
//                                    continue;
//                                }
//                                final boolean isSelected = i == underlyingViewNum;
//                                applyCssVisibility(component, isSelected);
//                                component.setDefaultModel(isSelected? model: dummyModel);
//                            }

                            selectorPanel.selectedComponentFactory = componentFactory;
//                            selectorPanel.selectedComponent = underlyingViews[underlyingViewNum];
//                            selectorPanel.onSelect(target);
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

//        for(i=0; i<MAX_NUM_UNDERLYING_VIEWS; i++) {
//            Component component = underlyingViews[i];
//            if(component != null) {
//                if(i != selected) {
//                    component.add(new CssClassAppender(INVISIBLE_CLASS));
//                } else {
//                    selectedComponent = component;
//                }
//            }
//        }
    }



    protected void setViewHintAndBroadcast(int viewNum, AjaxRequestTarget target) {
        final UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) {
            return;
        }
        uiHintContainer.setHint(CollectionContentsSelectorDropdownPanel.this, UIHINT_VIEW, ""+viewNum);
        send(getPage(), Broadcast.EXACT, new IsisUiHintEvent(uiHintContainer, target));
    }

//    /**
//     * Iterates up the component hierarchy looking for a parent
//     * {@link org.apache.isis.viewer.wicket.ui.components.collection.CollectionPanel}, and if so adds to ajax target so that it'll
//     * be repainted.
//     *
//     * <p>
//     * Yeah, agreed, it's a little bit hacky doing it this way, because it bakes
//     * in knowledge that this component is created, somehow, by a parent {@link org.apache.isis.viewer.wicket.ui.components.collection.CollectionPanel}.
//     * Perhaps it could be refactored to use a more general purpose observer pattern?
//     *
//     * <p>
//     * In fact, I've since discovered that Wicket has an event bus, which is used by the
//     * {@link org.apache.isis.viewer.wicket.model.hints.UiHintContainer hinting mechanism}.  So this ought to be relatively easy to do.
//     */
//    public void onSelect(AjaxRequestTarget target) {
//        Component component = this;
//        while(component != null) {
//            if(component instanceof CollectionPanel) {
//                CollectionPanel collectionPanel = (CollectionPanel) component;
//                boolean hasCount = collectionPanel.hasCount();
//                if(hasCount) {
//                    collectionPanel.updateLabel(target);
//                }
////                if(additionalLinks != null) {
////                    applyCssVisibility(additionalLinks, hasCount);
////                }
//                return;
//            }
//            component = component.getParent();
//        }
//    }


    protected static void applyCssVisibility(final Component component, final boolean visible) {
        if(component == null) {
            return;
        }
        AttributeModifier modifier = visible ? new CssClassRemover(INVISIBLE_CLASS) : new CssClassAppender(INVISIBLE_CLASS);
        component.add(modifier);
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


    /**
     * return the index of {@link org.apache.isis.viewer.wicket.ui.components.collectioncontents.unresolved.CollectionContentsAsUnresolvedPanelFactory unresolved panel} if present and not eager loading;
     * else the index of {@link org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory ajax table} if present,
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

    private List<ComponentFactory> findOtherComponentFactories(final EntityCollectionModel model, final ComponentFactory ignoreFactory) {
        final List<ComponentFactory> componentFactories = getComponentFactoryRegistry().findComponentFactories(componentType, model);
        ArrayList<ComponentFactory> otherFactories = Lists.newArrayList(Collections2.filter(componentFactories, new Predicate<ComponentFactory>() {
            @Override
            public boolean apply(final ComponentFactory input) {
                return input != ignoreFactory && input.getClass() != CollectionContentsLinksSelectorPanelFactory.class;
            }
        }));
        return ordered(otherFactories);
    }

    protected List<ComponentFactory> ordered(List<ComponentFactory> componentFactories) {
        return orderAjaxTableToEnd(componentFactories);
    }


    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        PanelUtil.renderHead(response, CollectionContentsSelectorDropdownPanel.class);
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


//    @Override
//    public Integer getCount() {
//        if(selectedComponent instanceof CollectionCountProvider) {
//            final CollectionCountProvider collectionCountProvider = (CollectionCountProvider) selectedComponent;
//            return collectionCountProvider.getCount();
//        } else {
//            return null;
//        }
//    }

}
