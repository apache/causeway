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
package org.apache.causeway.viewer.graphql.model.domain.simple;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.ElementCustom;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.query.CommonDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.common.query.CommonMeta;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.SimpleAction;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.SimpleCollection;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.SimpleProperty;

public class SchemaStrategySimple implements SchemaStrategy {

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.SIMPLE;
    }

    @Override
    public String topLevelFieldNameFrom(CausewayConfiguration.Viewer.Graphql graphqlConfiguration) {
        return graphqlConfiguration.getSchema().getSimple().getTopLevelFieldName();
    }

    public Element newProperty(
            final ObjectInteractor holder,
            final OneToOneAssociation otoa,
            final Context context
    ) {
        return new SimpleProperty(holder, otoa, context);
    };
    public Element newCollection(
            final ObjectInteractor holder,
            final OneToManyAssociation otma,
            final Context context
    ) {
        return new SimpleCollection(holder, otma, context);
    }
    public Element newAction(
            final ObjectInteractor holder,
            final ObjectAction objectAction,
            final Context context
    ) {
        return new SimpleAction(holder, objectAction, context);
    }

    @Override
    public ElementCustom newMeta(CommonDomainObject commonDomainObject, Context context) {
        return new CommonMeta(commonDomainObject, context);
    }

    @Override
    public boolean shouldInclude(
            final CausewayConfiguration.Viewer.Graphql.ApiVariant apiVariant,
            final ObjectAction objectAction) {
        switch (apiVariant) {
            case QUERY_ONLY:
            case QUERY_AND_MUTATIONS:
                return objectAction.getSemantics().isSafeInNature();
            case QUERY_WITH_MUTATIONS_NON_SPEC_COMPLIANT:
                return true;
            default:
                // shouldn't happen
                throw new IllegalArgumentException("Unknown API variant: " + apiVariant);
        }
    }

}
