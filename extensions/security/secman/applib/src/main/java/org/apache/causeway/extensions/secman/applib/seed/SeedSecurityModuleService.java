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
package org.apache.causeway.extensions.secman.applib.seed;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.events.metamodel.MetamodelEvent;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.seed.scripts.SeedUsersAndRolesFixtureScript;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Named(CausewayModuleExtSecmanApplib.NAMESPACE + ".SeedSecurityModuleService")
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject })
@Slf4j
public class SeedSecurityModuleService {

    private final FixtureScripts fixtureScripts;
    private final InteractionService interactionService;

    @EventListener(MetamodelEvent.class)
    @Order(PriorityPrecedence.MIDPOINT - 100)
    public void onMetamodelEvent(final MetamodelEvent event) {

        log.debug("received metamodel event {}", event);

        if (event.isPostMetamodel()) {
        	log.info("SEED security fixtures (Users and Roles)");

        	interactionService.runAnonymous(() -> fixtureScripts.run(new SeedUsersAndRolesFixtureScript()));
        }
    }
}
