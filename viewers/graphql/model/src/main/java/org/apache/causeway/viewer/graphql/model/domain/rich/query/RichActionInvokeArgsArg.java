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

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ActionInteractor;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RichActionInvokeArgsArg
        extends Element {

    @Getter private final ActionInteractor actionInteractor;
    @Getter private final ObjectActionParameter objectActionParameter;
    @Getter private final int paramNum;

    public RichActionInvokeArgsArg(
            final ActionInteractor actionInteractor,
            final ObjectActionParameter oap,
            final Context context,
            final int paramNum) {
        super(context);

        this.actionInteractor = actionInteractor;
        this.objectActionParameter = oap;
        this.paramNum = paramNum;

        var elementType = oap.getElementType();;

        var gqlObjectTypeForElementType = context.typeMapper.outputTypeFor(elementType, actionInteractor.getSchemaType());
        if (gqlObjectTypeForElementType != null) {
            var gqlOutputType = oap.isPlural()
                    ? GraphQLList.list(gqlObjectTypeForElementType)
                    : gqlObjectTypeForElementType;

            var fieldBuilder = newFieldDefinition()
                    .name(oap.asciiId())
                    .type(gqlOutputType);
            setField(fieldBuilder.build());
        } else {
            setField(null);
        }
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment dataFetchingEnvironment) {
        var environment = new Environment.ForTunnelled(dataFetchingEnvironment);
        var managedObjects = actionInteractor.argumentManagedObjectsFor(environment, actionInteractor.getObjectMember(), context.bookmarkService);
        return managedObjects.get(paramNum).map(ManagedObject::getPojo).orElse(null);
    }

}
