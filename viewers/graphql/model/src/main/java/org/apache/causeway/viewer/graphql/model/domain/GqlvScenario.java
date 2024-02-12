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
package org.apache.causeway.viewer.graphql.model.domain;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.viewer.graphql.model.context.Context;

/**
 * Exposes a domain service (view model or entity) via the GQL viewer.
 */
public class GqlvScenario
        extends GqlvAbstractCustom
        implements GqlvScenarioName.Holder, GqlvScenarioGiven.Holder {

    private final Holder holder;

    private final Scenario scenarioPojo;

    private final GqlvScenarioName scenarioName;
    private final GqlvScenarioGiven scenarioGiven;

    public GqlvScenario(
            final GqlvScenario.Holder holder,
            final Context context) {
        super("Scenario", context);
        this.holder = holder;

        this.scenarioPojo = context.serviceRegistry.lookupService(Scenario.class).orElseThrow();

        this.scenarioName = new GqlvScenarioName(this, context);
        addChildField(scenarioName.getField());
        this.scenarioGiven = new GqlvScenarioGiven(this, context);
        addChildField(scenarioGiven.getField());

        buildObjectType();

        setField(new GraphQLFieldDefinition.Builder()
                            .name("Scenario")
                            .type(getGqlObjectType())
                            .argument(new GraphQLArgument.Builder()
                                                .name("name")
                                                .type(Scalars.GraphQLString)
                            )
                            .build()
        );
    }


    public void addDataFetchers(Holder holder) {
        context.codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(getField()),
                (DataFetcher<Object>) environment -> scenarioPojo);

        scenarioName.addDataFetchers(this);
        scenarioGiven.addDataFetchers();
    }


    @Override
    public String toString() {
        return scenarioPojo.toString();
    }

    public interface Holder
            extends GqlvHolder {
    }
}
