package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;

public interface HolderActionInvokeArgsArg
        extends ObjectSpecificationProvider,
        ObjectActionProvider,
        SchemaTypeProvider {

    Can<ManagedObject> argumentManagedObjectsFor(
            Environment dataFetchingEnvironment,
            ObjectAction objectAction,
            BookmarkService bookmarkService);
}
