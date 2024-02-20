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
package org.apache.causeway.viewer.graphql.model.domain.rich;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GqlvActionInvokeArgsArg
        extends GqlvAbstract {

    @Getter private final Holder holder;
    @Getter private final ObjectActionParameter objectActionParameter;
    @Getter private final int paramNum;

    public GqlvActionInvokeArgsArg(
            final Holder holder,
            final ObjectActionParameter objectActionParameter,
            final Context context,
            final int paramNum) {
        super(context);

        this.holder = holder;
        this.objectActionParameter = objectActionParameter;
        this.paramNum = paramNum;

        val elementType = objectActionParameter.getElementType();;

        val gqlObjectTypeForElementType = context.typeMapper.outputTypeFor(elementType);
        if (gqlObjectTypeForElementType != null) {
            val gqlOutputType = objectActionParameter.isPlural()
                    ? GraphQLList.list(gqlObjectTypeForElementType)
                    : gqlObjectTypeForElementType;

            val fieldBuilder = newFieldDefinition()
                    .name(objectActionParameter.getId())
                    .type(gqlOutputType);
            setField(fieldBuilder.build());
        } else {
            setField(null);
        }
    }


    @Override
    protected Object fetchData(DataFetchingEnvironment dataFetchingEnvironment) {
        val environment = new Environment.ForTunnelled(dataFetchingEnvironment);
        val managedObjects = holder.argumentManagedObjectsFor(environment, holder.getObjectAction(), context.bookmarkService);
        return managedObjects.get(paramNum).map(ManagedObject::getPojo).orElse(null);
    }

    public interface Holder
            extends ObjectSpecificationProvider,
                    ObjectActionProvider {

        Can<ManagedObject> argumentManagedObjectsFor(
                Environment dataFetchingEnvironment,
                ObjectAction objectAction,
                BookmarkService bookmarkService);

    }
}
