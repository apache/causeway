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

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.rich.scenario.ScenarioStep;

import lombok.Getter;

public abstract class Element {

    protected final Context context;
    protected final CausewayConfiguration.Viewer.Graphql graphqlConfiguration;

    /**
     * Usually populated, being the field that will be added to the parent's type
     *
     * <p>
     *     However, {@link ScenarioStep} is an exception; it doesn't populate this field - instead
     *     {@link ElementCustom#newField(String, String)} is used to create multiple fields for the type.
     * </p>
     */
    @Getter
    private GraphQLFieldDefinition field;

    protected Element(final Context context) {
        this.context = context;
        this.graphqlConfiguration = this.context.causewayConfiguration.getViewer().getGraphql();
    }

    protected final GraphQLFieldDefinition setField(final GraphQLFieldDefinition field) {
        this.field = field;
        return field;
    }

    public final void addDataFetcher(Parent parent) {
        if (getField() != null) {
            // in some cases there might not be a field, eg RichCollectionGet for DomainObjectList#objects
            context.codeRegistryBuilder.dataFetcher(
                    parent.coordinatesFor(getField()),
                    this::fetchData);
        }

        addDataFetchersForChildren();
    }

    /**
     * Use the provided fieldName rather than that of {@link #getField()}.
     *
     * <p>
     *     Used to allow multiple fields of the same type, eg {@link ScenarioStep}.
     * </p>
     */
    public final void addDataFetcher(Parent parent, String fieldName) {
        context.codeRegistryBuilder.dataFetcher(
                parent.coordinatesFor(fieldName),
                this::fetchData);

        addDataFetchersForChildren();
    }

    protected void addDataFetchersForChildren() {
    }

    protected abstract Object fetchData(DataFetchingEnvironment environment);
}
