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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;

import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLUnionType.newUnionType;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.graphql.model.application.ApplicationEntryService.ApplicationSnapshot;
import org.apache.causeway.viewer.graphql.model.application.ApplicationEntryService.Issue;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

public class RichApplicationEntry extends Element {

    private static final String ENTRY_TYPE = "rich__gqlv_application_entry";
    private static final String MENU_BARS_TYPE = "rich__gqlv_application_menu_bars";
    private static final String HOME_TYPE = "rich__gqlv_application_home";
    private static final String HOME_KIND_TYPE = "rich__gqlv_application_home_kind";
    private static final String HOME_OBJECT_TYPE = "rich__gqlv_application_home_object";
    private static final String ISSUE_TYPE = "rich__gqlv_application_issue";

    private final boolean includeMenuBars;

    public RichApplicationEntry(final Context context) {
        super(context);
        this.includeMenuBars = graphqlConfiguration.resources().effectiveStructuralMetadataResponseType()
                != CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN;

        var issueType = issueType(context);
        var entryBuilder = newObject()
                .name(ENTRY_TYPE)
                .description("Authorized framework-neutral application entry points.");
        if (includeMenuBars) {
            entryBuilder.field(newFieldDefinition()
                    .name("menuBars")
                    .description("The authorized effective menu-bars structural resource.")
                    .type(menuBarsType(context)));
        }
        entryBuilder.field(newFieldDefinition()
                .name("home")
                .description("The supported configured object home page, when available.")
                .type(homeType(context)));
        entryBuilder.field(newFieldDefinition()
                .name("issues")
                .description("Bounded non-disclosing application-entry diagnostics.")
                .type(nonNull(GraphQLList.list(nonNull(issueType)))));
        var entryType = entryBuilder.build();
        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(entryType);
        setField(newFieldDefinition()
                .name("application")
                .description("Discovers authorized menu bars and the configured object home page.")
                .type(nonNull(entryType))
                .build());
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment environment) {
        var snapshot = context.applicationEntryService.applicationSnapshot(includeMenuBars);
        var home = snapshot.home();
        if (home == null) {
            return snapshot;
        }
        var homeSpecification = context.specificationLoader.loadSpecification(home.object().getClass());
        var homeTypeName = homeSpecification != null
                ? TypeNames.objectTypeNameFor(homeSpecification, SchemaType.RICH)
                : null;
        if (homeTypeName != null && context.domainObjectByTypeName.containsKey(homeTypeName)) {
            return snapshot;
        }
        var issues = new ArrayList<>(snapshot.issues());
        issues.add(new Issue("HOME_UNAVAILABLE", "The configured home entry is unavailable."));
        return new ApplicationSnapshot(snapshot.menuBars(), null, List.copyOf(issues));
    }

    private static GraphQLObjectType menuBarsType(final Context context) {
        return context.graphQLTypeRegistry.lookup(MENU_BARS_TYPE, GraphQLObjectType.class)
                .orElseGet(() -> {
                    var type = newObject()
                            .name(MENU_BARS_TYPE)
                            .description("Reference metadata for the effective menu-bars XML resource.")
                            .field(newFieldDefinition().name("href").type(nonNull(Scalars.GraphQLString)))
                            .field(newFieldDefinition().name("mediaType").type(nonNull(Scalars.GraphQLString)))
                            .field(newFieldDefinition().name("formatVersion").type(nonNull(Scalars.GraphQLString)))
                            .field(newFieldDefinition().name("generation").type(nonNull(Scalars.GraphQLString)))
                            .field(newFieldDefinition().name("cacheControl").type(nonNull(Scalars.GraphQLString)))
                            .build();
                    context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
                    return type;
                });
    }

    private static GraphQLObjectType issueType(final Context context) {
        return context.graphQLTypeRegistry.lookup(ISSUE_TYPE, GraphQLObjectType.class)
                .orElseGet(() -> {
                    var type = newObject()
                            .name(ISSUE_TYPE)
                            .description("A bounded non-disclosing application-entry issue.")
                            .field(newFieldDefinition().name("code").type(nonNull(Scalars.GraphQLString)))
                            .field(newFieldDefinition().name("message").type(nonNull(Scalars.GraphQLString)))
                            .build();
                    context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
                    return type;
                });
    }

    private static GraphQLObjectType homeType(final Context context) {
        return context.graphQLTypeRegistry.lookup(HOME_TYPE, GraphQLObjectType.class)
                .orElseGet(() -> {
                    var kindType = newEnum()
                            .name(HOME_KIND_TYPE)
                            .description("Supported application home entry kind.")
                            .value("OBJECT")
                            .build();
                    context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(kindType);
                    var type = newObject()
                            .name(HOME_TYPE)
                            .description("The current supported object home page.")
                            .field(newFieldDefinition().name("kind").type(nonNull(kindType)))
                            .field(newFieldDefinition().name("logicalTypeName").type(nonNull(Scalars.GraphQLString)))
                            .field(newFieldDefinition().name("object").type(nonNull(homeObjectType(context))))
                            .build();
                    context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
                    return type;
                });
    }

    private static GraphQLUnionType homeObjectType(final Context context) {
        return context.graphQLTypeRegistry.lookup(HOME_OBJECT_TYPE, GraphQLUnionType.class)
                .orElseGet(() -> {
                    var possibleSpecifications = context.objectSpecifications(RichApplicationEntry::isConcreteObject);
                    var builder = newUnionType()
                            .name(HOME_OBJECT_TYPE)
                            .description("Concrete rich object types eligible as an application home page.");
                    possibleSpecifications.stream()
                            .map(specification -> TypeNames.objectTypeNameFor(specification, SchemaType.RICH))
                            .distinct()
                            .map(GraphQLTypeReference::typeRef)
                            .forEach(builder::possibleType);
                    var unionType = context.graphQLTypeRegistry.addUnionTypeIfNotAlreadyPresent(builder.build());
                    context.codeRegistryBuilder.typeResolver(unionType, environment -> {
                        var pojo = environment.getObject();
                        if (pojo instanceof BookmarkedPojo bookmarkedPojo) {
                            pojo = bookmarkedPojo.getTargetPojo();
                        }
                        if (pojo instanceof ManagedObject managedObject) {
                            pojo = managedObject.getPojo();
                        }
                        if (pojo == null) {
                            return null;
                        }
                        var specification = context.specificationLoader.loadSpecification(pojo.getClass());
                        return specification != null && isConcreteObject(specification)
                                ? environment.getSchema().getObjectType(
                                        TypeNames.objectTypeNameFor(specification, SchemaType.RICH))
                                : null;
                    });
                    return unionType;
                });
    }

    private static boolean isConcreteObject(final ObjectSpecification specification) {
        return specification.isEntityOrViewModel()
                && !Modifier.isAbstract(specification.getCorrespondingClass().getModifiers());
    }
}
