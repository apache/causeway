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
package org.apache.causeway.viewer.graphql.model.domain.common.query;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.ElementCustom;
import org.apache.causeway.viewer.graphql.model.domain.Parent;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;

import lombok.Getter;

public abstract class CommonTopLevelQueryAbstract
        extends ElementCustom
        implements Parent {

    @Getter private final SchemaStrategy schemaStrategy;

    private final List<Element> domainServices = new ArrayList<>();
    private final List<ElementCustom> domainObjects = new ArrayList<>();

    public CommonTopLevelQueryAbstract(
            final SchemaStrategy schemaStrategy,
            final Context context) {
        super(schemaStrategy.getSchemaType().name() + "Schema", context);
        this.schemaStrategy = schemaStrategy;

        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL:
                case ENTITY:
                    var gqlvDomainObject = schemaStrategy.domainObjectFor(objectSpec, context);
                    addChildField(gqlvDomainObject.newField());
                    domainObjects.add(gqlvDomainObject);
                    break;

            }
        });

        // add services to top-level query
        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {
                case MANAGED_BEAN_CONTRIBUTING: // @DomainService
                    context.serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
                            .ifPresent(servicePojo ->
                                    domainServices.add(
                                            addChildFieldFor(schemaStrategy.domainServiceFor(objectSpec, servicePojo, context))));
                    break;
            }
        });
    }

    public static List<ObjectSpecification> superclassesOf(final ObjectSpecification objectSpecification) {
        var superclasses = new ArrayList<ObjectSpecification>();
        ObjectSpecification superclass = objectSpecification.superclass();
        while (superclass != null && superclass.getCorrespondingClass() != Object.class) {
            superclasses.add(0, superclass);
            superclass = superclass.superclass();
        }
        return superclasses;
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        return environment;
    }

    public void addDataFetchers() {
        addDataFetchersForChildren();
    }

    @Override
    protected void addDataFetchersForChildren() {
        domainServices.forEach(domainService -> domainService.addDataFetcher(this));
        domainObjects.forEach(domainObject -> domainObject.addDataFetcher(this));
    }
}
