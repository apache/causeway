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
package org.apache.isis.viewer.wicket.ui.util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel.Placement;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLink;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class WktLinks {

    public <T extends Page> AbstractLink newBookmarkablePageLink(
            final String linkId, final PageParameters pageParameters, final Class<T> pageClass) {
        return new BookmarkablePageLink<Void>(linkId, pageClass, pageParameters);
    }

    /**
     * For rendering {@link LinkAndLabel} within additional-link panels or drop-downs.
     */
    public AbstractLink asAdditionalLink(final String titleId, final LinkAndLabel linkAndLabel) {

        val link = linkAndLabel.getUiComponent();
        val action = linkAndLabel.getManagedAction().getAction();

        Tooltips.addTooltip(link, link instanceof ActionLink
                    && _Strings.isNotEmpty(((ActionLink) link).getReasonDisabledIfAny())
                ? ((ActionLink) link).getReasonDisabledIfAny()
                : linkAndLabel.getDescription().orElse(null));

        if(ObjectAction.Util.returnsBlobOrClob(action)) {
            Wkt.cssAppend(link, "noVeil");
        }
        if(action.isPrototype()) {
            Wkt.cssAppend(link, "prototype");
        }
        Wkt.cssAppend(link, linkAndLabel.getFeatureIdentifier());

        if (action.getSemantics().isAreYouSure()) {
            if(action.getParameterCount()==0) {
                val hasDisabledReason = link instanceof ActionLink
                        ? _Strings.isNotEmpty(((ActionLink)link).getReasonDisabledIfAny())
                        : false;
                if (!hasDisabledReason) {
                    val translationService = linkAndLabel.getAction().getMetaModelContext()
                            .getTranslationService();
                    val confirmUiModel = ConfirmUiModel
                            .ofAreYouSure(translationService, Placement.BOTTOM);
                    Decorators.getConfirm().decorate(link, confirmUiModel);
                }
            }
            // ensure links receive the danger style
            // don't care if expressed twice
            Decorators.getDanger().decorate(link);
        }

        linkAndLabel
        .getAdditionalCssClass()
        .ifPresent(cssClass->Wkt.cssAppend(link, cssClass));

        val viewTitleLabel = Wkt.labelAdd(link, titleId,
                linkAndLabel::getFriendlyName);

        val fontAwesome = linkAndLabel.getFontAwesomeUiModel();
        Decorators.getIcon().decorate(viewTitleLabel, fontAwesome);
        Decorators.getMissingIcon().decorate(viewTitleLabel, fontAwesome);

        return link;
    }

    public static <T, R extends MarkupContainer> R listItemAsDropdownLink(
            final @NonNull ListItem<T> item,
            final @NonNull R container,
            final @NonNull String titleId, final @NonNull Function<T, IModel<String>> titleProvider,
            final @NonNull String iconId, final @Nullable Function<T, IModel<String>> iconProvider,
            final @Nullable BiFunction<T, Label, IModel<String>> cssFactory) {

        val t = item.getModelObject();

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
