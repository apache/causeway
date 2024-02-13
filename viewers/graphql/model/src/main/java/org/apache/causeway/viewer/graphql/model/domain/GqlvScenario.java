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
import graphql.language.FieldDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.viewer.graphql.model.context.Context;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

/**
 * Exposes a domain service (view model or entity) via the GQL viewer.
 */
public class GqlvScenario
        extends GqlvAbstractCustom {

    public static final String KEY_SCENARIO_NAME = String.format("%s#%s", GqlvScenario.class.getName(), "name");

    private final GqlvScenarioName scenarioName;
    private final GqlvScenarioStep scenarioStep;

    public GqlvScenario(
            final Context context) {
        super("Scenario", context);

        this.scenarioName = new GqlvScenarioName(context);
        addChildField(scenarioName.getField());

        this.scenarioStep = new GqlvScenarioStep(context);
        addChildField(scenarioStep.newField("Given"));
        addChildField(scenarioStep.newField("When"));
        addChildField(scenarioStep.newField("Then"));

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
