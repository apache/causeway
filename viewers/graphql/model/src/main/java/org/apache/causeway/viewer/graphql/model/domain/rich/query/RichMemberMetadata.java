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

import java.util.LinkedHashMap;
import java.util.Map;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;

import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;

final class RichMemberMetadata extends Element {

    static final String TYPE_NAME = "RichMemberMetadata";

    private final ObjectFeature feature;
    private final boolean includeEditorConstraints;

    RichMemberMetadata(
            final Context context,
            final ObjectFeature feature,
            final boolean includeEditorConstraints) {
        super(context);
        this.feature = feature;
        this.includeEditorConstraints = includeEditorConstraints;
        setField(GraphQLFieldDefinition.newFieldDefinition()
                .name("metadata")
                .description("Canonical local member metadata.")
                .type(GraphQLNonNull.nonNull(metadataType(context)))
                .build());
    }

    private static GraphQLObjectType metadataType(final Context context) {
        var existing = context.graphQLTypeRegistry.lookup(TYPE_NAME, GraphQLObjectType.class);
        if (existing.isPresent()) {
            return existing.get();
        }
        var type = GraphQLObjectType.newObject()
                .name(TYPE_NAME)
                .description("Canonical local metadata shared by known rich member wrappers.")
                .field(field("friendlyName", "Canonical translated friendly name.",
                        GraphQLNonNull.nonNull(Scalars.GraphQLString)))
                .field(field("description", "Canonical translated description, or null.",
                        Scalars.GraphQLString))
                .field(field("cssClassFa", "Static action Font Awesome quick notation, or null.",
                        Scalars.GraphQLString))
                .field(field("cssClassFaPosition", "Static action Font Awesome LEFT or RIGHT position, or null.",
                        Scalars.GraphQLString))
                .field(field("areYouSure", "Whether canonical action semantics require confirmation, or null.",
                        Scalars.GraphQLBoolean))
                .field(field("promptStyle", "Resolved canonical action prompt-style enum name, or null.",
                        Scalars.GraphQLString))
                .field(field("maxLength", "Finite maximum input length, or null.",
                        Scalars.GraphQLInt))
                .field(field("pattern", "Java regular-expression text, or null.",
                        Scalars.GraphQLString))
                .field(field("patternFlags", "Java Pattern flags, or null.",
                        Scalars.GraphQLInt))
                .field(field("multiLine", "Requested multiline row count, or null.",
                        Scalars.GraphQLInt))
                .field(field("labelPosition", "Canonical label position.",
                        GraphQLNonNull.nonNull(Scalars.GraphQLString)))
                .field(field("typicalLength", "Positive typical input length, or null.",
                        Scalars.GraphQLInt))
                .build();
        context.graphQLTypeRegistry.addTypeIfNotAlreadyPresent(type);
        return context.graphQLTypeRegistry.lookup(TYPE_NAME, GraphQLObjectType.class).orElse(type);
    }

    private static GraphQLFieldDefinition field(
            final String name,
            final String description,
            final graphql.schema.GraphQLOutputType type) {
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(name)
                .description(description)
                .type(type)
                .build();
    }

    @Override
    protected Map<String, Object> fetchData(final DataFetchingEnvironment environment) {
        var values = new LinkedHashMap<String, Object>();
        values.put("friendlyName", feature.getCanonicalFriendlyName());
        values.put("description", feature.getCanonicalDescription().orElse(null));
        var staticIcon = staticActionIcon(feature);
        values.put("cssClassFa", staticIcon == null ? null : staticIcon.toQuickNotation());
        values.put("cssClassFaPosition", staticIcon == null || staticIcon.position() == null
                ? null
                : staticIcon.position().name());
        values.put("areYouSure", actionAreYouSure(feature));
        values.put("promptStyle", actionPromptStyle(feature));
        values.put("maxLength", includeEditorConstraints
                ? RichScalarMetadataField.finiteMaxLength(feature)
                : null);
        values.put("pattern", includeEditorConstraints
                ? RichScalarMetadataField.pattern(feature)
                : null);
        values.put("patternFlags", includeEditorConstraints
                ? RichScalarMetadataField.patternFlags(feature)
                : null);
        values.put("multiLine", includeEditorConstraints
                ? RichScalarMetadataField.multiLine(feature)
                : null);
        values.put("labelPosition", Facets.labelAt(feature).name());
        values.put("typicalLength", includeEditorConstraints
                ? RichScalarMetadataField.typicalLength(feature)
                : null);
        return values;
    }

    private static Boolean actionAreYouSure(final ObjectFeature feature) {
        if (!(feature instanceof ObjectAction action)) {
            return null;
        }
        return action.getSemantics() != null && action.getSemantics().isAreYouSure();
    }

    private static String actionPromptStyle(final ObjectFeature feature) {
        if (!(feature instanceof ObjectAction action)) {
            return null;
        }
        var promptStyle = action.getPromptStyle();
        return promptStyle == null ? null : promptStyle.name();
    }

    private static FontAwesomeLayers staticActionIcon(final ObjectFeature feature) {
        if (!(feature instanceof ObjectAction action)) {
            return null;
        }
        return action.lookupFacet(FaFacet.class)
                .flatMap(facet -> facet.getSpecialization().left())
                .map(staticFacet -> staticFacet.getLayers())
                .orElse(null);
    }
}
