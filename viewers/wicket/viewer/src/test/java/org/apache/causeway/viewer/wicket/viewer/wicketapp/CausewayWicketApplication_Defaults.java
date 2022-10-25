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

import org.apache.wicket.IConverterLocator;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.viewer.wicket.viewer.integration.AuthenticatedWebSessionForCauseway;

class CausewayWicketApplication_Defaults {

    private CausewayWicketApplication application;

    @BeforeEach
    public void setUp() throws Exception {
        application = new CausewayWicketApplication();
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void usesCustomSubclassOfAuthenticatedWebSession() {
        final Class<? extends AuthenticatedWebSession> webSessionClass = application.getWebSessionClass();
        assertThat(webSessionClass.equals(AuthenticatedWebSessionForCauseway.class), is(true));
    }

    @Test
    public void providesConverterLocatorRegistersCausewaySpecificConverters() {
        final IConverterLocator converterLocator = application.newConverterLocator();
        assertThat(converterLocator.getConverter(ManagedObject.class), is(not(nullValue())));
        assertThat(converterLocator.getConverter(ObjectMemento.class), is(not(nullValue())));
    }

}
