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

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.events.metamodel.MetamodelEvent;
import org.apache.causeway.applib.services.command.PauseCommandLoggingEvent;
import org.apache.causeway.applib.services.command.ResumeCommandLoggingEvent;
import org.apache.causeway.applib.services.eventbus.EventBusService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InitialFixtureScriptsInstallerTest {

    @Test
    void publishesPauseRunsFixtureAndPublishesResumeInOrder() {
        var fixtureScripts = mock(FixtureScripts.class);
        var eventBusService = mock(EventBusService.class);
        var installer = installer(fixtureScripts, eventBusService);

        installer.onMetamodelEvent(MetamodelEvent.AFTER_METAMODEL_LOADED);

        var inOrder = inOrder(eventBusService, fixtureScripts);
        inOrder.verify(eventBusService).post(argThat(PauseCommandLoggingEvent.class::isInstance));
        inOrder.verify(fixtureScripts).run(any(FixtureScript[].class));
        inOrder.verify(eventBusService).post(argThat(ResumeCommandLoggingEvent.class::isInstance));
    }

    @Test
    void publishesResumeWhenFixtureExecutionFails() {
        var fixtureScripts = mock(FixtureScripts.class);
        doThrow(new IllegalStateException("fixture failure"))
            .when(fixtureScripts).run(any(FixtureScript[].class));
        var eventBusService = mock(EventBusService.class);
        var installer = installer(fixtureScripts, eventBusService);

        assertThatThrownBy(() -> installer.onMetamodelEvent(MetamodelEvent.AFTER_METAMODEL_LOADED))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("fixture failure");

        var inOrder = inOrder(eventBusService, fixtureScripts);
        inOrder.verify(eventBusService).post(argThat(PauseCommandLoggingEvent.class::isInstance));
        inOrder.verify(fixtureScripts).run(any(FixtureScript[].class));
        inOrder.verify(eventBusService).post(argThat(ResumeCommandLoggingEvent.class::isInstance));
    }

    private InitialFixtureScriptsInstaller installer(
            final FixtureScripts fixtureScripts,
            final EventBusService eventBusService) {
        var causewayConfiguration = mock(CausewayConfiguration.class);
        var fixtures = new CausewayConfiguration.Testing.Fixtures(InitialScript.class, null);
        when(causewayConfiguration.testing()).thenReturn(new CausewayConfiguration.Testing(fixtures));
        return new InitialFixtureScriptsInstaller(causewayConfiguration, fixtureScripts, eventBusService);
    }

    public static class InitialScript extends FixtureScript {
        @Override
        protected void execute(final ExecutionContext executionContext) {
        }
    }
}
