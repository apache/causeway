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
package org.apache.causeway.testdomain.domainmodel.jdo;

import javax.inject.Inject;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.object.datastoreidentity.JdoDatastoreIdentityFacet;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.object.version.JdoVersionFacet;
import org.apache.causeway.testdomain.conf.Configuration_usingJdo;
import org.apache.causeway.testdomain.jdo.entities.JdoEntityMetaAnnotated;
import org.apache.causeway.testdomain.jdo.entities.JdoProduct;
import org.apache.causeway.testdomain.model.good.Configuration_usingValidDomain;
import org.apache.causeway.testing.integtestsupport.applib.validate.DomainModelValidator;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingValidDomain.class,

        },
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL",
                "causeway.applib.annotation.domain-object.editing=TRUE",
                "causeway.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
                "logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class DomainModelTest {

    @Inject private ServiceRegistry serviceRegistry;
    @Inject private SpecificationLoader specificationLoader;

    @Test
    void goodDomain_shouldPassValidation() {
        //debug();
        assertFalse(specificationLoader.snapshotSpecifications().isEmpty());

        val validateDomainModel = new DomainModelValidator(serviceRegistry);

        validateDomainModel.getFailures().forEach(f->System.err.printf("%s:%s%n", f.getOrigin(), f.getMessage()));

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
        assertNotNull(entitySpec.entityFacetElseFail());

        //@PersistenceCapable(identityType = IdentityType.DATASTORE)
        val persistenceCapableFacet = entitySpec.getFacet(JdoPersistenceCapableFacet.class);
        assertNotNull(persistenceCapableFacet);
        assertEquals(IdentityType.DATASTORE, persistenceCapableFacet.getIdentityType());

        //@EntityWithIdAndVersion(schema = "ORGANISATION")
        assertEquals("ORGANISATION", persistenceCapableFacet.getSchema());

        //@DatastoreIdentity(strategy = IdGeneratorStrategy.NATIVE)
        val datastoreIdentityFacet = entitySpec.getFacet(JdoDatastoreIdentityFacet.class);
        assertNotNull(datastoreIdentityFacet);
        assertEquals(IdGeneratorStrategy.NATIVE, datastoreIdentityFacet.getStrategy());

        //@Version(strategy = VersionStrategy.VERSION_NUMBER)
        val versionFacet = entitySpec.getFacet(JdoVersionFacet.class);
        assertNotNull(versionFacet);
    }


}
