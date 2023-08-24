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
package org.apache.causeway.testdomain.persistence.jpa.springdata;

import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.conf.Configuration_usingSpringDataJpa;
import org.apache.causeway.testdomain.jpa.springdata.Employee;
import org.apache.causeway.testdomain.jpa.springdata.EmployeeRepository;
import org.apache.causeway.testdomain.jpa.springdata.SpringDataJpaTestModule;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.ExecutionParametersServiceAutoConfiguration;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScriptsSpecificationProviderAutoConfiguration;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

@DataJpaTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:SpringDataJpaBootstrappingTest",
        })
@ContextConfiguration(classes = {
        Configuration_usingSpringDataJpa.class,
        FixtureScriptsSpecificationProviderAutoConfiguration.class, // because @DataJpaTest disables autoconfiguration
        ExecutionParametersServiceAutoConfiguration.class           // because @DataJpaTest disables autoconfiguration
})
@TestPropertySource(CausewayPresets.UseLog4j2Test)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
// @Disabled // CAUSEWAY-2789 revert
class SpringDataJpaBootstrappingTest extends CausewayIntegrationTestAbstract {

    @Inject private Optional<PlatformTransactionManager> platformTransactionManager;
    @Inject private RepositoryService repository;
    @Inject private SpecificationLoader specLoader;

    @Inject private EmployeeRepository employeeRepository;

    void cleanUp() {
        employeeRepository.deleteAllInBatch();
    }

    void setUp() {
        SpringDataJpaTestModule.setupEmployeeFixture(employeeRepository);
    }

    @Inject
    ConfigurableBeanFactory configurableBeanFactory;

    @Test @Order(0)
    void platformTransactionManager_shouldBeAvailable() {
        assertTrue(platformTransactionManager.isPresent());
        platformTransactionManager.ifPresent(ptm->{
            assertEquals("JpaTransactionManager", ptm.getClass().getSimpleName());
        });
    }

    @Test @Order(0)
    void transactionalAnnotation_shouldBeSupported() {
        assertTrue(platformTransactionManager.isPresent());
        platformTransactionManager.ifPresent(ptm->{

            val txDef = new DefaultTransactionDefinition();
            txDef.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_MANDATORY);

            val txStatus = ptm.getTransaction(txDef);

            assertNotNull(txStatus);
            assertFalse(txStatus.isCompleted());

        });
    }

    @Test @Order(0)
    void jpaEntities_shouldBeRecognisedAsSuch() {
        val productSpec = specLoader.loadSpecification(Employee.class);
        assertTrue(productSpec.isEntity());
        assertNotNull(productSpec.entityFacetElseFail());
    }

    @Test @Order(1) @Rollback(false)
    void sampleEmployeesShouldBeSetUp() {

        // given - expected pre condition: no inventories

        cleanUp();
        assertEquals(0, repository.allInstances(Employee.class).size());

        // when

        setUp();

        // then - expected post condition: 4 employees

        val employees = repository.allInstances(Employee.class);
        assertEquals(4, employees.size());

        val employee = employees.get(0);
        assertNotNull(employee);
        assertNotNull(employee.getLastName());

    }

    @Test @Order(2) @Rollback(false)
    void aSecondRunShouldWorkAsWell() {
        sampleEmployeesShouldBeSetUp();
    }


}
