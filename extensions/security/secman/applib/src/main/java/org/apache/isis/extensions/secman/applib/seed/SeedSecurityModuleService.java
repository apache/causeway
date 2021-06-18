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
package org.apache.isis.extensions.secman.applib.seed;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.core.metamodel.events.MetamodelEvent;
import org.apache.isis.extensions.secman.applib.seed.scripts.SeedUsersAndRolesFixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.extern.log4j.Log4j2;

/**
 * Automatically seeds the built-in roles (and permissions) for both Secman
 * and any other UI features made available by the other modules.
 *
 * <p>
 *     The service just runs the {@link SeedUsersAndRolesFixtureScript} on a callback of {@link MetamodelEvent}.
 * </p>
 *
 * @see SeedUsersAndRolesFixtureScript
 * @see MetamodelEvent
 *
 * @since 2.0 {@index}
 */
@Service
@Named("isis.ext.secman.SeedSecurityModuleService")
@Qualifier("Default")
@Log4j2
public class SeedSecurityModuleService {

    private final FixtureScripts fixtureScripts;

    @Inject
    public SeedSecurityModuleService(final FixtureScripts fixtureScripts) {
        this.fixtureScripts = fixtureScripts;
    }

    @EventListener(MetamodelEvent.class)
    @Order(PriorityPrecedence.MIDPOINT - 100)
    public void onMetamodelEvent(final MetamodelEvent event) {

        log.debug("received metamodel event {}", event);

        if (event.isPostMetamodel()) {
        	log.info("SEED security fixtures (Users and Roles)");

            fixtureScripts.run(new SeedUsersAndRolesFixtureScript());
        }
    }
}
