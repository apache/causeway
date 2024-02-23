package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectMemberProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

public interface HolderActionDetails
        extends SchemaTypeProvider,
                ObjectSpecificationProvider,
                ObjectMemberProvider<ObjectAction> {

    void addGqlArguments(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition.Builder fieldBuilder,
            final TypeMapper.InputContext inputContext,
            final int parameterCount);

    Can<ManagedObject> argumentManagedObjectsFor(
            Environment dataFetchingEnvironment,
            ObjectAction objectAction,
            BookmarkService bookmarkService);

}
