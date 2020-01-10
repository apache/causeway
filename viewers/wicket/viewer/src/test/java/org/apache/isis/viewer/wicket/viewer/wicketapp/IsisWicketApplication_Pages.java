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

package org.apache.isis.viewer.wicket.viewer.wicketapp;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.home.HomePage;

public class IsisWicketApplication_Pages {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);

    @Mock
    private PageClassRegistry mockPageClassRegistry;

    private IsisWicketApplication application;

    @Test
    public void delegatesToPageClassRegistryToObtainPageTypes() {
        final PageType pageType = PageType.HOME;
        final Class<HomePage> expectedPageClass = HomePage.class;

        application = new IsisWicketApplication() {
            private static final long serialVersionUID = 1L;

            @Override
            public PageClassRegistry getPageClassRegistry() {
                return mockPageClassRegistry;
            }

        };
        context.checking(new Expectations() {
            {
                oneOf(mockPageClassRegistry).getPageClass(pageType);
                will(returnValue(expectedPageClass));
            }
        });
        final Class<? extends Page> pageClass = application.getHomePage();
        assertThat(expectedPageClass.isAssignableFrom(pageClass), is(true));
    }

    @Test
    public void delegatesToPageClassRegistryToObtainPageTypes_ForSignIn() {

        final PageType pageType = PageType.SIGN_IN;
        final Class<WebPage> expectedPageClass = WebPage.class;

        final PageClassRegistry mockPageClassRegistry = context.mock(PageClassRegistry.class);
        application = new IsisWicketApplication() {
            private static final long serialVersionUID = 1L;

            @Override
            public PageClassRegistry getPageClassRegistry() {
                return mockPageClassRegistry;
            }
        };
        context.checking(new Expectations() {
            {
                oneOf(mockPageClassRegistry).getPageClass(pageType);
                will(returnValue(expectedPageClass));
            }
        });
        final Class<? extends Page> pageClass = application.getSignInPageClass();
        assertThat(expectedPageClass.isAssignableFrom(pageClass), is(true));
    }

}
