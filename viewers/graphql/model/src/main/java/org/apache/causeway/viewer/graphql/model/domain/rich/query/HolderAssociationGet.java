package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectAssociationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectMemberProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;

import org.springframework.beans.factory.ObjectProvider;

public interface HolderAssociationGet<T extends ObjectAssociation>
        extends SchemaTypeProvider,
                ObjectSpecificationProvider,
                ObjectMemberProvider<T> {

}
