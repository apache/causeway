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
package org.apache.causeway.viewer.graphql.model.context;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;

import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLEnumValueDefinition.newEnumValueDefinition;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.id.HasLogicalType;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.query.CommonDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.common.query.CommonDomainService;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Context {

    public final GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
    public final BookmarkService bookmarkService;
    public final SpecificationLoader specificationLoader;
    public final TypeMapper typeMapper;
    public final ServiceRegistry serviceRegistry;
    public final CausewayConfiguration causewayConfiguration;
    public final CausewaySystemEnvironment causewaySystemEnvironment;
    public final ObjectManager objectManager;
    public final GraphQLTypeRegistry graphQLTypeRegistry;

    public final Map<String, CommonDomainService> domainServiceByTypeName = new LinkedHashMap<>();
    public final Map<String, CommonDomainObject> domainObjectByTypeName = new LinkedHashMap<>();

    private GraphQLEnumType logicalTypeNames;

    public GraphQLEnumType getLogicalTypeNames() {
        if (logicalTypeNames == null) {
            computeLogicalTypeNames();
        }
        return logicalTypeNames;
    }

    public ImmutableEnumSet<ActionScope> getActionScope() {
        return causewaySystemEnvironment.getDeploymentType().isProduction()
                ? ActionScope.PRODUCTION_ONLY
                : ActionScope.ANY;
    }

    public List<ObjectSpecification> objectSpecifications() {
        return objectSpecifications(spec -> true);
    }

    public List<ObjectSpecification> objectSpecifications(final Predicate<ObjectSpecification> predicate) {
        var includeEntities = causewayConfiguration.getViewer().getGraphql().getApiScope() == CausewayConfiguration.Viewer.Graphql.ApiScope.ALL;
        return specificationLoader.snapshotSpecifications()
                .filter(x -> x.getCorrespondingClass().getPackage() != Either.class.getPackage())   // exclude the org.apache_causeway.commons.functional
                .distinct((a, b) -> a.getLogicalTypeName().equals(b.getLogicalTypeName()))
                .filter(x ->
                           x.isViewModel()
                        || (includeEntities && x.isEntity())
                        || (includeEntities && x.isAbstract()) // this is a little bit inaccurate; Person.class was not being picked up, not sure that MappedSuperclass is enough to install the EntityFacet though.
                        || x.getBeanSort().isManagedBeanContributing()
                )
                .filter(predicate)
                .sorted(Comparator.comparing(HasLogicalType::getLogicalTypeName))
                .toList();
    }

    private void computeLogicalTypeNames() {
        if (logicalTypeNames != null) {
            return;
        }
        logicalTypeNames = doComputeLogicalTypeNames();
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(logicalTypeNames);
    }

    private GraphQLEnumType doComputeLogicalTypeNames() {
        var entitiesOrViewModels = objectSpecifications(ObjectSpecification::isEntityOrViewModel);
        return newEnum()
                .name("logicalTypeNames__gqlv_enum")  // TODO: look this up from causeway configuration?
                .values(entitiesOrViewModels.stream()
                        .map(objectSpec -> newEnumValueDefinition()
                                .name(TypeNames.objectTypeFieldNameFor(objectSpec))
                                .description(objectSpec.getLogicalTypeName())
                                .value(objectSpec)
                                .build()).collect(Collectors.toList())
                )
                .build();
    }
}
