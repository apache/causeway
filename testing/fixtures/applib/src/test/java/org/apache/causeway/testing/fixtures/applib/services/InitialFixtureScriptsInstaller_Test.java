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
package org.apache.causeway.testing.fixtures.applib.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.core.env.StandardEnvironment;

import org.apache.causeway.applib.events.metamodel.MetamodelEvent;
import org.apache.causeway.applib.services.eventbus.EventBusService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.applib.services.command.ResumeCommandLoggingEvent;
import org.apache.causeway.applib.services.command.PauseCommandLoggingEvent;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;

class InitialFixtureScriptsInstaller_Test {

    @Test
    void posts_initial_fixture_events_around_initial_script() {

        CausewayConfiguration configuration = new CausewayConfiguration(new StandardEnvironment(), Optional.empty());
        configuration.getTesting().getFixtures().setInitialScript(TestFixtureScript.class);
        FixtureScripts fixtureScripts = mock(FixtureScripts.class);
        EventBusService eventBusService = mock(EventBusService.class);
        InitialFixtureScriptsInstaller installer = new InitialFixtureScriptsInstaller(
                configuration, fixtureScripts, eventBusService);

        installer.onMetamodelEvent(MetamodelEvent.AFTER_METAMODEL_LOADED);

        InOrder inOrder = inOrder(eventBusService, fixtureScripts);
        inOrder.verify(eventBusService).post(any(PauseCommandLoggingEvent.class));
        inOrder.verify(fixtureScripts).run(any(TestFixtureScript.class));
        inOrder.verify(eventBusService).post(any(ResumeCommandLoggingEvent.class));
        verifyNoMoreInteractions(eventBusService);
    }

    @Test
    void posts_initial_fixture_installed_event_when_initial_script_fails() {

        CausewayConfiguration configuration = new CausewayConfiguration(new StandardEnvironment(), Optional.empty());
        configuration.getTesting().getFixtures().setInitialScript(TestFixtureScript.class);
        FixtureScripts fixtureScripts = mock(FixtureScripts.class);
        EventBusService eventBusService = mock(EventBusService.class);
        doThrow(new RuntimeException("boom")).when(fixtureScripts).run(any(TestFixtureScript.class));
        InitialFixtureScriptsInstaller installer = new InitialFixtureScriptsInstaller(
                configuration, fixtureScripts, eventBusService);

        try {
            installer.onMetamodelEvent(MetamodelEvent.AFTER_METAMODEL_LOADED);
        } catch (RuntimeException ex) {
            // expected
        }

        verify(eventBusService).post(any(PauseCommandLoggingEvent.class));
        verify(eventBusService).post(any(ResumeCommandLoggingEvent.class));
    }

    public static class TestFixtureScript extends FixtureScript {

        @Override
        protected void execute(final ExecutionContext executionContext) {
        }
    }
}
