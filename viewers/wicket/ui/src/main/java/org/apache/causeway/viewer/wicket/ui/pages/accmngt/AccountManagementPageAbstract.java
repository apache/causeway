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
package org.apache.causeway.viewer.wicket.ui.pages.accmngt;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.viewer.commons.applib.services.branding.BrandingUiService;
import org.apache.causeway.viewer.commons.model.error.ExceptionModel;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.ui.components.widgets.navbar.BrandLogo;
import org.apache.causeway.viewer.wicket.ui.components.widgets.navbar.BrandName;
import org.apache.causeway.viewer.wicket.ui.errors.ExceptionStackTracePanel;
import org.apache.causeway.viewer.wicket.ui.pages.PageAbstract;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.WebPageBase;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.BootstrapJavascriptBehavior;
import de.agilecoders.wicket.core.markup.html.references.BootstrapJavaScriptReference;

/**
 * Boilerplate, pick up our HTML and CSS.
 */
public class AccountManagementPageAbstract extends WebPageBase {

    private static final long serialVersionUID = 1L;

    private static final String ID_PAGE_TITLE = "pageTitle";
    private static final String ID_APPLICATION_NAME = "applicationName";
    private static final String ID_EXCEPTION_STACK_TRACE = "exceptionStackTrace";

    /**
     * If set by {@link org.apache.causeway.viewer.wicket.ui.pages.PageAbstract}.
     */
    protected static ExceptionModel getAndClearExceptionModelIfAny() {
        ExceptionModel exceptionModel = PageAbstract.EXCEPTION.get();
        PageAbstract.EXCEPTION.remove();
        return exceptionModel;
    }

    protected AccountManagementPageAbstract(
            final PageParameters parameters,
            final ExceptionModel exceptionModel) {

        super(parameters);

        Class<? extends Page> pageClass = getPageClassRegistry().getPageClass(PageType.SIGN_IN);
        BookmarkablePageLink<Void> signInLink = new BookmarkablePageLink<>("signInLink", pageClass);
        signInLink.setAutoEnable(true);
        add(signInLink);

        addPageTitle();
        addApplicationName(signInLink);

        if(shouldDisplayException(exceptionModel)) {
            var pageClassRegistry = super.getServiceRegistry().lookupServiceElseFail(PageClassRegistry.class);
            add(new ExceptionStackTracePanel(pageClassRegistry, ID_EXCEPTION_STACK_TRACE, exceptionModel));
        } else {
            add(new WebMarkupContainer(ID_EXCEPTION_STACK_TRACE).setVisible(false));
        }

        add(new HeaderResponseContainer("footerJS", "footerJS"));
        BootstrapJavascriptBehavior.addTo(this);
    }

    private static boolean shouldDisplayException(ExceptionModel exceptionModel) {
        if (exceptionModel == null) {
            return false;
        }
        var exceptionModelMainMessage = exceptionModel.getMainMessage();
        if (exceptionModelMainMessage == null) {
            return false;
        }
        return suppressedExceptionMessages.stream()
                    .noneMatch(exceptionModelMainMessage::contains);
    }

    private final static List<String> suppressedExceptionMessages = Arrays.asList("Requested page is no longer available.");

    private void addPageTitle() {
        var applicationName = getApplicationSettings().name();
        Wkt.labelAdd(this, ID_PAGE_TITLE, applicationName);
    }

    private void addApplicationName(final MarkupContainer parent) {
        var branding = super.getMetaModelContext()
                .lookupServiceElseFail(BrandingUiService.class)
                .getSignInBranding();
        var brandLogo = new BrandLogo("brandLogo", branding);
        var brandName = new BrandName(ID_APPLICATION_NAME, branding);
        parent.add(brandName, brandLogo);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference())));
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(
                BootstrapJavaScriptReference.instance())));

        getWicketViewerSettings().css()
        .ifPresent(css -> response.render(CssReferenceHeaderItem.forUrl(css)));

        getWicketViewerSettings().js()
        .ifPresent(js -> response.render(JavaScriptReferenceHeaderItem.forUrl(js)));
    }

}
