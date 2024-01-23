package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.schema.*;

import lombok.extern.log4j.Log4j2;

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

        gqlObjectTypeBuilder = newObject().name(TypeNames.actionTypeNameFor(objectAction, holder.getObjectSpecification()));

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
                new Fetcher2());

        invoke.addDataFetcher();
    }

    private class Fetcher2 implements DataFetcher<Object> {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            Object source = environment.getSource();
            Object domainPojo;
            if (source instanceof GqlvMutations.Fetcher) {
                GqlvMutations.Fetcher mutationsFetcher = (GqlvMutations.Fetcher) source;
                domainPojo = mutationsFetcher.getTargetPojo();
            } else {
                // presumably this is a safe action
                domainPojo = source;
            }
            return bookmarkService.bookmarkFor(domainPojo)
                    .map(bookmark -> new Fetcher(bookmark, bookmarkService))
                    .orElseThrow();
        }
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
