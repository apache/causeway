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
package org.apache.isis.testdomain.persistence.jdo;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.RegressionTestAbstract;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoInventoryJaxbVm;
import org.apache.isis.testdomain.jdo.JdoTestFixtures;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class
        })
@TestPropertySource(IsisPresets.UseLog4j2Test)
//@Transactional
class JdoJaxbTest extends RegressionTestAbstract {

    @Inject private JdoTestFixtures testFixtures;
    @Inject private JaxbService jaxbService;

    @BeforeEach
    void setUp() throws InterruptedException {
        run(()->testFixtures.setUp3Books());
    }

    @Test
    void inventoryJaxbVm_shouldRoundtripProperly() {

        val xml = call(()->{
            val inventoryJaxbVm = testFixtures.setUpViewmodelWith3Books();
            // assert initial reference is populated as expected
            testFixtures.assertPopulatedWithDefaults(inventoryJaxbVm);
            // start round-trip
            return jaxbService.toXml(inventoryJaxbVm);
        });

        run(()->{
            //debug System.err.printf("%s%n", xml);
            val recoveredVm =
                    serviceInjector.injectServicesInto(
                            jaxbService.fromXml(JdoInventoryJaxbVm.class, xml));
            testFixtures.assertPopulatedWithDefaults(recoveredVm);
        });
    }

}
