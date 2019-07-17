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
package domainapp.application.bdd.specs;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;
import org.apache.isis.runtime.system.context.IsisContext;
import org.junit.Ignore;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import domainapp.application.integtests.SpringIntegrationTest;
import domainapp.modules.simple.dom.impl.SimpleObject;
import domainapp.modules.simple.dom.impl.SimpleObjects;
import domainapp.modules.simple.fixture.SimpleObject_persona;
import lombok.val;

@Ignore("not a JUnit test")
public class SimpleObjectsStepDef extends SpringIntegrationTest {


	@Given("^there are.* (\\d+) simple objects$")
	public void there_are_N_simple_objects(int n) throws Throwable {
		final List<SimpleObject> list = wrap(simpleObjects).listAll();
		assertThat(list.size(), is(n));
	}

	@When("^.*create a .*simple object$")
	public void create_a_simple_object() throws Throwable {
		wrap(simpleObjects).create(UUID.randomUUID().toString());
	}

	// -- TRANSACTION ASPECT

	private Runnable afterScenario;

	@cucumber.api.java.Before //TODO is there another way to make scenarios transactional?
	public void beforeScenario(){
		val txTemplate = IsisContext.createTransactionTemplate();
		val status = txTemplate.getTransactionManager().getTransaction(null);
		afterScenario = () -> {
			txTemplate.getTransactionManager().rollback(status);
		};

		fixtureScripts.runBuilderScript(SimpleObject_persona.BANG.builder());
		fixtureScripts.runBuilderScript(SimpleObject_persona.BAR.builder());
		fixtureScripts.runBuilderScript(SimpleObject_persona.BAZ.builder());

		status.flush();
	} 

	@cucumber.api.java.After //TODO is there another way to make scenarios transactional?
	public void afterScenario(){
		if(afterScenario==null) {
			return;
		}
		afterScenario.run();
		afterScenario = null;
	}

	// -- DEPENDENCIES

	@Inject protected SimpleObjects simpleObjects;
	@Inject private FixtureScripts fixtureScripts;


}
