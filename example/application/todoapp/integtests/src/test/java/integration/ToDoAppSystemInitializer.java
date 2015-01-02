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
package integration;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.integtestsupport.IsisSystemForTest;
import org.apache.isis.core.runtime.persistence.PersistenceConstants;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusPersistenceMechanismInstaller;
import org.apache.isis.objectstore.jdo.datanucleus.IsisConfigurationForJdoIntegTests;

/**
 * Holds an instance of an {@link IsisSystemForTest} as a {@link ThreadLocal} on the current thread,
 * initialized with ToDo app's domain services. 
 */
public class ToDoAppSystemInitializer {
    
    private ToDoAppSystemInitializer(){}

    public static IsisSystemForTest initIsft() {
        IsisSystemForTest isft = IsisSystemForTest.getElseNull();
        if(isft == null) {
            isft = new ToDoSystemBuilder().build().setUpSystem();
            IsisSystemForTest.set(isft);
        }
        return isft;
    }

    private static class ToDoSystemBuilder extends IsisSystemForTest.Builder {

        public ToDoSystemBuilder() {
            withLoggingAt(org.apache.log4j.Level.INFO);
            with(testConfiguration());
            with(new DataNucleusPersistenceMechanismInstaller());

            // services annotated with @DomainService
            withServicesIn("app"
                            ,"dom.todo"
                            ,"fixture.todo"
                            ,"webapp.admin"
                            ,"webapp.prototyping"
                            ,"org.apache.isis.core.wrapper"
                            ,"org.apache.isis.applib"
                            ,"org.apache.isis.core.metamodel.services"
                            ,"org.apache.isis.core.runtime.services"
                            ,"org.apache.isis.objectstore.jdo.datanucleus.service.support" // IsisJdoSupportImpl
                            ,"org.apache.isis.objectstore.jdo.datanucleus.service.eventbus" // EventBusServiceJdo
                            );
        }

        private static IsisConfiguration testConfiguration() {
            final IsisConfigurationForJdoIntegTests testConfiguration = new IsisConfigurationForJdoIntegTests();
            testConfiguration.addRegisterEntitiesPackagePrefix("dom");

            // enable stricter checking
            //
            // the consequence of this is having to call 'nextTransaction()' between most of the given/when/then's
            // because the command2 only ever refers to the event of the originating action.
            testConfiguration.put(PersistenceConstants.ENFORCE_SAFE_SEMANTICS, "true");

            return testConfiguration;
        }
    }
}
