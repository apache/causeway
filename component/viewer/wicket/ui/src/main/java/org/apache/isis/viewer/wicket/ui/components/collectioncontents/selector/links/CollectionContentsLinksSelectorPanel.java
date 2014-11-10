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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.isis.viewer.wicket.model.hints.IsisEnvelopeEvent;
import org.apache.isis.viewer.wicket.model.hints.IsisUiHintEvent;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.hints.UiHintPathSignificant;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.collection.CollectionCountProvider;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.dropdown.CollectionContentsSelectorDropdownPanel;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.dropdown.CollectionContentsSelectorHelper;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.dropdown.HasSelectorDropdownPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.CssClassRemover;

/**
 * Provides a list of links for selecting other views that support
 * {@link ComponentType#COLLECTION_CONTENTS} with a backing
 * {@link EntityCollectionModel}.
 */
public class CollectionContentsLinksSelectorPanel
        extends PanelAbstract<EntityCollectionModel> implements UiHintPathSignificant,  CollectionCountProvider {

    private static final long serialVersionUID = 1L;

    private static final String INVISIBLE_CLASS = "link-selector-panel-invisible";
    private static final int MAX_NUM_UNDERLYING_VIEWS = 10;

//    private static final String ID_SELECTOR_DROPDOWN = "selectorDropdown";

    private static final String UIHINT_VIEW = "view";

    private final ComponentFactory ignoreFactory;

    private final ComponentType componentType;
    private final String underlyingIdPrefix;
    private final CollectionContentsSelectorHelper selectorHelper;

    private ComponentFactory selectedComponentFactory;
    private Component selectedComponent;

    private Component[] underlyingViews;
    private CollectionContentsSelectorDropdownPanel selectorDropdownPanel;

    public CollectionContentsLinksSelectorPanel(
            final String id,
            final EntityCollectionModel model,
            final ComponentFactory ignoreFactory) {
        super(id, model);
        this.ignoreFactory = ignoreFactory;
        this.underlyingIdPrefix = ComponentType.COLLECTION_CONTENTS.toString();
        this.componentType = ignoreFactory.getComponentType();
        selectorHelper = new CollectionContentsSelectorHelper(model, getComponentFactoryRegistry(), ignoreFactory);

    }

    /**
     * Build UI only after added to parent.
     */
    public void onInitialize() {
        super.onInitialize();
        addUnderlyingViews();
    }


    private void addUnderlyingViews() {
        final EntityCollectionModel model = getModel();

        final int selected = selectorHelper.honourViewHintElseDefault(getSelectorDropdownPanel());
        final List<ComponentFactory> componentFactories = selectorHelper.findOtherComponentFactories();

        // create all, hide the one not selected
        underlyingViews = new Component[MAX_NUM_UNDERLYING_VIEWS];
        int i = 0;
        final EntityCollectionModel emptyModel = model.asDummy();
        for (ComponentFactory componentFactory : componentFactories) {
            final String underlyingId = underlyingIdPrefix + "-" + i;

            Component underlyingView = componentFactory.createComponent(underlyingId,i==selected? model: emptyModel);
            underlyingViews[i++] = underlyingView;
            this.addOrReplace(underlyingView);
        }

        // hide any unused placeholders
        while(i<MAX_NUM_UNDERLYING_VIEWS) {
            String underlyingId = underlyingIdPrefix + "-" + i;
            permanentlyHide(underlyingId);
            i++;
        }

        // selector
//        if (componentFactories.size() <= 1) {
//            permanentlyHide(ID_SELECTOR_DROPDOWN);
//        } else {
//            final Model<ComponentFactory> componentFactoryModel = new Model<>();
//
//            this.selectedComponentFactory = componentFactories.get(selected);
//            componentFactoryModel.setObject(this.selectedComponentFactory);
//
//            selectorDropdownPanel = new CollectionContentsSelectorDropdownPanel(ID_SELECTOR_DROPDOWN, getModel(), ignoreFactory);
//
//            addOrReplace(selectorDropdownPanel);
//        }

        this.setOutputMarkupId(true);

        for(i=0; i<MAX_NUM_UNDERLYING_VIEWS; i++) {
            Component component = underlyingViews[i];
            if(component != null) {
                if(i != selected) {
                    component.add(new CssClassAppender(INVISIBLE_CLASS));
                } else {
                    selectedComponent = component;
                }
            }
        }
    }

    @Override
    public void onEvent(IEvent<?> event) {
        super.onEvent(event);

        final IsisUiHintEvent uiHintEvent = IsisEnvelopeEvent.openLetter(event, IsisUiHintEvent.class);
        if(uiHintEvent == null) {
            return;
        }
        final UiHintContainer uiHintContainer = uiHintEvent.getUiHintContainer();

        int underlyingViewNum = 0;
        String viewStr = uiHintContainer.getHint(this.getSelectorDropdownPanel(), UIHINT_VIEW);

        List<ComponentFactory> componentFactories = selectorHelper.findOtherComponentFactories();

        if(viewStr != null) {
            try {
                int view = Integer.parseInt(viewStr);
                if(view >= 0 && view < componentFactories.size()) {
                    underlyingViewNum = view;
                }
            } catch(NumberFormatException ex) {
                // ignore
            }
        }

        final EntityCollectionModel dummyModel = getModel().asDummy();
        for(int i=0; i<MAX_NUM_UNDERLYING_VIEWS; i++) {
            final Component component = underlyingViews[i];
            if(component == null) {
                continue;
            }
            final boolean isSelected = i == underlyingViewNum;
            applyCssVisibility(component, isSelected);
            component.setDefaultModel(isSelected? getModel(): dummyModel);
        }

        this.selectedComponentFactory = ignoreFactory;
        this.selectedComponent = underlyingViews[underlyingViewNum];


        final AjaxRequestTarget target = uiHintEvent.getTarget();
        if(target != null) {
            target.add(this, getSelectorDropdownPanel());
        }

    }


    protected static void applyCssVisibility(final Component component, final boolean visible) {
        if(component == null) {
            return;
        }
        AttributeModifier modifier = visible ? new CssClassRemover(INVISIBLE_CLASS) : new CssClassAppender(INVISIBLE_CLASS);
        component.add(modifier);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        PanelUtil.renderHead(response, CollectionContentsLinksSelectorPanel.class);
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

    /**
     * Searches up the component hierarchy looking for a parent that implements
     * {@link org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.dropdown.HasSelectorDropdownPanel}.
     * @return
     */
    private CollectionContentsSelectorDropdownPanel getSelectorDropdownPanel() {
        Component component = this;
        while(component != null) {
            if(component instanceof HasSelectorDropdownPanel) {
                final CollectionContentsSelectorDropdownPanel selectorDropdownPanel1 = ((HasSelectorDropdownPanel) component).getSelectorDropdownPanel();
                if(selectorDropdownPanel1 == null) {
                    throw new IllegalStateException("Found parent that implements HasSelectorDropdownPanel, but no SelectorDropdownPanel available (is null)");

                }
                return selectorDropdownPanel1;
            }
            component = component.getParent();
        }
        throw new IllegalStateException("Could not locate parent that implements HasSelectorDropdownPanel");
    }

}
