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

package org.apache.isis.viewer.wicket.ui.pages.accmngt;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.BootstrapJavascriptBehavior;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.components.widgets.navbar.BrandLogo;
import org.apache.isis.viewer.wicket.ui.components.widgets.navbar.BrandName;
import org.apache.isis.viewer.wicket.ui.components.widgets.navbar.Placement;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionStackTracePanel;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;

/**
 * Boilerplate, pick up our HTML and CSS.
 */
public class AccountManagementPageAbstract extends WebPage {

    private static final long serialVersionUID = 1L;

    private static final String ID_PAGE_TITLE = "pageTitle";
    private static final String ID_APPLICATION_NAME = "applicationName";

    private static final String ID_EXCEPTION_STACK_TRACE = "exceptionStackTrace";

    /**
     * The name of a special cookie that is used as a temporary container for
     * stateless session scoped success feedback messages.
     */
    public static final String FEEDBACK_COOKIE_NAME = "isis.feedback.success";

    /**
     * {@link com.google.inject.Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @Named("applicationName")
    private String applicationName;

    /**
     * {@link com.google.inject.Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @Named("applicationCss")
    private String applicationCss;

    /**
     * {@link com.google.inject.Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @Named("applicationJs")
    private String applicationJs;

    /**
     * If set by {@link org.apache.isis.viewer.wicket.ui.pages.PageAbstract}.
     */
    protected static ExceptionModel getAndClearExceptionModelIfAny() {
        ExceptionModel exceptionModel = PageAbstract.EXCEPTION.get();
        PageAbstract.EXCEPTION.remove();
        return exceptionModel;
    }

    protected AccountManagementPageAbstract(final PageParameters parameters, final ExceptionModel exceptionModel) {
        super(parameters);

        Class<? extends Page> pageClass = pageClassRegistry.getPageClass(PageType.SIGN_IN);
        BookmarkablePageLink<Void> signInLink = new BookmarkablePageLink<>("signInLink", pageClass);
        signInLink.setAutoEnable(true);
        add(signInLink);

        addPageTitle();
        addApplicationName(signInLink);

        if(exceptionModel != null) {
            add(new ExceptionStackTracePanel(ID_EXCEPTION_STACK_TRACE, exceptionModel));
        } else {
            add(new WebMarkupContainer(ID_EXCEPTION_STACK_TRACE).setVisible(false));
        }

        add(new HeaderResponseContainer("footerJS", "footerJS"));
        BootstrapJavascriptBehavior.addTo(this);
    }


    private MarkupContainer addPageTitle() {
        return add(new Label(ID_PAGE_TITLE, applicationName));
    }

    private void addApplicationName(MarkupContainer parent) {
        final Placement placement = Placement.SIGNIN;
        final BrandLogo brandLogo = new BrandLogo("brandLogo", placement);
        final BrandName brandName = new BrandName(ID_APPLICATION_NAME, placement);
        parent.add(brandName, brandLogo);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference())));

        if(applicationCss != null) {
            response.render(CssReferenceHeaderItem.forUrl(applicationCss));
        }
        if(applicationJs != null) {
            response.render(JavaScriptReferenceHeaderItem.forUrl(applicationJs));
        }
    }


 
    // ///////////////////////////////////////////////////
    // System components
    // ///////////////////////////////////////////////////

    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    @javax.inject.Inject
    private PageClassRegistry pageClassRegistry;
}
