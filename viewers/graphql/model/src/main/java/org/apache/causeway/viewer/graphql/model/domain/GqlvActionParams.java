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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GqlvActionParams
        extends GqlvAbstractCustom
        implements GqlvActionParamsParam.Holder {

    @Getter private final Holder holder;

    private final List<GqlvActionParamsParam> params = new ArrayList<>();

    public GqlvActionParams(
            final Holder holder,
            final Context context) {
        super(TypeNames.actionParamsTypeNameFor(holder.getObjectSpecification(), holder.getObjectAction()), context);
        this.holder = holder;

        if (isBuilt()) {
            // nothing else to be done
            return;
        }

        val idx = new AtomicInteger(0);
        holder.getObjectAction().getParameters().forEach(objectActionParameter -> {
            val gqlvActionParam = new GqlvActionParamsParam(this, objectActionParameter, this.context, idx.getAndIncrement());
            addChildFieldFor(gqlvActionParam);
            params.add(gqlvActionParam);
        });

        if (hasParams()) {
            buildObjectTypeAndField("params");
        }
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return holder.getObjectSpecification();
    }

    @Override
    public ObjectAction getObjectMember() {
        return getObjectAction();
    }

    @Override
    public ObjectAction getObjectAction() {
        return holder.getObjectAction();
    }

    public boolean hasParams() {
        return !params.isEmpty();
    }

    @Override
    protected void addDataFetchersForChildren() {
        params.forEach(param -> param.addDataFetcher(this));
    }


    @Override
    protected Object fetchData(DataFetchingEnvironment dataFetchingEnvironment) {
        return BookmarkedPojo.sourceFrom(dataFetchingEnvironment, context);
    }

    @Override
    public void addGqlArguments(
            ObjectAction objectAction, GraphQLFieldDefinition.Builder fieldBuilder, TypeMapper.InputContext inputContext, int paramNum) {
        holder.addGqlArguments(objectAction, fieldBuilder, inputContext, paramNum);
    }

    @Override
    public Can<ManagedObject> argumentManagedObjectsFor(
            Environment dataFetchingEnvironment,
            ObjectAction objectAction,
            BookmarkService bookmarkService) {
        return holder.argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, bookmarkService);
    }

    public interface Holder
            extends ObjectSpecificationProvider,
                    ObjectActionProvider {

        void addGqlArguments(
                ObjectAction objectAction,
                GraphQLFieldDefinition.Builder fieldBuilder,
                TypeMapper.InputContext inputContext,
                int paramNum);

        Can<ManagedObject> argumentManagedObjectsFor(
                Environment dataFetchingEnvironment,
                ObjectAction objectAction,
                BookmarkService bookmarkService);
    }
}
