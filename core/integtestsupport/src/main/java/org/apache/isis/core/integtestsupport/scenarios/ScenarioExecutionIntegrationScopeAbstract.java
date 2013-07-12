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

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.fixtures.InstallableFixture;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.integtestsupport.IsisSystemForTest;
import org.apache.isis.core.unittestsupport.scenarios.ScenarioExecution;


/**
 * An extension of {@link ScenarioExecution} for use within (coarse grained)
 * integration tests and Cucumber specs where there is back-end database.
 *
 * <p>
 * To this end it provides the ability to 
 * {@link #install(InstallableFixture...) install arbitrary fixtures} to
 * tear down/setup data, and also to methods to {@link #beginTran() begin}
 * or {@link #endTran(boolean) end} transactions. 
 */
public abstract class ScenarioExecutionIntegrationScopeAbstract extends ScenarioExecution  {

    protected final IsisSystemForTest isft;
    
    public ScenarioExecutionIntegrationScopeAbstract(IsisSystemForTest isft) {
        super(isft);
        this.isft = isft;
    }

    // //////////////////////////////////////

    /**
     * Convenience
     */
    public DomainObjectContainer getContainer() {
        return service(DomainObjectContainer.class);
    }

    /**
     * Convenience
     */
    public WrapperFactory getWrapperFactory() {
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