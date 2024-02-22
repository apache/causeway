package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.OneToOneAssociationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;

public interface HolderPropertyAutoComplete
        extends ObjectSpecificationProvider,
        OneToOneAssociationProvider,
        SchemaTypeProvider {

}
