package org.apache.causeway.viewer.graphql.model.domain.common;

import java.util.Map;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvDomainService;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvMemberHolder;
import org.apache.causeway.viewer.graphql.model.domain.rich.SchemaStrategyRich;
import org.apache.causeway.viewer.graphql.model.domain.simple.SchemaStrategySimple;

public interface SchemaStrategy {

    SchemaStrategy RICH = new SchemaStrategyRich();
    SchemaStrategy SIMPLE  = new SchemaStrategySimple();

    SchemaType getSchemaType();

    Map<ObjectSpecification, GqlvDomainObject> domainObjectBySpec(Context context);
    Map<ObjectSpecification, GqlvDomainService> domainServiceBySpec(Context context);

    String topLevelFieldNameFrom(CausewayConfiguration.Viewer.Graphql graphqlConfiguration);

    GqlvAbstractCustom newGqlvDomainObject(
            final ObjectSpecification objectSpecification,
            final Context context
    );
    GqlvAbstractCustom newGqlvProperty(
            final GqlvMemberHolder holder,
            final OneToOneAssociation otoa,
            final Context context
    );
    GqlvAbstractCustom newGqlvCollection(
            final GqlvMemberHolder holder,
            final OneToManyAssociation otma,
            final Context context
    );
    GqlvAbstractCustom newGqlvAction(
            final GqlvMemberHolder holder,
            final ObjectAction objectAction,
            final Context context
    );

    GqlvAbstractCustom newGqlvMeta(
            final GqlvDomainObject gqlvDomainObject,
            final Context context);
}
