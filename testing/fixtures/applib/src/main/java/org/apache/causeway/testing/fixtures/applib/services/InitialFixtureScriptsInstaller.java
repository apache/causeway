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

import java.lang.reflect.InvocationTargetException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.events.metamodel.MetamodelEvent;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("causeway.testing.fixtures.InitialFixtureScriptsInstaller")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class InitialFixtureScriptsInstaller {

    private final FixtureScripts fixtureScripts;

    private FixtureScript initialFixtureScript;

    @Inject
    public InitialFixtureScriptsInstaller(
            final CausewayConfiguration causewayConfiguration,
            final FixtureScripts fixtureScripts) {

        this.fixtureScripts = fixtureScripts;

        final Class<?> initialScript = causewayConfiguration.getTesting().getFixtures().getInitialScript();
        if (initialScript != null
                && FixtureScript.class.isAssignableFrom(initialScript)) {
            try {
                initialFixtureScript = (FixtureScript) initialScript.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                initialFixtureScript = null;
            }
        }
    }

    @EventListener(MetamodelEvent.class)
    @Order(PriorityPrecedence.LAST - 100)
    public void onMetamodelEvent(final MetamodelEvent event) {

        log.debug("received metamodel event {}", event);

        if (event.isPostMetamodel()
                && initialFixtureScript != null) {

            log.info("install initial fixtures from script {}", initialFixtureScript.getFriendlyName());
            fixtureScripts.run(initialFixtureScript);
        }
    }

}
