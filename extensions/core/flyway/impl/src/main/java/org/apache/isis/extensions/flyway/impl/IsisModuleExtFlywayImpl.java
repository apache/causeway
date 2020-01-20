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
package org.apache.isis.extensions.flyway.impl;

import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.IsisModuleCoreConfig;
import org.apache.isis.extensions.flyway.impl.config.FlywayConfigurationCustomizerFromIsisConfiguration;

@Configuration
@Import({
        IsisModuleCoreConfig.class
})
@ComponentScan
public class IsisModuleExtFlywayImpl {

    @Bean
    public FlywayConfigurationCustomizer flywayConfigurationCustomizer(final IsisConfiguration isisConfiguration) {
        return new FlywayConfigurationCustomizerFromIsisConfiguration(isisConfiguration);
    }


}
