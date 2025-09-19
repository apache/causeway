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
package org.apache.causeway.extensions.executionoutbox.jpa.integtests;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.extensions.executionoutbox.applib.integtest.ExecutionOutbox_IntegTestAbstract;
import org.apache.causeway.extensions.executionoutbox.applib.integtest.model.ExecutionOutboxTestDomainModel;
import org.apache.causeway.extensions.executionoutbox.jpa.CausewayModuleExtExecutionOutboxPersistenceJpa;
import org.apache.causeway.extensions.executionoutbox.jpa.integtests.model.Counter;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;

@SpringBootTest(
        classes = ExecutionOutbox_IntegTest.AppManifest.class
)
@ActiveProfiles("test")
public class ExecutionOutbox_IntegTest extends ExecutionOutbox_IntegTestAbstract {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            CausewayModuleCoreRuntimeServices.class,
            CausewayModuleSecurityBypass.class,
            CausewayModuleExtExecutionOutboxPersistenceJpa.class,
    })
    @EntityScan(basePackageClasses = {Counter.class})
    @ComponentScan(basePackageClasses = {AppManifest.class, ExecutionOutboxTestDomainModel.class})
    public static class AppManifest {
    }

    @Override
    protected org.apache.causeway.extensions.executionoutbox.applib.integtest.model.Counter newCounter(final String name) {
        return Counter.builder().name(name).build();
    }

}
