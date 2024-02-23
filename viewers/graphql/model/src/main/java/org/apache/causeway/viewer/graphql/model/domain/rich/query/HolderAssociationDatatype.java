package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectMemberProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;

public interface HolderAssociationDatatype<T extends ObjectAssociation>
        extends ObjectMemberProvider<T>,
        SchemaTypeProvider {
}
