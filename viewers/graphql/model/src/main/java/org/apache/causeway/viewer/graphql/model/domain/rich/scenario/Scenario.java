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
package org.apache.causeway.viewer.graphql.model.domain.rich.scenario;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.ElementCustom;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;

/**
 * Exposes a domain service (view model or entity) via the GQL viewer.
 */
public class Scenario
        extends ElementCustom {

    public static final String KEY_SCENARIO_NAME = String.format("%s#%s", Scenario.class.getName(), "name");

    private final ScenarioName scenarioName;
    private final ScenarioStep scenarioStep;

    public Scenario(
            final SchemaStrategy schemaStrategy,
            final Context context) {
        super("Scenario", context);

        addChildFieldFor(this.scenarioName = new ScenarioName(context));

        this.scenarioStep = new ScenarioStep(schemaStrategy, context);
        addChildField(scenarioStep.newField("Given", "Given the system under test has this initial state"));
        addChildField(scenarioStep.newField("When", "When the system is changed in some particular way"));
        addChildField(scenarioStep.newField("Then", "Then the system has this resultant state"));

        buildObjectType();

        setField(newFieldDefinition()
                    .name("Scenario")
                    .type(getGqlObjectType())
                    .argument(new GraphQLArgument.Builder()
                                        .name("name")
                                        .type(Scalars.GraphQLString)
                    )
                    .build()
        );
    }

    @Override
    protected void addDataFetchersForChildren() {
        scenarioName.addDataFetcher(this);
        scenarioStep.addDataFetcher(this, "Given");
        scenarioStep.addDataFetcher(this, "When");
        scenarioStep.addDataFetcher(this, "Then");
    }

    @Override
    protected DataFetchingEnvironment fetchData(DataFetchingEnvironment environment) {
        String scenarioName = environment.getArgument("name");
        environment.getGraphQlContext().put(KEY_SCENARIO_NAME, scenarioName);
        return environment;
    }
}
