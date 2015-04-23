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

import static org.hamcrest.CoreMatchers.is;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaPosition;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.CssClassFaBehavior;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.Model;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

class CssMenuItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ID_MENU_LINK = "menuLink";

    public static class Builder {
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
            Ensure.ensureThatArg(link.getId(), is(ID_MENU_LINK));
            cssMenuItem.setLink(link);
            return this;
        }

        public <T extends Page> Builder enabled(final String disabledReasonIfAny) {
            cssMenuItem.setEnabled(disabledReasonIfAny == null);
            cssMenuItem.setDisabledReason(disabledReasonIfAny);
            return this;
        }

        public Builder describedAs(String descriptionIfAny) {
            cssMenuItem.setDescription(descriptionIfAny);
            return this;
        }

        public Builder returnsBlobOrClob(boolean blobOrClob) {
            cssMenuItem.setReturnsBlobOrClob(blobOrClob);
            return this;
        }

        public Builder prototyping(boolean prototype) {
            cssMenuItem.setPrototyping(prototype);
            return this;
        }

        public Builder separator(boolean separator) {
            cssMenuItem.setSeparator(separator);
            return this;
        }

        public Builder withActionIdentifier(String actionIdentifier) {
            cssMenuItem.setActionIdentifier(actionIdentifier);
            return this;
        }

        public Builder withCssClass(String cssClass) {
            cssMenuItem.setCssClass(cssClass);
            return this;
        }

        public Builder withCssClassFa(String cssClassFa) {
            cssMenuItem.setCssClassFa(cssClassFa);
            return this;
        }

        public Builder withCssClassFaPosition(final CssClassFaPosition position) {
            cssMenuItem.setCssClassFaPosition(position);
            return this;
        }

        /**
         * Returns the built {@link CssMenuItem}, associating with {@link #parent(CssMenuItem) parent} (if specified).
         */
        public CssMenuItem build() {
            if (cssMenuItem.parent != null) {
                cssMenuItem.parent.subMenuItems.add(cssMenuItem);
            }
            return cssMenuItem;
        }

    }

    private final String name;
    private final List<CssMenuItem> subMenuItems = Lists.newArrayList();
    private CssMenuItem parent;

    private AbstractLink link;
    private boolean enabled = true; // unless disabled
    private String disabledReason;
    private boolean blobOrClob = false; // unless set otherwise
    private boolean prototype = false; // unless set otherwise
    private boolean separator = false; // unless set otherwise

    static final String ID_MENU_LABEL = "menuLabel";

    static final String ID_SUB_MENU_ITEMS = "subMenuItems";

    private String actionIdentifier;
    private String cssClass;
    private String cssClassFa;
    private CssClassFaPosition cssClassFaPosition;

    private String description;

    /**
     * Factory method returning {@link Builder builder}.
     */
    public static Builder newMenuItem(final String name) {
        return new Builder(name);
    }

    public void setActionIdentifier(String actionIdentifier) {
        this.actionIdentifier = actionIdentifier;
    }

    public void setPrototyping(boolean prototype) {
        this.prototype = prototype;
    }

    public boolean isPrototyping() {
        return prototype;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    /**
     * Requires a separator before it
     *
     * @return
     */
    public boolean isSeparator() {
        return separator;
    }

    private CssMenuItem(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean hasParent() {
        return parent != null;
    }

    private void setParent(final CssMenuItem parent) {
        this.parent = parent;
    }

    public Builder newSubMenuItem(final String name) {
        return CssMenuItem.newMenuItem(name).parent(this);
    }

    public List<CssMenuItem> getSubMenuItems() {
        return Collections.unmodifiableList(subMenuItems);
    }

    public boolean hasSubMenuItems() {
        return subMenuItems.size() > 0;
    }

    public AbstractLink getLink() {
        return link;
    }

    private void setLink(final AbstractLink link) {
        this.link = link;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setReturnsBlobOrClob(boolean blobOrClob) {
        this.blobOrClob = blobOrClob;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClassFa(String cssClassFa) {
        this.cssClassFa = cssClassFa;
    }

    public String getCssClassFa() {
        return cssClassFa;
    }

    public void setCssClassFaPosition(final CssClassFaPosition position) {
        this.cssClassFaPosition = position;
    }

    public CssClassFaPosition getCssClassFaPosition() {
        return cssClassFaPosition;
    }

    /**
     * Only populated if not {@link #isEnabled() enabled}.
     */
    public String getDisabledReason() {
        return disabledReason;
    }

    public void setDisabledReason(final String disabledReason) {
        this.disabledReason = disabledReason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // //////////////////////////////////////////////////////////////
    // To add submenu items
    // //////////////////////////////////////////////////////////////

    /**
     * Creates a {@link Builder} for a submenu item invoking an action on the provided {@link ObjectAdapterMemento
     * target adapter}.
     */
    public Builder newSubMenuItem(
            final ObjectAdapterMemento targetAdapterMemento,
            final ObjectAction objectAction,
            final boolean separator,
            final ActionLinkFactory actionLinkFactory) {

        // check visibility
        final AuthenticationSession session = getAuthenticationSession();
        final ObjectAdapter adapter = targetAdapterMemento.getObjectAdapter(ConcurrencyChecking.CHECK);
        final Consent visibility = objectAction.isVisible(session, adapter, ActionModel.WHERE_FOR_ACTION_INVOCATION);
        if (visibility.isVetoed()) {
            return null;
        }

        // build the link
        final LinkAndLabel linkAndLabel = actionLinkFactory.newLink(
                targetAdapterMemento, objectAction, PageAbstract.ID_MENU_LINK
                );
        if (linkAndLabel == null) {
            // can only get a null if invisible, so this should not happen given guard above
            return null;
        }
        final AbstractLink link = linkAndLabel.getLink();
        final String actionLabel = linkAndLabel.getLabel();

        final Consent usability = objectAction.isUsable(session, adapter, ActionModel.WHERE_FOR_ACTION_INVOCATION);
        final String reasonDisabledIfAny = usability.getReason();

        final DescribedAsFacet describedAsFacet = objectAction.getFacet(DescribedAsFacet.class);
        final String descriptionIfAny = describedAsFacet != null ? describedAsFacet.value() : null;

        Builder builder = newSubMenuItem(actionLabel)
                .link(link)
                .describedAs(descriptionIfAny)
                .enabled(reasonDisabledIfAny)
                .returnsBlobOrClob(ObjectAction.Utils.returnsBlobOrClob(objectAction))
                .prototyping(ObjectAction.Utils.isExplorationOrPrototype(objectAction))
                .separator(separator)
                .withActionIdentifier(ObjectAction.Utils.actionIdentifierFor(objectAction))
                .withCssClass(ObjectAction.Utils.cssClassFor(objectAction, adapter))
                .withCssClassFa(ObjectAction.Utils.cssClassFaFor(objectAction))
                .withCssClassFaPosition(ObjectAction.Utils.cssClassFaPositionFor(objectAction));

        return builder;
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

            if (this.description != null) {
                label.add(new AttributeModifier("title", Model.of(description)));
            }
            if (this.blobOrClob) {
                link.add(new CssClassAppender("noVeil"));
            }
            if (this.prototype) {
                link.add(new CssClassAppender("prototype"));
            }

            if (this.cssClass != null) {
                link.add(new CssClassAppender(this.cssClass));
            }
            link.add(new CssClassAppender(this.actionIdentifier));

            String cssClassFa = getCssClassFa();
            if (!Strings.isNullOrEmpty(cssClassFa)) {
                label.add(new CssClassFaBehavior(cssClassFa, getCssClassFaPosition()));
            }

            if (!this.isEnabled()) {
                link.add(new AttributeModifier("title", Model.of(this.getDisabledReason())));
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
            label.add(new AttributeModifier("title", Model.of(this.getDisabledReason())));
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

    // //////////////////////////////////////////////////////////////
    // dependencies
    // //////////////////////////////////////////////////////////////

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

}
