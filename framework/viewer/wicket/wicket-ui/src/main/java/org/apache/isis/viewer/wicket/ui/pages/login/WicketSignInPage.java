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

package org.apache.isis.viewer.wicket.ui.pages.login;

import org.apache.wicket.authroles.authentication.pages.SignInPage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.viewer.wicket.ui.app.cssrenderer.ApplicationCssRenderer;

/**
 * Boilerplate, pick up our HTML and CSS.
 */
public final class WicketSignInPage extends SignInPage {
    public WicketSignInPage() {
    }

    public WicketSignInPage(final PageParameters parameters) {
    }

    /**
     * Renders the application-supplied CSS, if any.
     */
    @Override
    public void renderHead(final HtmlHeaderContainer container) {
        super.renderHead(container);
        final ApplicationCssRenderer applicationCssRenderer = getApplicationCssRenderer();
        applicationCssRenderer.renderApplicationCss(container);
    }

    protected ApplicationCssRenderer getApplicationCssRenderer() {
        return (ApplicationCssRenderer) getApplication();
    }

}