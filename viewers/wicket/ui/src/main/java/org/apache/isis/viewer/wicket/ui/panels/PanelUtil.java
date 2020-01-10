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
package org.apache.isis.viewer.wicket.ui.panels;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.config.messages.MessageRegistry;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationConfig;

public final class PanelUtil {

    private PanelUtil(){}

    /**
     * The contribution to the header performed implicitly by {@link PanelAbstract}.
     */
    public static void renderHead(final IHeaderResponse response, final Class<?> cls) {
        final CssResourceReference cssResourceReference = cssResourceReferenceFor(cls);
        if(cssResourceReference == null) {
            return;
        }
        final CssReferenceHeaderItem forReference = CssHeaderItem.forReference(cssResourceReference);
        response.render(forReference);
    }

    public static CssResourceReference cssResourceReferenceFor(final Class<?> cls) {
        return cssResourceReferenceFor(cls, null);
    }

    public static CssResourceReference cssResourceReferenceFor(final Class<?> cls, final String suffix) {
        final String url = cssFor(cls, suffix);
        if(url == null) {
            return null;
        }
        return new CssResourceReference(cls, url);
    }

    private static String cssFor(final Class<?> cls, String suffix) {
        if(cls == null) {
            return null;
        }
        String simpleName = cls.getSimpleName();
        if(_Strings.isNullOrEmpty(simpleName)) {
            return null; // eg inner classes
        }
        String string;
        if (suffix != null) {
            string = ("-" + suffix);
        } else {
            string = "";
        }
        return simpleName + string + ".css";
    }

    public static void disableBeforeReenableOnComplete(
            final AjaxRequestAttributes attributes, final Component ajaxButtonOrLink) {
        attributes.getAjaxCallListeners().add(new AjaxCallListener()
                .onBefore("$('#" + ajaxButtonOrLink.getMarkupId() + "').prop('disabled',true);")
                .onComplete("$('#" + ajaxButtonOrLink.getMarkupId() + "').prop('disabled',false);"));
    }

    public static void addConfirmationDialogIfAreYouSureSemantics(
            TranslationService translationService,
            Component component,
            SemanticsOf semanticsOf) {

        if (!semanticsOf.isAreYouSure()) {
            return;
        }

        ConfirmationConfig confirmationConfig = new ConfirmationConfig();

        final String context = MessageRegistry.class.getName();
        final String areYouSure = translationService.translate(context, MessageRegistry.MSG_ARE_YOU_SURE);
        final String confirm = translationService.translate(context, MessageRegistry.MSG_CONFIRM);
        final String cancel = translationService.translate(context, MessageRegistry.MSG_CANCEL);

        confirmationConfig
        .withTitle(areYouSure)
        .withBtnOkLabel(confirm)
        .withBtnCancelLabel(cancel)
        .withPlacement(TooltipConfig.Placement.right)
        .withBtnOkClass("btn btn-danger")
        .withBtnCancelClass("btn btn-default");

        component.add(new ConfirmationBehavior(confirmationConfig));
    }
}
