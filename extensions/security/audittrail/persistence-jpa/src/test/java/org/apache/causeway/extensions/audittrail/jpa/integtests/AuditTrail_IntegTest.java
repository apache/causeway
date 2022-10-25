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
package org.apache.causeway.extensions.audittrail.jpa.integtests;

import javax.inject.Inject;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.extensions.audittrail.applib.integtests.AuditTrail_IntegTestAbstract;
import org.apache.causeway.extensions.audittrail.applib.integtests.model.AuditTrailTestDomainModel;
import org.apache.causeway.extensions.audittrail.jpa.CausewayModuleExtAuditTrailPersistenceJpa;
import org.apache.causeway.extensions.audittrail.jpa.integtests.model.Counter;
import org.apache.causeway.extensions.audittrail.jpa.integtests.model.CounterRepository;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;

@SpringBootTest(
        classes = AuditTrail_IntegTest.AppManifest.class,
        properties = {
                "logging.level.org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener=DEBUG",
        }
)
@ActiveProfiles("test")
public class AuditTrail_IntegTest extends AuditTrail_IntegTestAbstract {


    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            CausewayModuleCoreRuntimeServices.class,
            CausewayModuleSecurityBypass.class,
            CausewayModuleExtAuditTrailPersistenceJpa.class,

            // entities, eager meta-model introspection
            Counter.class,
    })
    @PropertySources({
            @PropertySource(CausewayPresets.UseLog4j2Test),
    })
    @ComponentScan(basePackageClasses = {AppManifest.class, AuditTrailTestDomainModel.class, CounterRepository.class})
    @EntityScan(basePackageClasses = {Counter.class})
    public static class AppManifest {
    }

    @Override
    protected org.apache.causeway.extensions.audittrail.applib.integtests.model.Counter newCounter(final String name) {
        return Counter.builder().name(name).build();
    }

    @Inject CausewayBeanTypeRegistry causewayBeanTypeRegistry;
}
