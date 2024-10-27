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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.ElementCustom;
import org.apache.causeway.viewer.graphql.model.domain.Parent;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;
import org.apache.causeway.viewer.graphql.model.domain.common.query.CommonDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.common.query.CommonDomainService;

public class ScenarioStep
        extends ElementCustom
        implements Parent {

    private final List<CommonDomainService> domainServices = new ArrayList<>();
    private final List<CommonDomainObject> domainObjects = new ArrayList<>();

    public ScenarioStep(
            final SchemaStrategy schemaStrategy,
            final Context context) {
        super("ScenarioStep", context);

        if(isBuilt()) {
            return;
        }

        // add domain object lookup to top-level query
        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:     // @DomainObject(nature=ENTITY)

                    var gqlvDomainObject = schemaStrategy.domainObjectFor(objectSpec, context);
                    addChildField(gqlvDomainObject.newField());
                    domainObjects.add(gqlvDomainObject);

                    break;
            }
        });

        context.objectSpecifications().forEach(objectSpec -> {
            if (Objects.requireNonNull(objectSpec.getBeanSort()) == BeanSort.MANAGED_BEAN_CONTRIBUTING) { // @DomainService
                context.serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
                        .ifPresent(servicePojo -> domainServices.add(addChildFieldFor(schemaStrategy.domainServiceFor(objectSpec, servicePojo, context))));
            }
        });

        buildObjectType();
    }

    protected void addDataFetchersForChildren() {
        domainServices.forEach(domainService -> {
            boolean actionsAdded = domainService.hasActions();
            if (actionsAdded) {
                domainService.addDataFetcher(this);
            }
        });

        domainObjects.forEach(domainObject -> domainObject.addDataFetcher(this));
    }

    @Override
    protected DataFetchingEnvironment fetchData(DataFetchingEnvironment environment) {
        return environment;
    }

}
