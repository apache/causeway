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

package org.apache.isis.viewer.wicket.viewer.registries.pages;

import com.google.inject.Singleton;

import org.apache.isis.viewer.wicket.ui.pages.PageClassList;
import org.apache.isis.viewer.wicket.ui.pages.PageRegistrySpi;
import org.apache.isis.viewer.wicket.ui.pages.PageType;
import org.apache.isis.viewer.wicket.ui.pages.about.AboutPage;
import org.apache.isis.viewer.wicket.ui.pages.action.ActionPage;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.pages.home.HomePage;
import org.apache.isis.viewer.wicket.ui.pages.login.WicketSignInPage;

/**
 * Default implementation of {@link PageClassList}, specifying the default pages
 * for each of the {@link PageType}s.
 */
@Singleton
public class PageClassListDefault implements PageClassList {

    @Override
    public void registerPages(final PageRegistrySpi pageRegistry) {
        pageRegistry.registerPage(PageType.SIGN_IN, WicketSignInPage.class);
        //pageRegistry.registerPage(PageType.ABOUT, AboutPage.class);
        pageRegistry.registerPage(PageType.ENTITY, EntityPage.class);
        pageRegistry.registerPage(PageType.HOME, HomePage.class);
        pageRegistry.registerPage(PageType.ACTION, ActionPage.class);
    }
}
