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
package org.apache.isis.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;

@Configuration
@Import({
    //IsisConfiguration.class // not required
    IsisConfiguration.PatternsConverter.class,
    IsisSystemEnvironment.class
})
@EnableConfigurationProperties(IsisConfiguration.class)
public class IsisConfigModule {
    
    @SuppressWarnings("ConfigurationProperties") // think this is not really an issue...
    @ConfigurationProperties(prefix = "isis")
    @Bean("isis-settings")
    public Map<String, String> getAsMap() {
        return new HashMap<>();
    }

}
