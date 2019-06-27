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

package org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions;

import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.metamodel.facets.members.cssclassfa.CssClassFaPosition;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.ListOfLinksModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.CssClassFaBehavior;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLink;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;

public class AdditionalLinksPanel extends PanelAbstract<ListOfLinksModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ADDITIONAL_LINK_LIST = "additionalLinkList";
    private static final String ID_ADDITIONAL_LINK_ITEM = "additionalLinkItem";
    private static final String ID_ADDITIONAL_LINK_TITLE = "additionalLinkTitle";

    public static final String ID_ADDITIONAL_LINK = "additionalLink";

    public enum Style {
        INLINE_LIST {
            @Override
            AdditionalLinksPanel newPanel(String id, List<LinkAndLabel> links) {
                return new AdditionalLinksAsListInlinePanel(id, links);
            }
        },
        DROPDOWN {
            @Override
            AdditionalLinksPanel newPanel(String id, List<LinkAndLabel> links) {
                return new AdditionalLinksAsDropDownPanel(id, links);
            }
        };
        abstract AdditionalLinksPanel newPanel(String id, List<LinkAndLabel> links);
    }

    public static AdditionalLinksPanel addAdditionalLinks(
            final MarkupContainer markupContainer,
            final String id,
            final List<LinkAndLabel> links,
            final Style style) {
        if(links.isEmpty()) {
            Components.permanentlyHide(markupContainer, id);
            return null;
        }

        final AdditionalLinksPanel additionalLinksPanel =  style.newPanel(id, links);
        markupContainer.addOrReplace(additionalLinksPanel);
        return additionalLinksPanel;
    }


    protected AdditionalLinksPanel(final String id, final List<LinkAndLabel> linksDoNotUseDirectlyInsteadUseOfListOfLinksModel) {
        super(id, new ListOfLinksModel(linksDoNotUseDirectlyInsteadUseOfListOfLinksModel));

        final List<LinkAndLabel> linkAndLabels = getModel().getObject();

        final WebMarkupContainer container = new WebMarkupContainer(ID_ADDITIONAL_LINK_LIST) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                for (LinkAndLabel linkAndLabel : linkAndLabels) {
                    final AbstractLink link = linkAndLabel.getLink();
                    if(link.isVisible()) {
                        return true;
                    }
                }
                return false;
            }
        };
        addOrReplace(container);

        container.setOutputMarkupId(true);

        setOutputMarkupId(true);

        final ListView<LinkAndLabel> listView = new ListView<LinkAndLabel>(ID_ADDITIONAL_LINK_ITEM, linkAndLabels) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<LinkAndLabel> item) {
                final LinkAndLabel linkAndLabel = item.getModelObject();

                final AbstractLink link = linkAndLabel.getLink();
                final Model<String> tooltipModel = link instanceof ActionLink
                        ? new Model<String>() {
                            private static final long serialVersionUID = 1L;
                            @Override
                            public String getObject() {
                                final ActionLink actionLink = (ActionLink) link;
                                final String reasonDisabledIfAny = actionLink.getReasonDisabledIfAny();
                                return first(reasonDisabledIfAny, linkAndLabel.getDescriptionIfAny());
                            }
                        } 
                        : Model.of(linkAndLabel.getDescriptionIfAny());
                
                Tooltips.addTooltip(link, tooltipModel);

                final Label viewTitleLabel = new Label(ID_ADDITIONAL_LINK_TITLE, linkAndLabel.getLabel());
                if(linkAndLabel.isBlobOrClob()) {
                    link.add(new CssClassAppender("noVeil"));
                }
                if(linkAndLabel.isPrototype()) {
                    link.add(new CssClassAppender("prototype"));
                }
                link.add(new CssClassAppender(linkAndLabel.getActionIdentifier()));

                SemanticsOf semantics = linkAndLabel.getSemantics();
                if (linkAndLabel.getParameters().isNoParameters() &&
                    (link instanceof ActionLink ? ((ActionLink)link).getReasonDisabledIfAny() : null) == null
                    ) {
                    addConfirmationDialogIfAreYouSureSemantics(link, semantics);
                }

                final String cssClass = linkAndLabel.getCssClass();
                CssClassAppender.appendCssClassTo(link, cssClass);

                link.addOrReplace(viewTitleLabel);

                final String cssClassFa = linkAndLabel.getCssClassFa();
                if (_Strings.isNullOrEmpty(cssClassFa)) {
                    viewTitleLabel.add(new CssClassAppender("menuLinkSpacer"));
                } else {
                    final CssClassFaPosition position = linkAndLabel.getCssClassFaPosition();
                    viewTitleLabel.add(new CssClassFaBehavior(cssClassFa, position));
                }

                item.addOrReplace(link);
            }
        };

        container.addOrReplace(listView);
    }

    private static String first(String... str) {
        for (String s : str) {
            if(s != null) return s;
        }
        return null;
    }

}
