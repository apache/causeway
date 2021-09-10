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

import org.apache.wicket.IConverterLocator;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.memento.ObjectMemento;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.wicket.viewer.integration.AuthenticatedWebSessionForIsis;

public class IsisWicketApplication_Defaults {

    @Rule
    public final JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private IsisWicketApplication application;

    @Before
    public void setUp() throws Exception {
        application = new IsisWicketApplication();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void usesCustomSubclassOfAuthenticatedWebSession() {
        final Class<? extends AuthenticatedWebSession> webSessionClass = application.getWebSessionClass();
        assertThat(webSessionClass.equals(AuthenticatedWebSessionForIsis.class), is(true));
    }

    @Ignore // 6.0.0 does it differently
    @Test
    public void providesCustomRequestCycle() {
        //        final WebRequest mockRequest = context.mock(WebRequest.class);
        //        final Response mockResponse = context.mock(Response.class);
        //        final RequestCycle newRequestCycle = application.newRequestCycle(mockRequest, mockResponse);
        //        assertThat(newRequestCycle, is(WebRequestCycleForIsis.class));
    }

    @Test
    public void providesConverterLocatorRegistersIsisSpecificConverters() {
        final IConverterLocator converterLocator = application.newConverterLocator();
        assertThat(converterLocator.getConverter(ManagedObject.class), is(not(nullValue())));
        assertThat(converterLocator.getConverter(ObjectMemento.class), is(not(nullValue())));
    }

}
