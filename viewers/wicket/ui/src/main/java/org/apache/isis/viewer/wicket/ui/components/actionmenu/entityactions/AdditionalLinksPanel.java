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
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel.Placement;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.ListOfLinksModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLink;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Decorators;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;

import lombok.val;

public class AdditionalLinksPanel
extends PanelAbstract<List<LinkAndLabel>, ListOfLinksModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ADDITIONAL_LINK_LIST = "additionalLinkList";
    private static final String ID_ADDITIONAL_LINK_ITEM = "additionalLinkItem";
    private static final String ID_ADDITIONAL_LINK_TITLE = "additionalLinkTitle";

    public static final String ID_ADDITIONAL_LINK = "additionalLink";

    public enum Style {
        INLINE_LIST {
            @Override
            AdditionalLinksPanel newPanel(final String id, final Can<LinkAndLabel> links) {
                return new AdditionalLinksAsListInlinePanel(id, links);
            }
        },
        DROPDOWN {
            @Override
            AdditionalLinksPanel newPanel(final String id, final Can<LinkAndLabel> links) {
                return new AdditionalLinksAsDropDownPanel(id, links);
            }
        };
        abstract AdditionalLinksPanel newPanel(String id, Can<LinkAndLabel> links);
    }

    public static AdditionalLinksPanel addAdditionalLinks(
            final MarkupContainer markupContainer,
            final String id,
            final Can<LinkAndLabel> links,
            final Style style) {
        if(links.isEmpty()) {
            Components.permanentlyHide(markupContainer, id);
            return null;
        }

        final AdditionalLinksPanel additionalLinksPanel =  style.newPanel(id, links);
        markupContainer.addOrReplace(additionalLinksPanel);
        return additionalLinksPanel;
    }

    protected AdditionalLinksPanel(
            final String id,
            final Can<LinkAndLabel> linksDoNotUseDirectlyInsteadUseOfListOfLinksModel) {

        super(id, new ListOfLinksModel(linksDoNotUseDirectlyInsteadUseOfListOfLinksModel));


        final WebMarkupContainer container = new WebMarkupContainer(ID_ADDITIONAL_LINK_LIST) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return AdditionalLinksPanel.this.getModel().hasAnyVisibleLink();
            }
        };
        addOrReplace(container);

        container.setOutputMarkupId(true);

        setOutputMarkupId(true);

        final ListView<LinkAndLabel> listView =
                new ListView<LinkAndLabel>(ID_ADDITIONAL_LINK_ITEM, getModel()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(final ListItem<LinkAndLabel> item) {
                val linkAndLabel = item.getModelObject();
                val actionMeta = linkAndLabel.getActionUiMetaModel();
                val link = linkAndLabel.getUiComponent();
                final Model<String> tooltipModel = link instanceof ActionLink
                        ? new Model<String>() {
                            private static final long serialVersionUID = 1L;
                            @Override
                            public String getObject() {
                                return firstNonNull(
                                        ((ActionLink) link).getReasonDisabledIfAny(),
                                        actionMeta.getDescription());
                            }
                        }
                        : Model.of(actionMeta.getDescription());

                Tooltips.addTooltip(link, tooltipModel.getObject());

                val viewTitleLabel = new Label(ID_ADDITIONAL_LINK_TITLE, actionMeta.getLabel());
                if(actionMeta.isBlobOrClob()) {
                    link.add(new CssClassAppender("noVeil"));
                }
                if(actionMeta.isPrototyping()) {
                    link.add(new CssClassAppender("prototype"));
                }
                link.add(new CssClassAppender(actionMeta.getActionIdentifier()));

                if (actionMeta.getSemantics().isAreYouSure()) {
                    if(actionMeta.getParameters().isNoParameters()) {
                        val hasDisabledReason = link instanceof ActionLink
                                ? _Strings.isNotEmpty(((ActionLink)link).getReasonDisabledIfAny())
                                : false;
                        if (!hasDisabledReason) {
                            val confirmUiModel = ConfirmUiModel.ofAreYouSure(getTranslationService(), Placement.BOTTOM);
                            Decorators.getConfirm().decorate(link, confirmUiModel);
                        }
                    }
                    // ensure links receive the danger style
                    // don't care if expressed twice
                    Decorators.getDanger().decorate(link);
                }

                val cssClass = actionMeta.getCssClass();
                CssClassAppender.appendCssClassTo(link, cssClass);

                link.addOrReplace(viewTitleLabel);

                val fontAwesome = linkAndLabel.getFontAwesomeUiModel();
                Decorators.getIcon().decorate(viewTitleLabel, fontAwesome);
                Decorators.getMissingIcon().decorate(viewTitleLabel, fontAwesome);

                item.addOrReplace(link);
            }

        };

        container.addOrReplace(listView);
    }

    private static String firstNonNull(final String... str) {
        for (String s : str) {
            if(s != null) return s;
        }
        return null;
    }

}
