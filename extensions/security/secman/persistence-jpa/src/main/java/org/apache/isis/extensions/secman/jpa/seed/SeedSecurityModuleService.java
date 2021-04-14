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
package org.apache.isis.extensions.secman.jpa.seed;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.metamodel.events.MetamodelEvent;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.ext.secman.SeedSecurityModuleService")
@Order(OrderPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class SeedSecurityModuleService {

    private final FixtureScripts fixtureScripts;

    @Inject
    public SeedSecurityModuleService(final FixtureScripts fixtureScripts) {
        this.fixtureScripts = fixtureScripts;
    }

    @EventListener(MetamodelEvent.class)
    public void onMetamodelEvent(final MetamodelEvent event) {

        log.debug("received metamodel event {}", event);

        if (event.isPostMetamodel()) {
        	log.info("SEED security fixtures (JPA)");

            fixtureScripts.run(new SeedUsersAndRolesFixtureScript());
        }

    }



}
