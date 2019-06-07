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
import org.apache.isis.incubator.IsisPlatformTransactionManagerForJdo;
import org.apache.isis.runtime.spring.IsisBoot;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainModule;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import lombok.val;

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
				// "isis.reflector.introspector.parallelize=false",
				// "logging.level.org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract=TRACE"
	})
@Transactional
class AuditerServiceTest {

	@Inject private RepositoryService repository;
	@Inject private FixtureScripts fixtureScripts;
	@Inject private AuditerServiceProbe auditerService;

	@BeforeEach
	void setUp() {

		// cleanup
		fixtureScripts.runBuilderScript(JdoTestDomainPersona.PurgeAll.builder());

		// given
		fixtureScripts.runBuilderScript(JdoTestDomainPersona.InventoryWith1Book.builder());
	}

	@Test @Rollback(false)
	void auditerServiceShouldBeAwareOfInventoryChanges() {

		// given
		val book = repository.allInstances(Book.class).listIterator().next();

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

	@Singleton
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
		}

		void clearHistory() {
			history = new StringBuilder();
		}

		String getHistory() {
			return history.toString();
		}

	}

}
