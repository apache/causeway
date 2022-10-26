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
package org.apache.causeway.testdomain.persistence.jpa;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.RegressionTestAbstract;
import org.apache.causeway.testdomain.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.JpaInventoryJaxbVm;
import org.apache.causeway.testdomain.jpa.JpaTestFixtures;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
        })
@TestPropertySource(CausewayPresets.UseLog4j2Test)
//@Transactional
class JpaJaxbTest extends RegressionTestAbstract {

    @Inject private JpaTestFixtures testFixtures;
    @Inject private JaxbService jaxbService;

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
                            jaxbService.fromXml(JpaInventoryJaxbVm.class, xml));
            testFixtures.assertPopulatedWithDefaults(recoveredVm);
        });
    }

}
