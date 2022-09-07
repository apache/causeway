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
package org.apache.isis.core.metamodel.services.metamodel;

import org.apache.isis.applib.services.metamodel.Config;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.CollectionSemantics;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyFeature;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.metamodel.v2.Action;
import org.apache.isis.schema.metamodel.v2.Collection;
import org.apache.isis.schema.metamodel.v2.DomainClassDto;
import org.apache.isis.schema.metamodel.v2.Facet;
import org.apache.isis.schema.metamodel.v2.MetamodelElement;
import org.apache.isis.schema.metamodel.v2.MetamodelElement.Annotations;
import org.apache.isis.schema.metamodel.v2.Param;
import org.apache.isis.schema.metamodel.v2.Property;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _Util {

    String withSuffix(String fileName, String suffix) {
        if(!suffix.startsWith(".")) {
            suffix = "." + suffix;
        }
        if(!fileName.endsWith(suffix)) {
            fileName += suffix;
        }
        return fileName;
    }

    static void titleAnnotation(
            final Facet facetType,
            final org.apache.isis.core.metamodel.facetapi.Facet facet,
            final Config config) {
        if(!config.isIncludeTitleAnnotations()) return;
        titleAnnotation(facetType,
                String.format("%s: %s",
                        config.simpleName(facet.facetType()),
                        config.abbrev(facet.getClass())));
    }

    static void titleAnnotation(
            final DomainClassDto domainClass, final ObjectSpecification specification, final Config config) {
        if(!config.isIncludeTitleAnnotations()) return;
        titleAnnotation(domainClass,
                String.format("%s: %s",
                        specification.getLogicalTypeName(),
                        config.abbrev(specification.getCorrespondingClass())));
    }

    static void titleAnnotation(
            final Action actionType, final ObjectAction action, final Config config) {
        if(!config.isIncludeTitleAnnotations()) return;
        titleAnnotation(actionType,
            String.format("%s(...): %s%s",
                    action.getId(),
                    config.abbrev(action.getReturnType().getCorrespondingClass()),
                    titleSuffix(action.isMixedIn())));
    }

    static void titleAnnotation(
            final Param parameterType, final ObjectActionParameter parameter, final Config config) {
        if(!config.isIncludeTitleAnnotations()) return;
        titleAnnotation(parameterType,
                String.format("%s: %s",
                        parameter.getId(),
                        parameter.isScalar()
                        ? config.abbrev(parameter.getElementType().getCorrespondingClass())
                        : renderTypeOf((OneToManyFeature) parameter, config))
                );
    }

    static void titleAnnotation(
            final Property propertyType, final OneToOneAssociation property, final Config config) {
        if(!config.isIncludeTitleAnnotations()) return;
        titleAnnotation(propertyType,
                String.format("%s: %s%s",
                        property.getId(),
                        config.abbrev(property.getElementType().getCorrespondingClass()),
                        titleSuffix(property.isMixedIn())));
    }

    static void titleAnnotation(
            final Collection collectionType, final OneToManyAssociation collection, final Config config) {
        if(!config.isIncludeTitleAnnotations()) return;
        titleAnnotation(collectionType,
                String.format("%s: %s%s",
                        collection.getId(),
                        renderTypeOf(collection, config),
                        titleSuffix(collection.isMixedIn())));
    }

    // -- HELPER

    private String renderTypeOf(final OneToManyFeature nonScalarFeature, final Config config) {
        val toac = nonScalarFeature.getTypeOfAnyCardinality();
        val containerType = toac.getCollectionSemantics()
                .map(CollectionSemantics::getContainerType)
                .map(config::simpleName).orElse("?");
        val elementType = config.abbrev(toac.getElementType());
        return String.format("%s<%s>", containerType, elementType);
    }

    private <T extends MetamodelElement> T titleAnnotation(final T t, final String title) {
        val titleAnnot = new org.apache.isis.schema.metamodel.v2.Annotation();
        titleAnnot.setName("@title");
        titleAnnot.setValue(title);
        val annots = new Annotations();
        t.setAnnotations(annots);
        annots.getAsList().add(titleAnnot);
        return t;
    }

    private String titleSuffix(final boolean isMixedIn) {
        return isMixedIn ? " (mixed in)" : "";
    }

}
