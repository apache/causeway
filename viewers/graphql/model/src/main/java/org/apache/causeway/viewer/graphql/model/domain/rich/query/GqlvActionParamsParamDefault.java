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

import java.util.Collections;

import graphql.schema.DataFetchingEnvironment;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GqlvActionParamsParamDefault extends GqlvAbstract {
    private final HolderActionParamDetails holder;

    public GqlvActionParamsParamDefault(
            final HolderActionParamDetails holder,
            final Context context) {
        super(context);
        this.holder = holder;
        val objectActionParameter = holder.getObjectActionParameter();
        if (objectActionParameter.hasDefaults()) {
            val elementType = objectActionParameter.getElementType();
            val fieldBuilder = newFieldDefinition()
                    .name("default")
                    .type(context.typeMapper.outputTypeFor(elementType, holder.getSchemaType()));
            holder.addGqlArguments(holder.getObjectAction(), fieldBuilder, TypeMapper.InputContext.DEFAULT, holder.getParamNum());
            setField(fieldBuilder.build());
        } else {
            setField(null);
        }
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {
        val sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);
        val objectSpecification = context.specificationLoader.loadSpecification(sourcePojo.getClass());
        if (objectSpecification == null) {
            return Collections.emptyList();
        }
        val objectAction = holder.getObjectAction();
        val managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        val objectActionParameter = objectAction.getParameterById(holder.getObjectActionParameter().getId());
        val argumentManagedObjects = holder.argumentManagedObjectsFor(new Environment.For(dataFetchingEnvironment), objectAction, context.bookmarkService);
        val managedAction = ManagedAction.of(managedObject, objectAction, Where.ANYWHERE);
        val pendingArgs = ParameterNegotiationModel.of(managedAction, argumentManagedObjects);
        val defaultManagedObject = objectActionParameter.getDefault(pendingArgs);
        return defaultManagedObject.getPojo();
    }

}
