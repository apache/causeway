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
package org.apache.isis.testdomain.domainmodel.jdo;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.entities.JdoEntityMetaAnnotated;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;
import org.apache.isis.testdomain.model.good.Configuration_usingValidDomain;
import org.apache.isis.testing.integtestsupport.applib.validate.DomainModelValidator;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingValidDomain.class,

        },
        properties = {
                "isis.core.meta-model.introspector.mode=FULL",
                "isis.applib.annotation.domain-object.editing=TRUE",
                "isis.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
                "logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class DomainModelTest {

    @Inject private ServiceRegistry serviceRegistry;
    @Inject private SpecificationLoader specificationLoader;

    @Test
    void goodDomain_shouldPassValidation() {
        //debug();
        assertFalse(specificationLoader.snapshotSpecifications().isEmpty());

        val validateDomainModel = new DomainModelValidator(serviceRegistry);
        validateDomainModel.throwIfInvalid(); // should not throw
    }

    @Test
    void pluginProvidedMixins_shouldBePickedUp() {

        val entitySpec = specificationLoader.loadSpecification(JdoProduct.class);

        val mx_datanucleusVersionLong = entitySpec.getAssociationElseFail("datanucleusVersionLong"); // plugged in mixin
        assertNotNull(mx_datanucleusVersionLong);

    }

    @Test
    void metaAnnotatedEntities_shouldBeRecognized() {

        val entitySpec = specificationLoader.loadSpecification(JdoEntityMetaAnnotated.class);

        assertEquals(BeanSort.ENTITY, entitySpec.getBeanSort());
        assertNotNull(entitySpec.getFacet(EntityFacet.class));
    }


}
