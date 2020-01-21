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
package org.apache.isis.core.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.commons.IsisModuleCoreCommons;
import org.apache.isis.core.config.beans.IsisBeanFactoryPostProcessorForSpring;
import org.apache.isis.core.config.converters.PatternsConverter;
import org.apache.isis.core.config.validators.PatternOptionalStringConstraintValidator;
import org.apache.isis.core.config.viewer.wicket.WebAppContextPath;

@Configuration
@Import({
    // modules
    IsisModuleCoreCommons.class,

    // @Component's
    PatternsConverter.class,
    IsisBeanFactoryPostProcessorForSpring.class,
    PatternOptionalStringConstraintValidator.class,

    // @Service's
    WebAppContextPath.class,
})
@EnableConfigurationProperties({
        IsisConfiguration.class,
        RestEasyConfiguration.class,
})
public class IsisModuleCoreConfig {
    
    @ConfigurationProperties(prefix = "isis")
    @Bean("isis-settings")
    public Map<String, String> getIsisConfigProps() {
        return new HashMap<>();
    }

    @ConfigurationProperties(prefix = "resteasy")
    @Bean("resteasy-settings")
    public Map<String, String> getResteasyConfigProps() {
        return new HashMap<>();
    }


}
