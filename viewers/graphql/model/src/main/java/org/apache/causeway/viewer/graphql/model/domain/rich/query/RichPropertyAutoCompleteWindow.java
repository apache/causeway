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

import java.util.List;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

final class RichPropertyAutoCompleteWindow extends RichAutoCompleteWindow {

    private final MemberInteractor<OneToOneAssociation> memberInteractor;

    RichPropertyAutoCompleteWindow(
            final MemberInteractor<OneToOneAssociation> memberInteractor,
            final Context context) {
        super(
                context,
                TypeNames.propertyAutocompleteWindowTypeNameFor(
                        memberInteractor.getObjectSpecification(),
                        memberInteractor.getObjectMember(),
                        memberInteractor.getSchemaType()),
                context.typeMapper.outputTypeFor(
                        memberInteractor.getObjectMember().getElementType(), SchemaType.RICH),
                builder -> { });
        this.memberInteractor = memberInteractor;
    }

    @Override
    protected List<Object> autocompleteItems(final DataFetchingEnvironment environment) {
        var sourcePojo = BookmarkedPojo.sourceFrom(environment);
        var objectSpecification = context.specificationLoader.loadSpecification(sourcePojo.getClass());
        if (objectSpecification == null) {
            return List.of();
        }

        var association = memberInteractor.getObjectMember();
        var managedObject = ManagedObject.adaptSingular(objectSpecification, sourcePojo);
        var search = environment.<String>getArgument(SEARCH_ARGUMENT);
        return association.getAutoComplete(managedObject, search, InteractionInitiatedBy.USER).stream()
                .map(ManagedObject::getPojo)
                .toList();
    }
}
