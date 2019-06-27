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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.audit.AuditerService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.fixturespec.FixtureScriptsDefault;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.jdo.transaction.IsisPlatformTransactionManagerForJdo;
import org.apache.isis.runtime.spring.IsisBoot;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainModule;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Depends on {@link JdoBootstrappingTest_usingFixtures} to succeed.
 */
@SpringBootTest(
		classes = { 
				IsisBoot.class, 
				FixtureScriptsDefault.class, 
				JdoTestDomainModule.class, 
				IsisPlatformTransactionManagerForJdo.class,
				AuditerServiceTest.AuditerServiceProbe.class
		}, 
		properties = {
				"logging.config=log4j2-test.xml",
				"logging.level.org.apache.isis.jdo.transaction.IsisPlatformTransactionManagerForJdo=DEBUG",
				"logging.level.org.apache.isis.core.runtime.system.transaction.IsisTransaction=DEBUG",
				// "isis.reflector.introspector.parallelize=false",
				// "logging.level.org.apache.isis.metamodel.specloader.specimpl.ObjectSpecificationAbstract=TRACE"
	})
//@Transactional //XXX this test is non transactional
class AuditerServiceTest {

	@Inject private RepositoryService repository;
	@Inject private FixtureScripts fixtureScripts;
	@Inject private AuditerServiceProbe auditerService;

	@BeforeEach
	void setUp() {
		
		val transactionTemplate = IsisContext.createTransactionTemplate();
		transactionTemplate.execute(status -> {

			// cleanup
			fixtureScripts.runBuilderScript(JdoTestDomainPersona.PurgeAll.builder());

			// given
			fixtureScripts.runBuilderScript(JdoTestDomainPersona.InventoryWith1Book.builder());
			
			return null;
			
		});

	}

	@Test
	void auditerServiceShouldBeAwareOfInventoryChanges() {

		// given
		val books = repository.allInstances(Book.class);
		assertEquals(1, books.size());
		val book = books.listIterator().next();

		// when - running within its own transactional boundary
		val transactionTemplate = IsisContext.createTransactionTemplate();
		transactionTemplate.execute(status -> {
			
		    	auditerService.clearHistory();
				book.setName("Book #2");
				repository.persist(book);
				
				// then - before the commit
				assertEquals("", auditerService.getHistory());
				
				return null;
		});
		
		// then - after the commit
		assertEquals("targetClassName=Book,propertyName=name,preValue=Sample Book,postValue=Book #2;",
				auditerService.getHistory());
	}

	// -- HELPER

	@Singleton @Log4j2
	public static class AuditerServiceProbe implements AuditerService {

		private StringBuilder history = new StringBuilder();

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public void audit(UUID interactionId, int sequence, String targetClassName, Bookmark target,
				String memberIdentifier, String propertyName, String preValue, String postValue, String user,
				Timestamp timestamp) {
			
			history.append("targetClassName=").append(targetClassName).append(",").append("propertyName=")
					.append(propertyName).append(",").append("preValue=").append(preValue).append(",")
					.append("postValue=").append(postValue).append(";");
			
			log.debug("audit {}", history.toString());
		}

		void clearHistory() {
			log.debug("clearing");
			history = new StringBuilder();
			log.debug("cleared");
		}

		String getHistory() {
			log.debug("get {}", history.toString());
			return history.toString();
		}

	}

}
