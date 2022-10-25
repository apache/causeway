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
package org.apache.causeway.viewer.wicket.viewer.wicketapp;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.home.HomePage;

@ExtendWith(MockitoExtension.class)
class CausewayWicketApplication_Pages {

    @Mock PageClassRegistry mockPageClassRegistry;

    private CausewayWicketApplication application;

    @Test
    public void delegatesToPageClassRegistryToObtainPageTypes() {
        final PageType pageType = PageType.HOME;
        final Class<HomePage> expectedPageClass = HomePage.class;

        application = new CausewayWicketApplication() {
            private static final long serialVersionUID = 1L;

            @Override
            public PageClassRegistry getPageClassRegistry() {
                return mockPageClassRegistry;
            }

        };

        Mockito.when(mockPageClassRegistry.getPageClass(pageType))
        .thenReturn(_Casts.uncheckedCast(expectedPageClass));

        final Class<? extends Page> pageClass = application.getHomePage();
        assertThat(expectedPageClass.isAssignableFrom(pageClass), is(true));
    }

    @Test
    public void delegatesToPageClassRegistryToObtainPageTypes_ForSignIn() {

        final PageType pageType = PageType.SIGN_IN;
        final Class<WebPage> expectedPageClass = WebPage.class;

        final PageClassRegistry mockPageClassRegistry = Mockito.mock(PageClassRegistry.class);
        application = new CausewayWicketApplication() {
            private static final long serialVersionUID = 1L;

            @Override
            public PageClassRegistry getPageClassRegistry() {
                return mockPageClassRegistry;
            }
        };
        Mockito.when(mockPageClassRegistry.getPageClass(pageType))
        .thenReturn(_Casts.uncheckedCast(expectedPageClass));

        final Class<? extends Page> pageClass = application.getSignInPageClass();
        assertThat(expectedPageClass.isAssignableFrom(pageClass), is(true));
    }

}
