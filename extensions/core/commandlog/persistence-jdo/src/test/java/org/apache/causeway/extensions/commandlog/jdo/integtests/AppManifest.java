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
package org.apache.causeway.extensions.commandlog.jdo.integtests;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.extensions.commandlog.applib.integtest.model.CommandLogTestDomainModel;
import org.apache.causeway.extensions.commandlog.jdo.CausewayModuleExtCommandLogPersistenceJdo;
import org.apache.causeway.extensions.commandlog.jdo.integtests.model.CounterRepository;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({
        CausewayModuleCoreRuntimeServices.class,
        CausewayModuleSecurityBypass.class,
        CausewayModuleExtCommandLogPersistenceJdo.class,
})
@PropertySources({
        @PropertySource(CausewayPresets.UseLog4j2Test)
})
@ComponentScan(basePackageClasses = {AppManifest.class, CommandLogTestDomainModel.class, CounterRepository.class})
public class AppManifest {
}
