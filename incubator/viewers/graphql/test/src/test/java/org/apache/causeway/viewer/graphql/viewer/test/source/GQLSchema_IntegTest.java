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
package org.apache.causeway.viewer.graphql.viewer.test.source;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.viewer.source.GraphQlSourceForCauseway;
import org.apache.causeway.viewer.graphql.viewer.test.source.gqltestdomain.E1;
import org.apache.causeway.viewer.graphql.viewer.test.source.gqltestdomain.E2;
import org.apache.causeway.viewer.graphql.viewer.test.source.gqltestdomain.GQLTestDomainMenu;

import static org.apache.causeway.commons.internal.assertions._Assert.assertEquals;
import static org.apache.causeway.commons.internal.assertions._Assert.assertNotNull;
import static org.apache.causeway.commons.internal.assertions._Assert.assertTrue;

import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;

@Transactional
public class GQLSchema_IntegTest extends TestDomainModuleIntegTestAbstract{

    @Inject
    private CausewaySystemEnvironment causewaySystemEnvironment;

    @Inject
    private SpecificationLoader specificationLoader;

    @Inject
    private GraphQlSourceForCauseway graphQlSourceForCauseway;

    @BeforeEach
    void beforeEach() {
        assertNotNull(causewaySystemEnvironment);
        assertNotNull(specificationLoader);
        assertNotNull(graphQlSourceForCauseway);
    }

    @Test
    @Disabled
    void assert_stuff_works() {

        GraphQLSchema x;

//        _IocContainer iocContainer = causewaySystemEnvironment.getIocContainer();
//        iocContainer.streamAllBeans().forEach(b->{
//            System.out.println(b.getId());
//        });

        System.out.println(port);

        ObjectSpecification objectSpecification1 = specificationLoader.specForType(E1.class).get();
        assertNotNull(objectSpecification1);

        ObjectSpecification objectSpecification2 = specificationLoader.specForType(E2.class).get();
        assertNotNull(objectSpecification2);

        ObjectSpecification objectSpecification3 = specificationLoader.specForType(GQLTestDomainMenu.class).get();
        assertNotNull(objectSpecification3);

        GraphQL graphQL = graphQlSourceForCauseway.graphQl();
        GraphQLSchema graphQLSchema = graphQL.getGraphQLSchema();
//        List<GraphQLNamedType> allTypesAsList = graphQLSchema.getAllTypesAsList();
//        allTypesAsList.forEach(t->{
//            System.out.println(t.getName());
//        });

        assertTrue(graphQLSchema.containsType("gqltestdomain_E1"));
        assertTrue(graphQLSchema.containsType("gqltestdomain_E2"));
        assertTrue(graphQLSchema.containsType("gqltestdomain_GQLTestDomainMenu"));
        assertTrue(graphQLSchema.containsType("_gql_input__gqltestdomain_E1"));
        assertTrue(graphQLSchema.containsType("_gql_input__gqltestdomain_E2"));

        GraphQLType gqltestdomain_e1 = graphQLSchema.getType("gqltestdomain_E1");
        List<GraphQLSchemaElement> children = gqltestdomain_e1.getChildren();
        assertEquals(5, children.size());

        GraphQLObjectType gqltestdomain_e2 = (GraphQLObjectType) graphQLSchema.getType("gqltestdomain_E2");
        List<GraphQLFieldDefinition> fields = gqltestdomain_e2.getFields();
        assertEquals(10, fields.size());

        GraphQLFieldDefinition f6 = fields.get(5);
        assertEquals("otherE2List", f6.getName());
        Class<? extends GraphQLOutputType> f6TypeClass = f6.getType().getClass();
        assertEquals(GraphQLList.class, f6TypeClass);
        GraphQLList list = (GraphQLList) f6.getType();
        GraphQLTypeReference originalWrappedType = (GraphQLTypeReference) list.getOriginalWrappedType();
        assertEquals(originalWrappedType.getName(), gqltestdomain_e2.getName());

        GraphQLFieldDefinition f7 = fields.get(6);
        assertEquals("stringList", f7.getName());
        Class<? extends GraphQLOutputType> f7TypeClass = f7.getType().getClass();
        assertEquals(GraphQLList.class, f7TypeClass);
        GraphQLList list2 = (GraphQLList) f7.getType();
        GraphQLScalarType originalWrappedType2 = (GraphQLScalarType) list2.getOriginalWrappedType();
        assertEquals(Scalars.GraphQLString, originalWrappedType2);

        GraphQLFieldDefinition f8 = fields.get(7);
        assertEquals("zintList", f8.getName());
        Class<? extends GraphQLOutputType> f8TypeClass = f8.getType().getClass();
        assertEquals(GraphQLList.class, f8TypeClass);
        GraphQLList list3 = (GraphQLList) f8.getType();
        GraphQLScalarType originalWrappedType3 = (GraphQLScalarType) list3.getOriginalWrappedType();
        assertEquals(Scalars.GraphQLInt, originalWrappedType3);

        GraphQLFieldDefinition f9 = fields.get(8);
        assertEquals("otherEntities", f9.getName());
        Class<? extends GraphQLOutputType> f9TypeClass = f9.getType().getClass();
        assertEquals(GraphQLList.class, f9TypeClass);
        GraphQLList list4 = (GraphQLList) f9.getType();
        GraphQLTypeReference originalWrappedType4 = (GraphQLTypeReference) list4.getOriginalWrappedType();
        assertEquals("org_apache_causeway_viewer_graphql_viewer_source_gqltestdomain_TestEntity", originalWrappedType4.getName());

        GraphQLFieldDefinition f10 = fields.get(9);
        assertEquals("_gql_mutations", f10.getName());
        GraphQLObjectType mutationType = (GraphQLObjectType) f10.getType();
        assertEquals("gqltestdomain_E2__DomainObject_mutators", mutationType.getName());
        assertEquals(1, mutationType.getFields().size());
        GraphQLFieldDefinition graphQLFieldDefinition = mutationType.getFields().get(0);
        assertEquals("changeE1",graphQLFieldDefinition.getName());
        GraphQLArgument mutatorArgument = graphQLFieldDefinition.getArgument("e1");

        GraphQLType gqltestdomain_e1__domainObject_meta = graphQLSchema.getType("gqltestdomain_E1__DomainObject_meta");
        List<GraphQLSchemaElement> children1 = gqltestdomain_e1__domainObject_meta.getChildren();
        assertEquals(3, children1.size());

        GraphQLCodeRegistry codeRegistry = graphQLSchema.getCodeRegistry();
        assertNotNull(codeRegistry);

        // example of data fetches registered
        assertTrue(codeRegistry.hasDataFetcher(FieldCoordinates.coordinates("gqltestdomain_E1", "e2")));
        DataFetcher<?> dataFetcher = codeRegistry.getDataFetcher(FieldCoordinates.coordinates("gqltestdomain_E1", "e2"), (GraphQLFieldDefinition) gqltestdomain_e1.getChildren().get(0));
        assertNotNull(dataFetcher);


    }


}
