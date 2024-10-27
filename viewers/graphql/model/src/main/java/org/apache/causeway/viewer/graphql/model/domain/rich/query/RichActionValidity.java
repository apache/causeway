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
package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import java.util.Map;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Element;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ActionInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RichActionValidity extends Element {

    private final ActionInteractor actionInteractor;

    public RichActionValidity(
            final ActionInteractor actionInteractor,
            final Context context
    ) {
        super(context);
        this.actionInteractor = actionInteractor;

        var objectAction = actionInteractor.getObjectMember();

        var fieldBuilder = newFieldDefinition()
                .name("validate")
                .type((GraphQLOutputType) this.context.typeMapper.outputTypeFor(String.class));

        actionInteractor.addGqlArguments(objectAction, fieldBuilder, TypeMapper.InputContext.VALIDATE, objectAction.getParameterCount());
        setField(fieldBuilder.build());
    }

    @Override
    protected Object fetchData(final DataFetchingEnvironment dataFetchingEnvironment) {

        final ObjectAction objectAction = actionInteractor.getObjectMember();

        var sourcePojo = BookmarkedPojo.sourceFrom(dataFetchingEnvironment);

        var sourcePojoClass = sourcePojo.getClass();
        var specificationLoader = objectAction.getSpecificationLoader();
        var objectSpecification = specificationLoader.loadSpecification(sourcePojoClass);
        if (objectSpecification == null) {
            // not expected
            return null;
        }

        var managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        var actionInteractionHead = objectAction.interactionHead(managedObject);

        Map<String, Object> argumentPojos = dataFetchingEnvironment.getArguments();
        Can<ObjectActionParameter> parameters = objectAction.getParameters();
        Can<ManagedObject> argumentManagedObjects = parameters
                .map(oap -> {
                    Object argumentValue = argumentPojos.get(oap.asciiId());
                    return ManagedObject.adaptParameter(oap, argumentValue);
                });

        Consent consent = objectAction.isArgumentSetValid(actionInteractionHead, argumentManagedObjects, InteractionInitiatedBy.USER);

        return consent.isVetoed() ? consent.getReasonAsString().orElse("Invalid") : null;
    }

}
