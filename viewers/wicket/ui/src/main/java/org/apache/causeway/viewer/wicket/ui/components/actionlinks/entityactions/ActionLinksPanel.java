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
package org.apache.causeway.viewer.wicket.ui.components.actionlinks.entityactions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.MarkupContainer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.viewer.commons.model.decorators.ActionDecorators.ActionStyle;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.ui.components.menuable.MenuablePanelAbstract;
import org.apache.causeway.viewer.wicket.ui.components.widgets.actionlink.ActionLink;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;
import org.apache.causeway.viewer.wicket.ui.util.WktLinks;

import lombok.RequiredArgsConstructor;

public class ActionLinksPanel
extends MenuablePanelAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_ADDITIONAL_LINK_LIST = "additionalLinkList";
    private static final String ID_ADDITIONAL_LINK_ITEM = "additionalLinkItem";
    private static final String ID_ADDITIONAL_LINK_TITLE = "additionalLinkTitle";

    @RequiredArgsConstructor
    public enum Style {
        INLINE_LIST(ActionStyle.BUTTON) {
            @Override
            public ActionLinksPanel newPanel(final String id, final Can<ActionLink> links) {
                return new ActionLinksAsButtonInlinePanel(id, links);
            }
        },
        DROPDOWN(ActionStyle.MENU_ITEM) {
            @Override
            public ActionLinksPanel newPanel(final String id, final Can<ActionLink> links) {
                return new ActionLinksAsDropDownPanel(id, links);
            }
        };
        abstract ActionLinksPanel newPanel(String id, Can<ActionLink> links);
        final ActionStyle actionStyle;                

    }

    /**
     * Permanently hides given markupContainer, if no links are given.
     */
    public static ActionLinksPanel addActionLinks(
            final MarkupContainer markupContainer,
            final String id,
            final Can<ActionModel> links,
            final Style style) {
        var panel = actionLinks(id, links, style);
        if(panel.isEmpty()) {
            WktComponents.permanentlyHide(markupContainer, id);
            return null;
        }
        return Wkt.add(markupContainer, panel.get());
    }
    
    public static Optional<ActionLinksPanel> actionLinks(
            final String id,
            final Can<ActionModel> links,
            final Style style) {
        if(links.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(style.newPanel(id, links.map(ActionLink::create)));
    }

    protected ActionLinksPanel(
            final String id,
            final Can<ActionLink> menuables,
            final Style style) {
        super(id, menuables);
        setOutputMarkupId(true);

        var container = Wkt.add(this, Wkt.containerWithVisibility(ID_ADDITIONAL_LINK_LIST,
                    this::hasAnyVisibleLink));

        Wkt.listViewAdd(container, ID_ADDITIONAL_LINK_ITEM, listOfLinkAndLabels(), item->{
            var linkAndLabel = item.getModelObject();
            item.addOrReplace(WktLinks
                    .asActionLink(item, ID_ADDITIONAL_LINK_TITLE, linkAndLabel, style.actionStyle));
            if (!linkAndLabel.isVisible()) {
                Wkt.cssAppend(item, "hidden");
            }
        });

        //refactoring hint: in CssSubMenuItemsPanel we use a RepeatingView instead
//        Wkt.repeatingViewAdd(container, ID_ADDITIONAL_LINK_ITEM, streamLinkAndLabels(),
//                (inner, menuable)->{
//                    WktLinks.asAdditionalLink(inner, ID_ADDITIONAL_LINK_TITLE, menuable);
//                });

    }

    protected final Stream<ActionLink> streamLinkAndLabels() {
        return menuablesModel().streamMenuables(ActionLink.class);
    }

    protected final List<ActionLink> listOfLinkAndLabels() {
        return streamLinkAndLabels().collect(Collectors.toList());
    }

    public final boolean hasAnyVisibleLink() {
        return streamLinkAndLabels().anyMatch(actionLink->actionLink.isVisible());
    }

}
