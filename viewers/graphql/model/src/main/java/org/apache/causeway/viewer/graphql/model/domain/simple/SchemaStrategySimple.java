package org.apache.causeway.viewer.graphql.model.domain.simple;

import java.util.Map;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvDomainService;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.GqlvAction;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.GqlvCollection;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvMemberHolder;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.GqlvProperty;

public class SchemaStrategySimple implements SchemaStrategy {

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.SIMPLE;
    }

    @Override
    public Map<ObjectSpecification, GqlvDomainObject> domainObjectBySpec(Context context) {
        return context.simpleDomainObjectBySpec;
    }

    @Override
    public Map<ObjectSpecification, GqlvDomainService> domainServiceBySpec(Context context) {
        return context.simpleDomainServiceBySpec;
    }

    @Override
    public String topLevelFieldNameFrom(CausewayConfiguration.Viewer.Graphql graphqlConfiguration) {
        return graphqlConfiguration.getTopLevelFieldNameForSimple();
    }

    public GqlvAbstractCustom newGqlvDomainObject(
            final ObjectSpecification objectSpecification,
            final Context context
    ) {
        return new GqlvDomainObject(this, objectSpecification, context);
    }

    public GqlvAbstractCustom newGqlvProperty(
            final GqlvMemberHolder holder,
            final OneToOneAssociation otoa,
            final Context context
    ) {
        return new GqlvProperty(holder, otoa, context);
    };
    public GqlvAbstractCustom newGqlvCollection(
            final GqlvMemberHolder holder,
            final OneToManyAssociation otma,
            final Context context
    ) {
        return new GqlvCollection(holder, otma, context);
    }
    public GqlvAbstractCustom newGqlvAction(
            final GqlvMemberHolder holder,
            final ObjectAction objectAction,
            final Context context
    ) {
        return new GqlvAction(holder, objectAction, context);
    }
}
