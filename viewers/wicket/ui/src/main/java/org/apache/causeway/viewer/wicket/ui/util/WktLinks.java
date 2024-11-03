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
package org.apache.causeway.viewer.wicket.ui.util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.springframework.lang.Nullable;

import org.apache.causeway.viewer.commons.model.decorators.ActionDecorators.ActionDecorationModel;
import org.apache.causeway.viewer.commons.model.decorators.ActionDecorators.ActionStyle;
import org.apache.causeway.viewer.wicket.ui.components.widgets.actionlink.ActionLink;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class WktLinks {

    public <T extends Page> AbstractLink newBookmarkablePageLink(
            final String linkId, final PageParameters pageParameters, final Class<T> pageClass) {
        return new BookmarkablePageLink<Void>(linkId, pageClass, pageParameters);
    }

    /**
     * For rendering action links within a button panel or drop-down panel.
     */
    public AbstractLink asActionLink(
            final Component tooltipReceiver,
            final String titleId,
            final ActionLink link,
            final ActionStyle actionStyle) {
        
        var actionLabel = Wkt.labelAdd(link, titleId,
                link::getFriendlyName);
        
        WktDecorators.decorateActionLink(
                link, tooltipReceiver, actionLabel,
                ActionDecorationModel.builder(link)
                    .actionStyle(actionStyle)
                    .build());

        return link;
    }

    public static <T, R extends MarkupContainer> R listItemAsDropdownLink(
            final @NonNull ListItem<T> item,
            final @NonNull R container,
            final @NonNull String titleId, final @NonNull Function<T, IModel<String>> titleProvider,
            final @NonNull String iconId, final @Nullable Function<T, IModel<String>> iconProvider,
            final @Nullable BiFunction<T, Label, IModel<String>> cssFactory) {

        var t = item.getModelObject();

        // add title and icon to the link

        Wkt.labelAdd(container, titleId, titleProvider.apply(t));

        final Label viewItemIcon = Wkt.labelAdd(container, iconId, Optional.ofNullable(iconProvider)
            .map(iconProv->iconProv.apply(t))
            .orElseGet(()->Model.of("")));

        Optional.ofNullable(cssFactory)
            .map(cssFact->cssFact.apply(t, viewItemIcon))
            .ifPresent(cssModel->Wkt.cssAppend(viewItemIcon, cssModel));

        return container;
    }}
