/**
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
package org.apache.isis.core.integtestsupport.scenarios;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.internal.ExpectationBuilder;
import org.jmock.internal.InvocationExpectation;
import org.jmock.internal.NamedSequence;
import org.jmock.internal.State;
import org.jmock.internal.StateMachine;
import org.jmock.internal.StatePredicate;

import org.apache.isis.applib.fixtures.InstallableFixture;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.integtestsupport.IsisSystemForTest;
import org.apache.isis.core.specsupport.scenarios.DomainServiceProvider;
import org.apache.isis.core.specsupport.scenarios.ScenarioExecution;
import org.apache.isis.core.wrapper.WrapperFactoryDefault;


/**
 * An extension of {@link ScenarioExecution} for use within (coarse grained)
 * integration tests and Cucumber specs where there is back-end database.
 *
 * <p>
 * To this end it provides implementations of 
 * {@link #install(InstallableFixture...)} (to tear down/setup data)
 * and of {@link #beginTran() begin} and {@link #endTran(boolean) end} (
 * for transaction management.
 */
public class ScenarioExecutionForIntegration extends ScenarioExecution  {

    private IsisSystemForTest isft;

    public ScenarioExecutionForIntegration() {
        super(IsisSystemForTest.get());
        this.isft = (IsisSystemForTest) dsp;
    }

    // //////////////////////////////////////

    public WrapperFactory wrapperFactory() {
        return service(WrapperFactory.class);
    }

    // //////////////////////////////////////

    /**
     * Install arbitrary fixtures, eg before an integration tests or as part of a 
     * Cucumber step definitions or hook.
     */
    public void install(InstallableFixture... fixtures) {
        isft.installFixtures(fixtures);
    }

    // //////////////////////////////////////

    /**
     * For Cucumber hooks to call, performing transaction management around each step.
     */
    public void beginTran() {
        isft.beginTran();
    }

    /**
     * For Cucumber hooks to call, performing transaction management around each step.
     */
    public void endTran(boolean ok) {
        if(ok) {
            isft.commitTran();
        } else {
            isft.abortTran();
        }
    }

}