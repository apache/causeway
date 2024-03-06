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
package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;
import org.apache.causeway.viewer.graphql.model.domain.common.query.CommonTopLevelQueryAbstract;
import org.apache.causeway.viewer.graphql.model.domain.rich.scenario.Scenario;

public class RichTopLevelQuery
        extends CommonTopLevelQueryAbstract {

    private static final SchemaStrategy SCHEMA_STRATEGY = SchemaStrategy.RICH;

    private final Scenario scenario;

    public RichTopLevelQuery(final Context context) {
        super(SCHEMA_STRATEGY, context);

        var graphqlConfiguration = context.causewayConfiguration.getViewer().getGraphql();

        if (graphqlConfiguration.getSchema().getRich().isEnableScenarioTesting()) {
            addChildFieldFor(scenario = new Scenario(SCHEMA_STRATEGY, context));
        } else {
            scenario = null;
        }

        buildObjectType();

        // the field is used if the schemaStyle is 'SIMPLE_AND_RICH', but is ignored/unused otherwise
        setField(newFieldDefinition()
                .name(SCHEMA_STRATEGY.topLevelFieldNameFrom(graphqlConfiguration))
                .type(getGqlObjectType())
                .build());
    }

    @Override
    protected void addDataFetchersForChildren() {

        super.addDataFetchersForChildren();

        if (scenario != null) {
            scenario.addDataFetcher(this);
        }
    }
}
