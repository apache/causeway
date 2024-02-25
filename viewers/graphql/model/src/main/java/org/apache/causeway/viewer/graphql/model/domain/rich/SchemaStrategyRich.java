/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.graphql.model.domain.rich;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvMeta;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvAction;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvCollection;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvProperty;

public class SchemaStrategyRich implements SchemaStrategy {

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.RICH;
    }

    @Override
    public String topLevelFieldNameFrom(CausewayConfiguration.Viewer.Graphql graphqlConfiguration) {
        return graphqlConfiguration.getTopLevelFieldNameForRich();
    }

    public GqlvAbstractCustom newGqlvProperty(
            final ObjectInteractor holder,
            final OneToOneAssociation otoa,
            final Context context
    ) {
        return new GqlvProperty(holder, otoa, context);
    };
    public GqlvAbstractCustom newGqlvCollection(
            final ObjectInteractor holder,
            final OneToManyAssociation otma,
            final Context context
    ) {
        return new GqlvCollection(holder, otma, context);
    }
    public GqlvAbstractCustom newGqlvAction(
            final ObjectInteractor holder,
            final ObjectAction objectAction,
            final Context context
    ) {
        return new GqlvAction(holder, objectAction, context);
    }

    @Override
    public GqlvAbstractCustom newGqlvMeta(GqlvDomainObject gqlvDomainObject, Context context) {
        return new GqlvMeta(gqlvDomainObject, context);
    }

    /**
     * Irrespective of the {@link org.apache.causeway.core.config.CausewayConfiguration.Viewer.Graphql.ApiVariant API variant},
     * the rich schema will always include all actions (to show additional facets such as whether the action is hidden
     * or disabled).
     *
     * <p>
     *  What <i>might be</i> suppressed under the rich schema is whether the action can be invoked or not.
     * </p>
     */
    @Override
    public boolean shouldInclude(
            final CausewayConfiguration.Viewer.Graphql.ApiVariant apiVariant,
            final ObjectAction objectAction) {
        return true;
    }

}
