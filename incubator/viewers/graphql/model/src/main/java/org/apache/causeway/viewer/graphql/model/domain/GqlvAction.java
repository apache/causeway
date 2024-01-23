package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLObjectType;

import lombok.extern.log4j.Log4j2;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Log4j2
public class GqlvAction extends GqlvMember<ObjectAction, GqlvActionHolder> implements GqlvActionInvokeHolder {

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final GraphQLObjectType gqlObjectType;
    private final GqlvActionInvoke invoke;
    private final BookmarkService bookmarkService;

    public GqlvAction(
            final GqlvActionHolder holder,
            final ObjectAction objectAction,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService
            ) {
        super(holder, objectAction, codeRegistryBuilder);

        gqlObjectTypeBuilder = newObject().name(TypeNames.actionTypeNameFor(objectAction));

        this.invoke = new GqlvActionInvoke(this, codeRegistryBuilder);
        this.bookmarkService = bookmarkService;

        gqlObjectType = gqlObjectTypeBuilder.build();

        final GraphQLFieldDefinition field = newFieldDefinition()
                .name(objectAction.getId())
                .type(gqlObjectTypeBuilder)
                .build();

        holder.addField(field);

        setField(field);
    }


    public ObjectAction getObjectAction() {
        return getObjectMember();
    }

    @Override
    public void addField(GraphQLFieldDefinition fieldDefinition) {
        gqlObjectTypeBuilder.field(fieldDefinition);
    }

    public void addDataFetcher() {

        codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(getField()),
                (DataFetcher<Object>) environment ->
                        bookmarkService.bookmarkFor(environment.getSource())
                                .map(bookmark -> new GqlvAction.Fetcher(bookmark, bookmarkService))
                                .orElseThrow());

        invoke.addDataFetcher();
    }

    static class Fetcher {

        private final Bookmark bookmark;
        private final BookmarkService bookmarkService;

        public Fetcher(
                final Bookmark bookmark,
                final BookmarkService bookmarkService) {

            this.bookmark = bookmark;
            this.bookmarkService = bookmarkService;
        }

        public Object getTargetPojo() {
            return bookmarkService.lookup(bookmark).orElseThrow();
        }
    }


    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }

}
