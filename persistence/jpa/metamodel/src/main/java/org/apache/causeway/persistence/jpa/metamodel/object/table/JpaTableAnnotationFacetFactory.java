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
package org.apache.causeway.persistence.jpa.metamodel.object.table;


import javax.inject.Inject;
import javax.persistence.Table;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.causeway.core.metamodel.facets.object.domainobject.DomainObjectAnnotationFacetFactory;

import lombok.val;

/**
 * Implements {@link ObjectTypeFacetFactory} only because is a prereq of {@link DomainObjectAnnotationFacetFactory}.
 */
public class JpaTableAnnotationFacetFactory
extends FacetFactoryAbstract
implements ObjectTypeFacetFactory {

    @Inject
    public JpaTableAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ObjectTypeFacetFactory.ProcessObjectTypeContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();

        final Table annotation = processClassContext.synthesizeOnType(Table.class).orElse(null);
        if (annotation == null) {
            return;
        }
        String annotationSchemaAttribute = annotation.schema();
        if(_Strings.isNullOrEmpty(annotationSchemaAttribute)) {
            annotationSchemaAttribute = null;
        }
        String annotationTableAttribute = annotation.name();
        if (_Strings.isNullOrEmpty(annotationTableAttribute)) {
            annotationTableAttribute = cls.getSimpleName();
        }

        val facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacet(
                new JpaTableFacetAnnotationImpl(
                    annotationSchemaAttribute,
                    annotationTableAttribute,
                    facetHolder));
    }


}
