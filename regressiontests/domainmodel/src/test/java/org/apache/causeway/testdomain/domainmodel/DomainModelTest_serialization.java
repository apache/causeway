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

import java.io.Serializable;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.commons.internal.testing._SerializationTester;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.good.Configuration_usingValidDomain;
import org.apache.causeway.testdomain.model.good.ProperMemberSupport;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingValidDomain.class,

        },
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL",
                "causeway.applib.annotation.domain-object.editing=TRUE",
                "causeway.core.meta-model.validator.explicit-object-type=FALSE" // does not override any of the imports
        })
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class DomainModelTest_serialization extends CausewayIntegrationTestAbstract {

    @Inject private SpecificationLoader specificationLoader;

    // -- ACT

    @Test
    void objectActionDefault_shouldBe_Serializable() {
        var holderSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);
        var act = (Serializable) holderSpec.getActionElseFail("hideMe");
        assertEquals("ObjectActionDefault", act.getClass().getSimpleName());
        _SerializationTester.assertEqualsOnRoundtrip(act);
    }
    @Test
    void objectActionMixedIn_shouldBe_Serializable() {
        var holderSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);
        var act = (Serializable) holderSpec.getActionElseFail("action1");
        assertEquals("ObjectActionMixedIn", act.getClass().getSimpleName());
        _SerializationTester.assertEqualsOnRoundtrip(act);
    }

    // -- PROP

    @Test
    void oneToOneAssociationDefault_shouldBe_Serializable() {
        var holderSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);
        var prop = (Serializable) holderSpec.getPropertyElseFail("myProp");
        assertEquals("OneToOneAssociationDefault", prop.getClass().getSimpleName());
        _SerializationTester.assertEqualsOnRoundtrip(prop);
    }
    @Test
    void oneToOneAssociationMixedIn_shouldBe_Serializable() {
        var holderSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);
        var prop = (Serializable) holderSpec.getPropertyElseFail("property1");
        assertEquals("OneToOneAssociationMixedIn", prop.getClass().getSimpleName());
        _SerializationTester.assertEqualsOnRoundtrip(prop);
    }

    // -- COLL

    @Test
    void oneToManyAssociationDefault_shouldBe_Serializable() {
        var holderSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);
        var coll = (Serializable) holderSpec.getCollectionElseFail("myColl");
        assertEquals("OneToManyAssociationDefault", coll.getClass().getSimpleName());
        _SerializationTester.assertEqualsOnRoundtrip(coll);
    }
    @Test
    void oneToManyAssociationMixedIn_shouldBe_Serializable() {
        var holderSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);
        var coll = (Serializable) holderSpec.getCollectionElseFail("collection1");
        assertEquals("OneToManyAssociationMixedIn", coll.getClass().getSimpleName());
        _SerializationTester.assertEqualsOnRoundtrip(coll);
    }

}
