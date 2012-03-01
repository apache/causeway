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

package org.apache.isis.viewer.wicket.viewer;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.inject.Injector;

import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.settings.ISecuritySettings;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;

public class IsisWicketApplication_init {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);

    private IsisWicketApplication application;

    @Mock
    private ISecuritySettings mockSecuritySettings;
    @Mock
    private IResourceSettings mockResourceSettings;

    @Before
    public void setUp() throws Exception {
        context.ignoring(mockSecuritySettings);
        context.ignoring(mockResourceSettings);

        application = new IsisWicketApplication() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void initWicketComponentInjection(final Injector injector) {
                // ignore
            }

            @Override
            public ISecuritySettings getSecuritySettings() {
                return mockSecuritySettings;
            }

            @Override
            public IResourceSettings getResourceSettings() {
                return mockResourceSettings;
            }
        };
    }

    @Ignore
    @Test
    public void injectedApplicationCssUrl() {
        application.init();
        assertThat(application.getApplicationCssUrl(), is(notNullValue()));
    }

    @Ignore
    @Test
    public void injectedComponentFactoryRegistry() {
        application.init();
        assertThat(application.getComponentFactoryRegistry(), is(notNullValue()));
    }

    @Ignore
    @Test
    public void injectedImageCache() {
        application.init();
        assertThat(application.getImageCache(), is(notNullValue()));
    }

}
