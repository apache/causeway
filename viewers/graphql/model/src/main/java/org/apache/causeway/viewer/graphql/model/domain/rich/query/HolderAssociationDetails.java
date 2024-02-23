package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectMemberProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;

public interface HolderAssociationDetails<T extends ObjectAssociation>
        extends SchemaTypeProvider,
                ObjectSpecificationProvider,
                ObjectMemberProvider<T> {

}
