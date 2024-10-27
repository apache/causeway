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
package org.apache.causeway.viewer.graphql.viewer.testsupport.schema;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;

import graphql.schema.idl.SchemaPrinter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.viewer.integration.GraphQlSourceForCauseway;
import org.apache.causeway.viewer.graphql.viewer.testsupport.CausewayViewerGraphqlIntegTestAbstract;

import static org.apache.causeway.commons.internal.assertions._Assert.assertNotNull;

/**
 * Utility to print out the schema, to <code>src/test/resources</code> of the implementing subclass.
 *
 * <p>
 *      IDEs can then detect this and use it to provide intellisense/code-completion for GraphQL queries,
 *      eg used by tests.
 * </p>
 *
 * <p>
 *     If the {@link org.apache.causeway.core.config.CausewayConfiguration.Viewer.Graphql.ApiVariant ApiVariant} is to
 *     be overridden from the framework's default, use for example Spring's @{@link DynamicPropertySource} annotation.
 * </p>
 *
 * <p>
 * For example:
 *
 * <pre>
 * import static org.apache.causeway.core.config.CausewayConfiguration.Viewer.Graphql;
 *
 * public class PrintSchemaIntegTest extends PrintSchemaIntegTestAbstract {
 *    {@literal @}DynamicPropertySource
 *     static void apiVariant(DynamicPropertyRegistry registry) {
 *         registry.add(
 *              "causeway.viewer.graphql.api-variant",
 *              Graphql.ApiVariant.QUERY_WITH_MUTATIONS_NON_SPEC_COMPLIANT::name);
 *     }
 * }
 * </pre>
 * </p>
 *
 * @since 2.0 {@index}
 */
@Transactional
public abstract class PrintSchemaIntegTestAbstract extends CausewayViewerGraphqlIntegTestAbstract {

    @Inject private CausewaySystemEnvironment causewaySystemEnvironment;
    @Inject private SpecificationLoader specificationLoader;
    @Inject private GraphQlSourceForCauseway graphQlSourceForCauseway;

    public PrintSchemaIntegTestAbstract() {
        super(PrintSchemaIntegTestAbstract.class);
    }

    @Override
    @BeforeEach
    protected void beforeEach() {
        assertNotNull(causewaySystemEnvironment);
        assertNotNull(specificationLoader);
        assertNotNull(graphQlSourceForCauseway);
    }

    @Test
    protected void schema() throws Exception {

        var graphQL = graphQlSourceForCauseway.graphQl();
        var graphQLSchema = graphQL.getGraphQLSchema();

        var printer = new SchemaPrinter();

        var submit = printer.print(graphQLSchema);

        var targetFile = new File("src/test/resources/schema.gql");

        Files.write(Paths.get(targetFile.getPath()), submit.getBytes());
    }

}
