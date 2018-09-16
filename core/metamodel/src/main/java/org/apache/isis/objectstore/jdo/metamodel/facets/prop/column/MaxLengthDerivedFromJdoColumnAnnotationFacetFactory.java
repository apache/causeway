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
package org.apache.isis.objectstore.jdo.metamodel.facets.prop.column;

import java.util.List;
import java.util.stream.Stream;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.JdoMetamodelUtil;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.properties.property.maxlength.MaxLengthFacetForMaxLengthAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.maxlength.MaxLengthFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.notpersistent.JdoNotPersistentFacet;


public class MaxLengthDerivedFromJdoColumnAnnotationFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

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

        final List<Column> annotations = Annotations.getAnnotations(processMethodContext.getMethod(), Column.class);

        if(String.class != processMethodContext.getMethod().getReturnType()) {
            return;
        }

        if (annotations.isEmpty()) {
            return;
        }
        final Column annotation = annotations.get(0);
        if(annotation.length() == -1) {
            return;
        }

        final FacetedMethod holder = processMethodContext.getFacetHolder();

        MaxLengthFacet existingFacet = holder.getFacet(MaxLengthFacet.class);

        final MaxLengthFacet facet = new MaxLengthFacetDerivedFromJdoColumn(annotation.length(), holder);

        if(!existingFacet.isNoop()) {
            // will raise violation later
            facet.setUnderlyingFacet(existingFacet);
        }

        FacetUtil.addFacet(facet);
    }

    /* (non-Javadoc)
     * @see org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner#refineMetaModelValidator(org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite, org.apache.isis.core.commons.config.IsisConfiguration)
     */
    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        metaModelValidator.add(new MetaModelValidatorVisiting(newValidatorVisitor()));
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
                                    "%s: inconsistent lengths specified in Isis' @MaxLength(...) and @javax.jdo.annotations.Column(length=...); use just @javax.jdo.annotations.Column(length=...)",
                                    association.getIdentifier().toClassAndNameIdentityString());
                        }
                    }
                    if(facet instanceof MaxLengthFacetDerivedFromJdoColumn && underlying instanceof MaxLengthFacetForPropertyAnnotation) {
                        if(facet.value() != underlying.value()) {
                            validationFailures.add(
                                    "%s: inconsistent lengths specified in Isis' @Property(maxLength=...) and @javax.jdo.annotations.Column(length=...); use just @javax.jdo.annotations.Column(length=...)",
                                    association.getIdentifier().toClassAndNameIdentityString());
                        }
                    }
                });
                

            }
        };
    }



}
