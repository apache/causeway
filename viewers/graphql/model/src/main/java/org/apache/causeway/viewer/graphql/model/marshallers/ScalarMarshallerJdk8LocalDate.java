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
package org.apache.causeway.viewer.graphql.model.marshallers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.annotation.Priority;
import javax.inject.Inject;

import graphql.Scalars;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.applib.marshallers.ScalarMarshallerAbstract;

@Component
@Priority(PriorityPrecedence.LATE)
public class ScalarMarshallerJdk8LocalDate extends ScalarMarshallerAbstract<LocalDate> {

    private final CausewayConfiguration.Viewer.Graphql.ScalarMarshaller scalarMarshallerConfig;

    @Inject
    public ScalarMarshallerJdk8LocalDate(final CausewayConfiguration causewayConfiguration) {
        super(LocalDate.class, Scalars.GraphQLString, causewayConfiguration);
        scalarMarshallerConfig = causewayConfiguration.getViewer().getGraphql().getScalarMarshaller();
    }

    @Override
    public LocalDate unmarshal(Object graphValue, Class<?> targetType) {
        String argumentStr = (String) graphValue;
        return LocalDate.parse(argumentStr, DateTimeFormatter.ofPattern(scalarMarshallerConfig.getLocalDateFormat()));
    }
}
