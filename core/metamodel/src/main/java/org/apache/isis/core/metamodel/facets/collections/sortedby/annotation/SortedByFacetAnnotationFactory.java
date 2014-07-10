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

package org.apache.isis.core.metamodel.facets.collections.sortedby.annotation;

import java.util.Comparator;
import java.util.List;

import org.apache.isis.applib.annotation.SortedBy;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

/**
 * There is no check that the value is a {@link Comparator}; instead this is done through 
 * the {@link #refineMetaModelValidator(MetaModelValidatorComposite, IsisConfiguration)}.
 */
public class SortedByFacetAnnotationFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    public SortedByFacetAnnotationFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final SortedBy renderAnnotation = Annotations.getAnnotation(processMethodContext.getMethod(), SortedBy.class);
        FacetUtil.addFacet(create(renderAnnotation, processMethodContext.getFacetHolder()));
    }

    private SortedByFacet create(final SortedBy annotation, final FacetHolder holder) {
        if (annotation == null) {
            return null;
        } 
        final Class<?> annotationValue = annotation.value();
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Class<? extends Comparator<?>> comparatorType = (Class)annotationValue;
        return new SortedByFacetAnnotation(holder, comparatorType);
    }

    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        metaModelValidator.add(new MetaModelValidatorVisiting(newValidatorVisitor()));
    }

    protected Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                List<OneToManyAssociation> objectCollections = objectSpec.getCollections(Contributed.EXCLUDED);
                for (OneToManyAssociation objectCollection : objectCollections) {
                    final SortedByFacet facet = objectCollection.getFacet(SortedByFacet.class);
                    if(facet != null) {
                        final Class<? extends Comparator<?>> cls = facet.value();
                        if(!Comparator.class.isAssignableFrom(cls)) {
                            validationFailures.add("%s#%s is annotated with @SortedBy, but the class specified '%s' is not a Comparator", objectSpec.getIdentifier().getClassName(), objectCollection.getId(), facet.value().getName());
                        }
                    }
                }
                return true;
            }
        };
    }


}
