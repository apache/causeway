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
package org.apache.isis.persistence.jpa.metamodel.object.table;


import javax.persistence.Table;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainobject.DomainObjectAnnotationFacetFactory;
import org.apache.isis.persistence.jpa.metamodel.object.domainobject.objectspecid.LogicalTypeFacetForTableAnnotation;

import lombok.val;

/**
 * Implements {@link ObjectTypeFacetFactory} only because is a prereq of {@link DomainObjectAnnotationFacetFactory}.
 */
public class JpaTableAnnotationFacetFactory
extends FacetFactoryAbstract
implements ObjectTypeFacetFactory {

    public JpaTableAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ObjectTypeFacetFactory.ProcessObjectTypeContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();

        final Table annotation = Annotations.getAnnotation(cls, Table.class);
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


        val jdoPersistenceCapableFacet = new JpaTableFacetAnnotationImpl(
                annotationSchemaAttribute,
                annotationTableAttribute,
                facetHolder);
        FacetUtil.addFacet(jdoPersistenceCapableFacet);
        FacetUtil.addFacet(LogicalTypeFacetForTableAnnotation.create(jdoPersistenceCapableFacet, cls, facetHolder));

        return;
    }


}
