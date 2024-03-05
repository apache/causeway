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
package org.apache.causeway.viewer.graphql.viewer.test.schema;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.viewer.test.domain.UniversityModule;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.apache.causeway.viewer.graphql.viewer.testsupport.schema.PrintSchemaIntegTestAbstract;

import static org.apache.causeway.core.config.CausewayConfiguration.Viewer.Graphql.ApiVariant;

@Import({
        UniversityModule.class
})
public class PrintSchemaIntegTest extends PrintSchemaIntegTestAbstract {

    @DynamicPropertySource
    static void apiVariant(DynamicPropertyRegistry registry) {
        registry.add("causeway.viewer.graphql.api-variant", CausewayConfiguration.Viewer.Graphql.ApiVariant.QUERY_WITH_MUTATIONS_NON_SPEC_COMPLIANT::name);
        registry.add("causeway.viewer.graphql.schema.rich.enable-scenario-testing", () -> Boolean.TRUE);
        registry.add("causeway.viewer.graphql.resources.response-type", CausewayConfiguration.Viewer.Graphql.ResponseType.ATTACHMENT::name);
    }

}
