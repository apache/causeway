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
package org.apache.isis.regressiontests.cmdexecauditsess.jdo.integtests;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ActiveProfiles;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.extensions.audittrail.jdo.IsisModuleExtAuditTrailPersistenceJdo;
import org.apache.isis.extensions.commandlog.jdo.IsisModuleExtCommandLogPersistenceJdo;
import org.apache.isis.extensions.executionlog.jdo.IsisModuleExtExecutionLogPersistenceJdo;
import org.apache.isis.extensions.executionoutbox.jdo.IsisModuleExtExecutionOutboxPersistenceJdo;
import org.apache.isis.extensions.sessionlog.jdo.IsisModuleExtSessionLogPersistenceJdo;
import org.apache.isis.regressiontests.cmdexecauditsess.generic.integtest.CmdExecAuditSessLog_IntegTestAbstract;
import org.apache.isis.regressiontests.cmdexecauditsess.generic.integtest.model.CmdExecAuditSessTestDomainModel;
import org.apache.isis.regressiontests.cmdexecauditsess.jdo.integtests.model.Counter;
import org.apache.isis.regressiontests.cmdexecauditsess.jdo.integtests.model.CounterRepository;
import org.apache.isis.security.bypass.IsisModuleSecurityBypass;

@SpringBootTest(
        classes = CmdExecAuditSessLog_IntegTest.AppManifest.class
)
@ActiveProfiles("test")
public class CmdExecAuditSessLog_IntegTest extends CmdExecAuditSessLog_IntegTestAbstract {


    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            IsisModuleCoreRuntimeServices.class,
            IsisModuleSecurityBypass.class,
            IsisModuleExtCommandLogPersistenceJdo.class,
            IsisModuleExtExecutionLogPersistenceJdo.class,
            IsisModuleExtExecutionOutboxPersistenceJdo.class,
            IsisModuleExtAuditTrailPersistenceJdo.class,
            IsisModuleExtSessionLogPersistenceJdo.class,
    })
    @PropertySources({
            @PropertySource(IsisPresets.UseLog4j2Test)
    })
    @ComponentScan(basePackageClasses = {AppManifest.class, CmdExecAuditSessTestDomainModel.class, CounterRepository.class})
    public static class AppManifest {
    }


    protected org.apache.isis.regressiontests.cmdexecauditsess.generic.integtest.model.Counter newCounter(String name) {
        return Counter.builder().name(name).build();
    }

}
