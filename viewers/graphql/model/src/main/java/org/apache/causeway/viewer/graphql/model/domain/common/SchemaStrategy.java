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
package org.apache.causeway.viewer.graphql.model.domain.common;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.ElementCustom;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.query.CommonDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.common.query.CommonDomainService;
import org.apache.causeway.viewer.graphql.model.domain.rich.SchemaStrategyRich;
import org.apache.causeway.viewer.graphql.model.domain.simple.SchemaStrategySimple;

import static org.apache.causeway.viewer.graphql.model.domain.common.query.CommonTopLevelQueryAbstract.superclassesOf;

public interface SchemaStrategy {

    SchemaStrategy RICH = new SchemaStrategyRich();
    SchemaStrategy SIMPLE = new SchemaStrategySimple();

    SchemaType getSchemaType();

    default CommonDomainObject domainObjectFor(
            final ObjectSpecification objectSpecification,
            final Context context) {

        mapSuperclassesIfNecessary(this, objectSpecification, context);
        var typeNameFor = CommonDomainObject.typeNameFor(this, objectSpecification);
        return context.domainObjectByTypeName.computeIfAbsent(typeNameFor, typeName -> new CommonDomainObject(this, typeName, objectSpecification, context));
    }

    default CommonDomainService domainServiceFor(
            final ObjectSpecification objectSpecification,
            final Object servicePojo,
            final Context context) {
        var typeNameFor = CommonDomainService.typeNameFor(this, objectSpecification);
        return context.domainServiceByTypeName.computeIfAbsent(typeNameFor, typeName -> new CommonDomainService(this, typeName, objectSpecification, servicePojo, context));
    }

    String topLevelFieldNameFrom(CausewayConfiguration.Viewer.Graphql graphqlConfiguration);

    Element newProperty(
            final ObjectInteractor holder,
            final OneToOneAssociation otoa,
            final Context context
    );

    Element newCollection(
            final ObjectInteractor holder,
            final OneToManyAssociation otma,
            final Context context
    );

    Element newAction(
            final ObjectInteractor holder,
            final ObjectAction objectAction,
            final Context context
    );

    ElementCustom newMeta(
            final CommonDomainObject commonDomainObject,
            final Context context);

    private static void mapSuperclassesIfNecessary(
            final SchemaStrategy schemaStrategy,
            final ObjectSpecification objectSpecification,
            final Context context) {
        // no need to map if the target subclass has already been built
        var typeName = CommonDomainObject.typeNameFor(schemaStrategy, objectSpecification);
        if (context.domainObjectByTypeName.containsKey(typeName)) {
            return;
        }
        var superclasses = superclassesOf(objectSpecification);
        superclasses.forEach(objectSpec -> {
            var typeNameForSuperclass = CommonDomainObject.typeNameFor(schemaStrategy, objectSpecification);
            context.domainObjectByTypeName.computeIfAbsent(typeNameForSuperclass, typeNm -> new CommonDomainObject(schemaStrategy, typeNm, objectSpecification, context));
        });
    }

    boolean shouldInclude(
            final CausewayConfiguration.Viewer.Graphql.ApiVariant apiVariant,
            final ObjectAction objectAction);
}
