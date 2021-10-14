/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Decorators;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

class CssMenuItem
implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String ID_MENU_LINK = "menuLink";
    private static final String ID_MENU_LABEL = "menuLabel";
    private static final String ID_SUB_MENU_ITEMS = "subMenuItems";

    public static CssMenuItem newMenuItem(final String name) {
        return new CssMenuItem(name, MenuItemType.ACTION_OR_SUBMENU_CONTAINER);
    }

    @Getter private final String name;

    @Getter @Setter private LinkAndLabel linkAndLabel;

    private CssMenuItem(final String name, final MenuItemType itemType) {
        this.name = name;
        this.itemType = itemType;
    }

//    protected CssMenuItem newSubMenuItem(final String name, final MenuItemType itemType) {
//        val subMenuItem = newMenuItem(name);
//        subMenuItem.setParent(this);
//        return subMenuItem;
//    }

    private final List<CssMenuItem> subMenuItems = _Lists.newArrayList();
    protected void addSubMenuItem(final CssMenuItem cssMenuItem) {
        subMenuItems.add(cssMenuItem);
    }
    public List<CssMenuItem> getSubMenuItems() {
        return Collections.unmodifiableList(subMenuItems);
    }
    /**
     * @param menuItems we assume these have the correct parent already set
     */
    public void replaceSubMenuItems(final List<CssMenuItem> menuItems) {
        subMenuItems.clear();
        subMenuItems.addAll(menuItems);
    }
    public boolean hasSubMenuItems() {
        return subMenuItems.size() > 0;
    }

    // //////////////////////////////////////////////////////////////
    // Build wicket components from the menu item.
    // //////////////////////////////////////////////////////////////

    void addTo(final MarkupContainer markupContainer) {

        final Component menuItemComponent = addMenuItemComponentTo(markupContainer);
        addSubMenuItemComponentsIfAnyTo(markupContainer);

        addCssClassAttributesIfRequired(menuItemComponent);
    }

    private Component addMenuItemComponentTo(final MarkupContainer markupContainer) {

        val linkAndLabel = getLinkAndLabel();
        val actionMeta = getLinkAndLabel().getActionUiMetaModel();
        val actionLink = getLinkAndLabel().getUiComponent();

        val label = new Label(CssMenuItem.ID_MENU_LABEL, Model.of(this.getName()));

        if (actionLink != null) {

            // show link...
            markupContainer.add(actionLink);
            actionLink.add(label);

            linkAndLabel
            .getDescription()
            .ifPresent(describedAs->Tooltips.addTooltip(actionLink, describedAs));

            if (actionMeta.isBlobOrClob()) {
                actionLink.add(new CssClassAppender("noVeil"));
            }
            if (actionMeta.isPrototyping()) {
                actionLink.add(new CssClassAppender("prototype"));
            }

            if (actionMeta.getCssClass() != null) {
                actionLink.add(new CssClassAppender(actionMeta.getCssClass()));
            }
            actionLink.add(new CssClassAppender(actionMeta.getActionIdentifier()));

            val fontAwesome = getLinkAndLabel().getFontAwesomeUiModel();
            Decorators.getIcon().decorate(label, fontAwesome);

            actionMeta.getDisableUiModel().ifPresent(disableUiModel->{
                Decorators.getDisable().decorate(actionLink, disableUiModel);
            });


            // .. and hide label
            Components.permanentlyHide(markupContainer, CssMenuItem.ID_MENU_LABEL);
            return actionLink;
        } else {
            // hide link...
            Components.permanentlyHide(markupContainer, ID_MENU_LINK);
            // ... and show label, along with disabled reason

            actionMeta.getDisableUiModel().ifPresent(disableUiModel->{
                Tooltips.addTooltip(label, disableUiModel.getReason());
            });

            label.add(new AttributeModifier("class", Model.of("disabled")));

            markupContainer.add(label);

            return label;
        }
    }

    private void addSubMenuItemComponentsIfAnyTo(final MarkupContainer menuItemMarkup) {
        val subMenuItems = getSubMenuItems();
        if (subMenuItems.isEmpty()) {
            Components.permanentlyHide(menuItemMarkup, CssMenuItem.ID_SUB_MENU_ITEMS);
        } else {
            menuItemMarkup.add(
                    new CssSubMenuItemsPanel(CssMenuItem.ID_SUB_MENU_ITEMS, subMenuItems));
        }
    }

    private void addCssClassAttributesIfRequired(final Component linkComponent) {
        if (!hasSubMenuItems()) {
            return;
        }
        if (this.hasParent()) {
            linkComponent.add(new CssClassAppender("parent"));
        }
        else {
            linkComponent.add(new CssClassAppender("top-parent"));
        }
    }

    @Getter private CssMenuItem parent;
    protected void setParent(final CssMenuItem parent) {
        this.parent = parent;
        parent.addSubMenuItem(_Casts.uncheckedCast(this));
    }
    public boolean hasParent() {
        return parent != null;
    }

    // -- SUPPORT FOR SPECIAL MENU ITEMS

    public enum MenuItemType {
        SPACER,
        SECTION_LABEL,
        ACTION_OR_SUBMENU_CONTAINER;

        boolean isActionOrSubMenuContainer() {
            return this == ACTION_OR_SUBMENU_CONTAINER;
        }
    }

    @Getter
    private final MenuItemType itemType;

    public static CssMenuItem newSpacer() {
        return new CssMenuItem("---", MenuItemType.SPACER);
    }

    public static CssMenuItem newSectionLabel(final String named) {
        return new CssMenuItem(named, MenuItemType.SECTION_LABEL);
    }



}
