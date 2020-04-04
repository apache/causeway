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
import java.util.List;
import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.Model;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.menuitem.MenuItemUiModelAbstract;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.CssClassFaBehavior;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

class CssMenuItem 
extends MenuItemUiModelAbstract<CssMenuItem> 
implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String ID_MENU_LINK = "menuLink";
    private static final String ID_MENU_LABEL = "menuLabel";
    private static final String ID_SUB_MENU_ITEMS = "subMenuItems";

    private static class Builder {
        private final CssMenuItem cssMenuItem;

        private Builder(final String name) {
            cssMenuItem = new CssMenuItem(name);
        }

        public Builder parent(final CssMenuItem parent) {
            cssMenuItem.setParent(parent);
            return this;
        }

        public <T extends Page> Builder link() {
            final AbstractLink link = new SubmitLink(ID_MENU_LINK);
            return link(link);
        }

        public <T extends Page> Builder link(final AbstractLink link) {
            assert link.getId().equals(ID_MENU_LINK);
            cssMenuItem.setLink(link);
            return this;
        }

        public <T extends Page> Builder enabled(final String disabledReasonIfAny) {
            cssMenuItem.setEnabled(disabledReasonIfAny == null);
            cssMenuItem.setDisabledReason(disabledReasonIfAny);
            return this;
        }

        /**
         * Returns the built {@link CssMenuItem}, associating with {@link #parent(CssMenuItem) parent} (if specified).
         */
        public CssMenuItem build() {
            if (cssMenuItem.hasParent()) {
                cssMenuItem.getParent().addSubMenuItem(cssMenuItem);
            }
            return cssMenuItem;
        }

    }

    @Getter @Setter(AccessLevel.PRIVATE) private AbstractLink link;
    
    /**
     * Factory method returning {@link Builder builder}.
     */
    public static CssMenuItem newMenuItem(final String name) {
        return new Builder(name).build();
    }

    private CssMenuItem(final String name) {
        super(name);
    }

    private Builder newSubMenuItemBuilder(final String name) {
        return new Builder(name).parent(this);
    }


    // //////////////////////////////////////////////////////////////
    // To add submenu items
    // //////////////////////////////////////////////////////////////

    /**
     * Optionally creates a sub-menu item invoking an action on the provided 
     * {@link ServiceAndAction action model}, based on visibility and usability.
     */
    Optional<CssMenuItem> addMenuItemFor(ServiceAndAction serviceAndAction) {

        final EntityModel targetEntityModel = serviceAndAction.serviceEntityModel;
        final ObjectAction objectAction = serviceAndAction.objectAction;
        final boolean requiresSeparator = serviceAndAction.isFirstSection;
        final ServiceActionLinkFactory actionLinkFactory = serviceAndAction.linkAndLabelFactory;

        val serviceAdapter = targetEntityModel.load();
        final ObjectSpecification serviceSpec = serviceAdapter.getSpecification();
        if (serviceSpec.isHidden()) {
            return Optional.empty();
        }

        // check visibility
        final Consent visibility = objectAction.isVisible(
                serviceAdapter,
                InteractionInitiatedBy.USER,
                ActionModel.WHERE_FOR_ACTION_INVOCATION);
        if (visibility.isVetoed()) {
            return Optional.empty();
        }

        // check usability
        final Consent usability = objectAction.isUsable(
                serviceAdapter,
                InteractionInitiatedBy.USER,
                ActionModel.WHERE_FOR_ACTION_INVOCATION
                );
        final String reasonDisabledIfAny = usability.getReason();

        final DescribedAsFacet describedAsFacet = objectAction.getFacet(DescribedAsFacet.class);
        final String descriptionIfAny = describedAsFacet != null ? describedAsFacet.value() : null;

        // build the link
        final LinkAndLabel linkAndLabel = actionLinkFactory.newLink(objectAction, PageAbstract.ID_MENU_LINK, null);
        if (linkAndLabel == null) {
            // can only get a null if invisible, so this should not happen given the visibility guard above
            return Optional.empty();
        }

        final AbstractLink link = linkAndLabel.getLink();
        final String actionLabel = serviceAndAction.serviceName != null ? serviceAndAction.serviceName : linkAndLabel.getLabel();

        val menutIem = (CssMenuItem) newSubMenuItemBuilder(actionLabel)
                .link(link)
                .enabled(reasonDisabledIfAny)
                .build()
                .setPrototyping(objectAction.isPrototype())
                .setRequiresSeparator(requiresSeparator)
                .setRequiresImmediateConfirmation(
                        ObjectAction.Util.isAreYouSureSemantics(objectAction) &&
                        ObjectAction.Util.isNoParameters(objectAction))
                .setBlobOrClob(ObjectAction.Util.returnsBlobOrClob(objectAction))
                .setDescription(descriptionIfAny)
                .setActionIdentifier(ObjectAction.Util.actionIdentifierFor(objectAction))
                .setCssClass(ObjectAction.Util.cssClassFor(objectAction, serviceAdapter))
                .setCssClassFa(ObjectAction.Util.cssClassFaFor(objectAction))
                .setCssClassFaPosition(ObjectAction.Util.cssClassFaPositionFor(objectAction));
        return Optional.of(menutIem);
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
        final AbstractLink link = getLink();
        final Label label = new Label(CssMenuItem.ID_MENU_LABEL, Model.of(this.getName()));

        if (link != null) {

            // show link...
            markupContainer.add(link);
            link.add(label);

            if (getDescription() != null) {
                Tooltips.addTooltip(link, Model.of(getDescription()));
            }
            if (isBlobOrClob()) {
                link.add(new CssClassAppender("noVeil"));
            }
            if (isPrototyping()) {
                link.add(new CssClassAppender("prototype"));
            }

            if (getCssClass() != null) {
                link.add(new CssClassAppender(getCssClass()));
            }
            link.add(new CssClassAppender(getActionIdentifier()));

            String cssClassFa = getCssClassFa();
            if (!_Strings.isNullOrEmpty(cssClassFa)) {
                label.add(new CssClassFaBehavior(cssClassFa, getCssClassFaPosition()));
            }

            if (!this.isEnabled()) {
                Tooltips.addTooltip(link, this.getDisabledReason());
                link.add(new CssClassAppender("disabled"));

                link.setEnabled(false);
            }

            // .. and hide label
            Components.permanentlyHide(markupContainer, CssMenuItem.ID_MENU_LABEL);
            return link;
        }
        else {
            // hide link...
            Components.permanentlyHide(markupContainer, ID_MENU_LINK);
            // ... and show label, along with disabled reason
            Tooltips.addTooltip(link, this.getDisabledReason());
            label.add(new AttributeModifier("class", Model.of("disabled")));

            markupContainer.add(label);

            return label;
        }
    }

    private void addSubMenuItemComponentsIfAnyTo(final MarkupContainer menuItemMarkup) {
        final List<CssMenuItem> subMenuItems = getSubMenuItems();
        if (subMenuItems.isEmpty()) {
            Components.permanentlyHide(menuItemMarkup, CssMenuItem.ID_SUB_MENU_ITEMS);
        }
        else {
            menuItemMarkup.add(new CssSubMenuItemsPanel(CssMenuItem.ID_SUB_MENU_ITEMS, subMenuItems));
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

}
