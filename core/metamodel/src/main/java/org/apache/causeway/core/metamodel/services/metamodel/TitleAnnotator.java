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
package org.apache.causeway.core.metamodel.services.metamodel;

import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.CollectionSemantics;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyFeature;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.schema.metamodel.v2.Action;
import org.apache.causeway.schema.metamodel.v2.Collection;
import org.apache.causeway.schema.metamodel.v2.DomainClassDto;
import org.apache.causeway.schema.metamodel.v2.Facet;
import org.apache.causeway.schema.metamodel.v2.MetamodelElement;
import org.apache.causeway.schema.metamodel.v2.Param;
import org.apache.causeway.schema.metamodel.v2.Property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public class TitleAnnotator implements MetaModelAnnotator {

    @Getter(onMethod_={@Override}) @Accessors(fluent=true)
    private final ExporterConfig config;

    @Override
    public void annotate(
            final Facet facetType,
            final org.apache.causeway.core.metamodel.facetapi.Facet facet) {
        titleAnnotation(facetType,
                String.format("%s: %s",
                        config().simpleName(facet.facetType()),
                        config().abbrev(facet.getClass())));
    }

    @Override
    public void annotate(
            final DomainClassDto domainClass, final ObjectSpecification specification) {
        titleAnnotation(domainClass,
                String.format("%s: %s",
                        specification.getLogicalTypeName(),
                        config().abbrev(specification.getCorrespondingClass())));
    }

    @Override
    public void annotate(
            final Action actionType, final ObjectAction action) {
        titleAnnotation(actionType,
            String.format("%s(...): %s%s",
                    action.getId(),
                    config().abbrev(action.getReturnType().getCorrespondingClass()),
                    titleSuffix(action.isMixedIn())));
    }

    @Override
    public void annotate(
            final Param parameterType, final ObjectActionParameter parameter) {
        titleAnnotation(parameterType,
                String.format("%s: %s",
                        parameter.getId(),
                        parameter.isSingular()
                        ? config().abbrev(parameter.getElementType().getCorrespondingClass())
                        : renderTypeOf((OneToManyFeature) parameter, config()))
                );
    }

    @Override
    public void annotate(
            final Property propertyType, final OneToOneAssociation property) {
        titleAnnotation(propertyType,
                String.format("%s: %s%s",
                        property.getId(),
                        config().abbrev(property.getElementType().getCorrespondingClass()),
                        titleSuffix(property.isMixedIn())));
    }

    @Override
    public void annotate(
            final Collection collectionType, final OneToManyAssociation collection) {
        titleAnnotation(collectionType,
                String.format("%s: %s%s",
                        collection.getId(),
                        renderTypeOf(collection, config()),
                        titleSuffix(collection.isMixedIn())));
    }

    // -- HELPER

    private String renderTypeOf(final OneToManyFeature nonScalarFeature, final ExporterConfig exporterConfig) {
        val toac = nonScalarFeature.getTypeOfAnyCardinality();
        val containerType = toac.getCollectionSemantics()
                .map(CollectionSemantics::getContainerType)
                .map(exporterConfig::simpleName).orElse("?");
        val elementType = exporterConfig.abbrev(toac.getElementType());
        return String.format("%s<%s>", containerType, elementType);
    }

    private <T extends MetamodelElement> T titleAnnotation(final T t, final String title) {
        return createAnnotation(t, "@title", title);
    }

    private String titleSuffix(final boolean isMixedIn) {
        return isMixedIn ? " (mixed in)" : "";
    }

}
