package org.apache.causeway.viewer.graphql.model.domain;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

import graphql.schema.GraphQLObjectType;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.val;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvMeta {

    @UtilityClass
    static class Fields {
        static GraphQLFieldDefinition id =
                newFieldDefinition()
                        .name("id")
                        .type(nonNull(Scalars.GraphQLString))
                        .build();
        static GraphQLFieldDefinition logicalTypeName =
                newFieldDefinition()
                        .name("logicalTypeName")
                        .type(nonNull(Scalars.GraphQLString))
                        .build();
        static GraphQLFieldDefinition version =
                newFieldDefinition()
                        .name("version")
                        .type(Scalars.GraphQLString).build();
    }

    private final GqlvDomainObject domainObject;

    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;

    @Getter private final GraphQLFieldDefinition metaField;

    public GqlvMeta(
            final GqlvDomainObject domainObject,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService,
            final ObjectManager objectManager
    ) {
        this.domainObject = domainObject;

        this.codeRegistryBuilder = codeRegistryBuilder;
        this.bookmarkService = bookmarkService;
        this.objectManager = objectManager;

        metaField = newFieldDefinition().name("_gql_meta").type(metaType()).build();
    }

    public GraphQLObjectType getMetaType() {
        return (GraphQLObjectType) metaField.getType();
    }

    private GraphQLObjectType metaType() {
        val metaTypeBuilder = newObject().name(domainObject.getLogicalTypeNameSanitized() + "__meta");
        metaTypeBuilder.field(GqlvMeta.Fields.id);
        metaTypeBuilder.field(GqlvMeta.Fields.logicalTypeName);
        if (domainObject.getBeanSort() == BeanSort.ENTITY) {
            metaTypeBuilder.field(GqlvMeta.Fields.version);
        }
        return metaTypeBuilder.build();
    }

    public void addDataFetchers() {

        codeRegistryBuilder.dataFetcher(
                coordinates(domainObject.getGqlObjectType(), getMetaField()),
                (DataFetcher<Object>) environment -> {
                    return bookmarkService.bookmarkFor(environment.getSource())
                            .map(bookmark -> new org.apache.causeway.viewer.graphql.model.parts.GqlvMeta(bookmark, bookmarkService, objectManager))
                            .orElse(null); //TODO: is this correct ?
                });

        codeRegistryBuilder.dataFetcher(
                coordinates(getMetaType(), GqlvMeta.Fields.id),
                (DataFetcher<Object>) environment -> {
                    org.apache.causeway.viewer.graphql.model.parts.GqlvMeta gqlvMeta = environment.getSource();
                    return gqlvMeta.id();
                });

        codeRegistryBuilder.dataFetcher(
                coordinates(getMetaType(), GqlvMeta.Fields.logicalTypeName),
                (DataFetcher<Object>) environment -> {
                    org.apache.causeway.viewer.graphql.model.parts.GqlvMeta gqlvMeta = environment.getSource();
                    return gqlvMeta.logicalTypeName();
                });

        if (domainObject.getBeanSort() == BeanSort.ENTITY) {
            codeRegistryBuilder.dataFetcher(
                    coordinates(getMetaType(), GqlvMeta.Fields.version),
                    (DataFetcher<Object>) environment -> {
                        org.apache.causeway.viewer.graphql.model.parts.GqlvMeta gqlvMeta = environment.getSource();
                        return gqlvMeta.version();
                    });
        }
    }
}
