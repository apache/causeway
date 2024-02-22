package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;

public interface HolderPropertyGet
        extends HolderAssociationGet<OneToOneAssociation>,
        SchemaTypeProvider {

    @Override
    OneToOneAssociation getObjectAssociation();
}
