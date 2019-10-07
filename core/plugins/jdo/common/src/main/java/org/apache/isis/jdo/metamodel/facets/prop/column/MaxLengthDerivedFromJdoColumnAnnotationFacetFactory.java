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
package org.apache.isis.jdo.metamodel.facets.prop.column;

import java.util.stream.Stream;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

import org.apache.isis.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.jdo.metamodel.facets.prop.notpersistent.JdoNotPersistentFacet;
import org.apache.isis.metamodel.JdoMetamodelUtil;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.metamodel.facets.properties.property.maxlength.MaxLengthFacetForMaxLengthAnnotationOnProperty;
import org.apache.isis.metamodel.facets.properties.property.maxlength.MaxLengthFacetForPropertyAnnotation;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.metamodel.specloader.validator.ValidationFailures;

import lombok.val;


public class MaxLengthDerivedFromJdoColumnAnnotationFacetFactory extends FacetFactoryAbstract 
implements MetaModelRefiner {

    public MaxLengthDerivedFromJdoColumnAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processMethodContext.getCls();
        if(!JdoMetamodelUtil.isPersistenceEnhanced(cls)) {
            return;
        }

       
        if(String.class != processMethodContext.getMethod().getReturnType()) {
            return;
        }
        val jdoColumnAnnotation = processMethodContext.synthesizeOnMethod(Column.class)
                .orElse(null);

        if (jdoColumnAnnotation==null) {
            return;
        }
        if(jdoColumnAnnotation.length() == -1) {
            return;
        }

        final FacetedMethod holder = processMethodContext.getFacetHolder();

        MaxLengthFacet existingFacet = holder.getFacet(MaxLengthFacet.class);

        final MaxLengthFacet facet = new MaxLengthFacetDerivedFromJdoColumn(jdoColumnAnnotation.length(), holder);

        if(!existingFacet.isNoop()) {
            // will raise violation later
            facet.setUnderlyingFacet(existingFacet);
        }

        FacetUtil.addFacet(facet);
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.addValidator(newValidatorVisitor());
    }

    private Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                validate(objectSpec, validationFailures);
                return true;
            }

            private void validate(ObjectSpecification objectSpec, ValidationFailures validationFailures) {

                final JdoPersistenceCapableFacet pcFacet = objectSpec.getFacet(JdoPersistenceCapableFacet.class);
                if(pcFacet==null || pcFacet.getIdentityType() == IdentityType.NONDURABLE) {
                    return;
                }

                final Stream<ObjectAssociation> associations = objectSpec
                        .streamAssociations(Contributed.EXCLUDED)
                        .filter(ObjectAssociation.Predicates.PROPERTIES);

                associations.forEach(association->{
                    // skip checks if annotated with JDO @NotPersistent
                    if(association.containsDoOpFacet(JdoNotPersistentFacet.class)) {
                        return;
                    }

                    final MaxLengthFacet facet = association.getFacet(MaxLengthFacet.class);
                    final MaxLengthFacet underlying = (MaxLengthFacet) facet.getUnderlyingFacet();
                    if(underlying == null) {
                        return;
                    }

                    if(facet instanceof MaxLengthFacetDerivedFromJdoColumn && underlying instanceof MaxLengthFacetForMaxLengthAnnotationOnProperty) {
                        if(facet.value() != underlying.value()) {
                            validationFailures.add(
                                    association.getIdentifier(),
                                    "%s: inconsistent lengths specified in Isis' @MaxLength(...) and @javax.jdo.annotations.Column(length=...); use just @javax.jdo.annotations.Column(length=...)",
                                    association.getIdentifier().toClassAndNameIdentityString());
                        }
                    }
                    if(facet instanceof MaxLengthFacetDerivedFromJdoColumn && underlying instanceof MaxLengthFacetForPropertyAnnotation) {
                        if(facet.value() != underlying.value()) {
                            validationFailures.add(
                                    association.getIdentifier(),
                                    "%s: inconsistent lengths specified in Isis' @Property(maxLength=...) and @javax.jdo.annotations.Column(length=...); use just @javax.jdo.annotations.Column(length=...)",
                                    association.getIdentifier().toClassAndNameIdentityString());
                        }
                    }
                });


            }
        };
    }



}
