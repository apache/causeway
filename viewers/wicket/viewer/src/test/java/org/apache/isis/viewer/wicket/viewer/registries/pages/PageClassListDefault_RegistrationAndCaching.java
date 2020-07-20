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

import org.apache.wicket.Page;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.actionprompt.ActionPromptPage;

public class PageClassListDefault_RegistrationAndCaching {

    private PageClassRegistryDefault registryImpl;

    @Before
    public void setUp() throws Exception {
        // necessary to provide an implementation that will register
        // all pages with the registry.
        final PageClassListDefault pageClassList = new PageClassListDefault();
        registryImpl = PageClassListDefault_Instantiation.newPageClassRegistryDefault(pageClassList);
    }

    @Test
    public void cachesPageByPageType() {
        final Class<? extends Page> pageClass = registryImpl.getPageClass(PageType.ACTION_PROMPT);
        assertThat(pageClass, is(not(nullValue())));
    }

    @Test
    public void canRegisterNewPageType() {
        class TestingActionPage extends ActionPromptPage {
            private static final long serialVersionUID = 1L;

            TestingActionPage() {
                super((ActionModel) null);
            }
        }
        registryImpl.registerPage(PageType.ACTION_PROMPT, TestingActionPage.class);

        final Class<? extends Page> pageClass = registryImpl.getPageClass(PageType.ACTION_PROMPT);
        assertThat(pageClass, is(org.hamcrest.Matchers.equalTo(TestingActionPage.class)));
    }

}
