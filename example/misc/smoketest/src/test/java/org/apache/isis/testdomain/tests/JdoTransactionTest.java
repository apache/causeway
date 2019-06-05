/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.testdomain.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.fixturespec.FixtureScriptsDefault;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.commons.collections.Bin;
import org.apache.isis.core.integtestsupport.components.HeadlessTransactionSupportDefault;
import org.apache.isis.core.runtime.headless.HeadlessTransactionSupport;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.runtime.spring.IsisBoot;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.Inventory;
import org.apache.isis.testdomain.jdo.JdoTestDomainModule;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.Product;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.val;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
		classes = {
				HeadlessTransactionSupportDefault.class,
				IsisBoot.class,
				FixtureScriptsDefault.class,
				JdoTestDomainModule.class,
		},
		properties = {
				"logging.config=log4j2-test.xml",
				//"isis.reflector.introspector.parallelize=false",
				//"logging.level.org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract=TRACE"
		}
		)
class JdoTransactionTest {

	@Inject IsisSessionFactory isisSessionFactory;
	@Inject HeadlessTransactionSupport transactions;
	@Inject FixtureScripts fixtureScripts;

	@Inject RepositoryService repository;

	@BeforeAll
	static void launchH2Console() throws SQLException {
		H2Console.main(null);
	}
	
	@BeforeEach
	void setUp() {

		isisSessionFactory.openSession(new InitialisationSession());

		transactions.beginTransaction();

		// cleanup
		fixtureScripts.runBuilderScript(
				JdoTestDomainPersona.PurgeAll.builder());

		transactions.endTransaction();

		isisSessionFactory.closeSession();
	}

	@Test
	void sampleInventoryShouldBeSetUp() {

		// expected pre condition: no inventories
		
		isisSessionFactory.openSession(new InitialisationSession());
		assertEquals(0, repository.allInstances(Inventory.class).size());
		isisSessionFactory.closeSession();

		{ // setup sample Inventory
		
			isisSessionFactory.openSession(new InitialisationSession());
	
			transactions.beginTransaction();
	
			Set<Product> products = new HashSet<>();
	
			products.add(Book.of(
					"Sample Book", "A sample book for testing.", 99.,
					"Sample Author", "Sample ISBN", "Sample Publisher"));
	
			val inventory = Inventory.of("Sample Inventory", products);
			repository.persist(inventory);
	
			transactions.endTransaction();
	
			isisSessionFactory.closeSession();
		
		}

		// expected post condition: ONE inventory

		isisSessionFactory.openSession(new InitialisationSession());
		
		val inventories = Bin.ofCollection(repository.allInstances(Inventory.class)); 
		assertEquals(1, inventories.size());
		
		val inventory = inventories.getSingleton().get();
		assertNotNull(inventory);
		assertNotNull(inventory.getProducts());
		assertEquals(1, inventory.getProducts().size());

		val product = inventory.getProducts().iterator().next();
		assertEquals("Sample Book", product.getName());
		
		isisSessionFactory.closeSession();

	}


}
