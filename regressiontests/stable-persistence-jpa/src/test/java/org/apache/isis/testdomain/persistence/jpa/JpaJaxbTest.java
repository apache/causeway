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
package org.apache.isis.testdomain.persistence.jpa;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.jpa.JpaInventoryJaxbVm;
import org.apache.isis.testdomain.jpa.JpaTestFixtures;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
        })
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Transactional
class JpaJaxbTest extends IsisIntegrationTestAbstract {

    @Inject private JaxbService jaxbService;
    @Inject private BookmarkService bookmarkService;

    private JpaInventoryJaxbVm inventoryJaxbVm;

    @BeforeEach
    void setUp() {
        JpaTestFixtures.setUp3Books(repositoryService);
        inventoryJaxbVm = JpaTestFixtures.setUpViewmodelWith3Books(factoryService);
    }

    @Test
    void inventoryJaxbVm_shouldRoundtripProperly() {

        // assert initial reference is populated as expected
        JpaTestFixtures.assertPopulatedWithDefaults(inventoryJaxbVm, bookmarkService);

        // round-trip
        val xml = jaxbService.toXml(inventoryJaxbVm);
        //debug System.err.printf("%s%n", xml);
        val recoveredVm =
                serviceInjector.injectServicesInto(
                jaxbService.fromXml(JpaInventoryJaxbVm.class, xml));

        JpaTestFixtures.assertPopulatedWithDefaults(recoveredVm, bookmarkService);
    }

}
