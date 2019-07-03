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
package org.apache.isis.specsupport.scenarios;

import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.internal.ExpectationBuilder;

/**
 * An implementation of {@link ScenarioExecution} with which uses JMock to provide
 * all services.
 *
 * <p>
 * Expectations can be {@link Mockery#checking(org.jmock.internal.ExpectationBuilder) set}
 * and interactions {@link Mockery#assertIsSatisfied() verified} by accessing
 * the underlying {@link Mockery}.
 *
 * @deprecated - to be removed in 2.0, will support BDD for integration tests only
 */
@Deprecated
public class ScenarioExecutionForUnit extends ScenarioExecution {

    private final DomainServiceProviderMockery dspm;

    public ScenarioExecutionForUnit() {
        this(new DomainServiceProviderMockery());
    }
    private ScenarioExecutionForUnit(DomainServiceProviderMockery dspm) {
        super(dspm, ScenarioExecutionScope.UNIT);
        this.dspm = dspm.init(this);
    }


    // //////////////////////////////////////

    @Override
    public boolean supportsMocks() {
        return true;
    }
    /**
     * Sets up an expectation against the underlying JMock {@link Mockery}
     * (as wrapped by {@link DomainServiceProviderMockery}).
     */
    @Override
    public void checking(ExpectationBuilder expectations) {
        dspm.mockery().checking(expectations);
    }

    @Override
    public void assertIsSatisfied() {
        dspm.assertIsSatisfied();
    }

    @Override
    public Sequence sequence(String name) {
        return dspm.mockery().sequence(name);
    }
    @Override
    public States states(String name) {
        return dspm.mockery().states(name);
    }

}
