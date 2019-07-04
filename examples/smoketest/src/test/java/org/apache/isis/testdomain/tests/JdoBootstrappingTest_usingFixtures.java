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
package org.apache.isis.testdomain.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.extensions.fixtures.legacy.fixturescripts.FixtureScripts;
import org.apache.isis.testdomain.jdo.Inventory;
import org.apache.isis.testdomain.jdo.JdoTestDomainModule;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import lombok.val;

@SpringBootTest(
	classes = { 
			JdoTestDomainModule.class, 
	}, 
	properties = {
			"logging.config=log4j2-test.xml",
			// "isis.reflector.introspector.parallelize=false",
			// "logging.level.org.apache.isis.metamodel.specloader.specimpl.ObjectSpecificationAbstract=TRACE"
})
@Transactional
class JdoBootstrappingTest_usingFixtures {

	@Inject private FixtureScripts fixtureScripts;
	@Inject private RepositoryService repository;

	void setUp() {
		// cleanup
		fixtureScripts.runBuilderScript(JdoTestDomainPersona.PurgeAll.builder());

		// given
		fixtureScripts.runBuilderScript(JdoTestDomainPersona.InventoryWith1Book.builder());
	}

	@Test @Rollback(false)
	void sampleInventoryShouldBeSetUp() {

		setUp();

		val inventories = repository.allInstances(Inventory.class);
		assertEquals(1, inventories.size());

		val inventory = inventories.get(0);
		assertNotNull(inventory);
		assertNotNull(inventory.getProducts());
		assertEquals(1, inventory.getProducts().size());

		val product = inventory.getProducts().iterator().next();
		assertEquals("Sample Book", product.getName());

	}

}
