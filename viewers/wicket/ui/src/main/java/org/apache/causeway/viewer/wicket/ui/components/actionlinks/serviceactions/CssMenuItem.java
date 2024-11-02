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
package org.apache.causeway.viewer.wicket.ui.components.actionlinks.serviceactions;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.Model;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.viewer.commons.model.decorators.ActionDecorators.ActionDecorationModel;
import org.apache.causeway.viewer.commons.model.decorators.ActionDecorators.ActionStyle;
import org.apache.causeway.viewer.wicket.model.links.Menuable;
import org.apache.causeway.viewer.wicket.ui.components.widgets.actionlink.ActionLink;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;
import org.apache.causeway.viewer.wicket.ui.util.WktDecorators;

import lombok.Getter;
import lombok.experimental.Accessors;

class CssMenuItem
implements Menuable {

    private static final long serialVersionUID = 1L;
    private static final String ID_MENU_LINK = "menuLink";
    private static final String ID_MENU_LABEL = "menuLabel";
    private static final String ID_SUB_MENU_ITEMS = "subMenuItems";

    public static CssMenuItem newMenuItemWithLink(final String name, final ActionLink actionLink) {
        return new CssMenuItem(name, Menuable.Kind.LINK, actionLink);
    }

    public static CssMenuItem newMenuItemWithSubmenu(final String name) {
        return new CssMenuItem(name, Menuable.Kind.SUBMENU);
    }

    //refactoring hint: perhaps use Menuable.SectionSeparator instead
    public static CssMenuItem newSpacer() {
        return new CssMenuItem("---", Menuable.Kind.SECTION_SEPARATOR);
    }

    //refactoring hint: perhaps use Menuable.SectionLabel instead
    public static CssMenuItem newSectionLabel(final String named) {
        return new CssMenuItem(named, Menuable.Kind.SECTION_LABEL);
    }

    @Getter private final String name;
    /**
     * only available for kind LINK, otherwise null
     */
    private final ActionLink actionLink;
    public ActionLink actionLinkElseFail() {
        if(actionLink==null) throw new NullPointerException("this menu item has no action link");
        return actionLink;
    }

    private CssMenuItem(final String name, final Menuable.Kind menuableKind) {
        this(name, menuableKind, null);
    }

    private CssMenuItem(final String name, final Menuable.Kind menuableKind,
            @Nullable final ActionLink actionLink) {
        this.name = name;
        this.menuableKind = menuableKind;
        this.actionLink = actionLink;
    }

    private final List<CssMenuItem> subMenuItems = _Lists.newArrayList();
    protected final void addSubMenuItem(final CssMenuItem cssMenuItem) {
        subMenuItems.add(cssMenuItem);
    }
    public Can<CssMenuItem> getSubMenuItems() {
        return Can.ofCollection(subMenuItems);
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

        var label = Wkt.labelAdd(markupContainer, CssMenuItem.ID_MENU_LABEL, this::getName);

        if (actionLink!=null) {

            // show link...
            markupContainer.add(actionLink);

            WktDecorators.decorateMenuAction(
                    actionLink, actionLink, label,
                    ActionDecorationModel.builder(actionLinkElseFail())
                        .actionStyle(ActionStyle.MENU_ITEM)
                        .build());

            // .. and hide label
            WktComponents.permanentlyHide(markupContainer, CssMenuItem.ID_MENU_LABEL);
            return actionLink;
        } else {
            // hide link...
            WktComponents.permanentlyHide(markupContainer, ID_MENU_LINK);
            label.add(new AttributeModifier("class", Model.of("disabled")));
            return label;
        }
    }

    private void addSubMenuItemComponentsIfAnyTo(final MarkupContainer menuItemMarkup) {
        var subMenuItems = getSubMenuItems();
        if (subMenuItems.isEmpty()) {
            WktComponents.permanentlyHide(menuItemMarkup, CssMenuItem.ID_SUB_MENU_ITEMS);
        } else {
            menuItemMarkup.add(
                    new CssSubMenuItemsPanel(CssMenuItem.ID_SUB_MENU_ITEMS, subMenuItems));
        }
    }

    private void addCssClassAttributesIfRequired(final Component linkComponent) {
        if (!hasSubMenuItems()) {
            return;
        }
        Wkt.cssAppend(linkComponent, this.hasParent() ? "parent" : "top-parent");
    }

    @Getter private CssMenuItem parent;
    protected void setParent(final CssMenuItem parent) {
        this.parent = parent;
        parent.addSubMenuItem(_Casts.uncheckedCast(this));
    }
    public boolean hasParent() {
        return parent != null;
    }

    @Getter(onMethod_={@Override}) @Accessors(fluent=true)
    private final Menuable.Kind menuableKind;

}
