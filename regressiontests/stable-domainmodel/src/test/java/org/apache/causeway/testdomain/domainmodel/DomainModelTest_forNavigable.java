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
package org.apache.causeway.testdomain.domainmodel;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.navigable.ITypeA;
import org.apache.causeway.testdomain.model.navigable.TypeA;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
        },
        properties = {
                "causeway.core.meta-model.introspector.policy=ANNOTATION_REQUIRED",
        })
@TestPropertySource({
    CausewayPresets.IntrospectFully,
    CausewayPresets.UseLog4j2Test,
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class DomainModelTest_forNavigable extends CausewayIntegrationTestAbstract {

    @Inject private SpecificationLoader specificationLoader;
    @Inject private CausewayConfiguration causewayConfiguration;

    @Test
    void overridden_getter_from_interface_should_be_included_with_snapshots() {

        // prereq.
        val introspectorCfg = causewayConfiguration.getCore().getMetaModel().getIntrospector();
        assertEquals(IntrospectionPolicy.ANNOTATION_REQUIRED, introspectorCfg.getPolicy());

        // when
        val specIA = specificationLoader.specForTypeElseFail(ITypeA.class);
        val specA = specificationLoader.specForTypeElseFail(TypeA.class);

        // then ... interface has no setter, so should exclude the single property from snapshots
        val propsIA = specIA.streamProperties(MixedIn.EXCLUDED)
                .collect(Can.toCan());
        assertEquals(1, propsIA.size());
        assertTrue(propsIA.getSingletonOrFail().isExcludedFromSnapshots());

        // then ... concrete class has setter, so should include the single property with snapshots
        val propsA = specA.streamProperties(MixedIn.EXCLUDED)
                .collect(Can.toCan());
        assertEquals(1, propsA.size());
        assertFalse(propsA.getSingletonOrFail().isExcludedFromSnapshots());
    }

}
