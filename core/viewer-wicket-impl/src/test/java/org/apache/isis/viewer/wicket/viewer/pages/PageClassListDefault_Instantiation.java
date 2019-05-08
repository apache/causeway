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

package org.apache.isis.viewer.wicket.viewer.pages;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.viewer.wicket.ui.pages.PageClassList;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistrySpi;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassListDefault;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassRegistryDefault;

@RunWith(JMock.class)
public class PageClassListDefault_Instantiation {

    private final Mockery context = new JUnit4Mockery();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldCauseAllPagesToBeRegistered() {
        // necessary to provide an implementation that will register
        // all pages with the registry.
        //[2112] final PageClassListDefault pageClassList = new PageClassListDefault();
        new PageClassRegistryDefault();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfNoPagesRegistered() {
        // no side effects, ie doesn't register
        final PageClassList mockPageClassList = context.mock(PageClassList.class);
        context.checking(new Expectations() {
            {
                mockPageClassList.registerPages(with(any(PageClassRegistrySpi.class)));
            }
        });
        new PageClassRegistryDefault();
    }

}
