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
package demoapp.dom.domain.tests;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testing.integtestsupport.applib.validate.DomainModelValidator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import demoapp.dom.domain.tests.conf.Configuration_usingJdo;
import demoapp.dom.domain.tests.conf.MetaModelExportToConsole;
import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
        },
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL",
                //"causeway.core.config.configuration-property-visibility-policy=ALWAYS_SHOW",
                "causeway.applib.annotation.domain-object.editing=TRUE",
                "causeway.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
                "logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
@ActiveProfiles(profiles = "demo-jdo")
class DomainIntrospectionTestJdo {

    @Inject private MetaModelService metaModelService;
    //@Inject private JaxbService jaxbService;
    @Inject private ServiceRegistry serviceRegistry;
    @Inject private SpecificationLoader specificationLoader;
    //@Inject private TitleService titleService;
    //@Inject private CausewayConfiguration causewayConfig;
    //@Inject private DomainObjectTesterFactory testerFactory;

    @Test
    void domain_shouldPassValidation() {

        assertFalse(specificationLoader.snapshotSpecifications().isEmpty());

        val validateDomainModel = new DomainModelValidator(serviceRegistry);
        val validationFailures = validateDomainModel.getFailures().stream()
                .collect(Collectors.toSet());

        if(!validationFailures.isEmpty()) {
            fail(String.format("%d problems found:\n%s",
                    validationFailures.size(),
                    validationFailures.stream()
                    .map(validationFailure->validationFailure.getMessage())
                    .collect(Collectors.joining("\n"))));
        }

        new MetaModelExportToConsole().export(metaModelService.getDomainModel());
    }

}
