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
package org.apache.isis.testdomain.domainmodel;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.badnoactenforce.Configuration_usingInvalidDomain_noActionEnforced;
import org.apache.isis.testdomain.model.badnoactenforce.InvalidOrphanedActionSupportNoAnnotationEnforced;
import org.apache.isis.testdomain.util.interaction.DomainObjectTesterFactory;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInvalidDomain_noActionEnforced.class
        },
        properties = {
                "isis.core.meta-model.introspector.policy=ANNOTATION_OPTIONAL",
                "isis.core.meta-model.introspector.mode=FULL"
        })
@TestPropertySource({
    //IsisPresets.DebugMetaModel,
    //IsisPresets.DebugProgrammingModel,
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class DomainModelTest_usingBadDomain_noAnnotationEnforced {

    @Inject private IsisConfiguration configuration;
    @Inject private IsisSystemEnvironment isisSystemEnvironment;
    @Inject private DomainObjectTesterFactory testerFactory;

    @Test
    void fullIntrospection_shouldBeEnabledByThisTestClass() {
        assertTrue(IntrospectionMode.isFullIntrospect(configuration, isisSystemEnvironment));
    }

    @Test
    void actionAnnotation_shouldBeOptionalByThisTestClass() {
        assertFalse(configuration
                .getCore().getMetaModel().getIntrospector().getPolicy()
                .getMemberAnnotationPolicy().isMemberAnnotationsRequired());
    }

    @Test
    void orphanedActionSupport_shouldFail() {

        val tester = testerFactory.objectTester(InvalidOrphanedActionSupportNoAnnotationEnforced.class);

        tester.assertValidationFailureOnMember(
                ProgrammingModelConstants.Validation.ORPHANED_METHOD, "hideOrphaned()");
    }

}
