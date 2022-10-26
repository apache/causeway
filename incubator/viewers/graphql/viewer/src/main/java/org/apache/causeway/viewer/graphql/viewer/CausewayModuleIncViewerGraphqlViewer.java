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
package org.apache.causeway.viewer.graphql.viewer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;
import org.springframework.boot.autoconfigure.graphql.GraphQlCorsProperties;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.boot.autoconfigure.graphql.servlet.GraphQlWebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.graphql.execution.DefaultBatchLoaderRegistry;

import org.apache.causeway.viewer.graphql.model.CausewayModuleIncViewerGraphqlModel;

@Configuration
@Import({
        // @Service's

        // Modules
        CausewayModuleIncViewerGraphqlModel.class,

        // autoconfigurations
        GraphQlAutoConfiguration.class,
        GraphQlWebMvcAutoConfiguration.class
})
@EnableConfigurationProperties({
        GraphQlProperties.class, GraphQlCorsProperties.class
})
@ComponentScan
public class CausewayModuleIncViewerGraphqlViewer {

    @Bean
    @ConditionalOnMissingBean
    public BatchLoaderRegistry batchLoaderRegistry() {
        return new DefaultBatchLoaderRegistry();
    }
}

