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
package org.apache.isis.extensions.jpa.metamodel.specloader.progmodelfacets;


import org.apache.isis.extensions.jpa.metamodel.facets.collection.elements.JpaElementCollectionAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.object.discriminator.JpaDiscriminatorValueAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.object.embeddable.JpaEmbeddableAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.object.entity.JpaEntityAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.JpaNamedQueryAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.basic.JpaBasicAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.column.JpaColumnAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.id.JpaIdAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.joincolumn.JpaJoinColumnAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone.JpaManyToOneAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.onetoone.JpaOneToOneAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.transience.JpaTransientAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.version.JpaVersionAnnotationFacetFactory;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

/**
 * As per the {@link ProgrammingModelFacetsJava5 Java 5 default programming
 * model}, but also
 * includes support for JPA.
 * <p>
 * Intended to be used by the {@link JpaJavaReflectorInstaller}, which
 * additionally sets up other required components needed for JPA support.
 */
public class JpaProgrammingModelFacets extends ProgrammingModelFacetsJava5 {

    public JpaProgrammingModelFacets() {
        addFactory(JpaEntityAnnotationFacetFactory.class);
        addFactory(JpaDiscriminatorValueAnnotationFacetFactory.class);
        addFactory(JpaIdAnnotationFacetFactory.class);
        
        
        addFactory(JpaElementCollectionAnnotationFacetFactory.class);
        addFactory(JpaEmbeddableAnnotationFacetFactory.class);
        addFactory(JpaNamedQueryAnnotationFacetFactory.class);
        addFactory(JpaBasicAnnotationFacetFactory.class);
        addFactory(JpaColumnAnnotationFacetFactory.class);
        addFactory(JpaJoinColumnAnnotationFacetFactory.class);
        addFactory(JpaManyToOneAnnotationFacetFactory.class);
        addFactory(JpaOneToOneAnnotationFacetFactory.class);
        addFactory(JpaTransientAnnotationFacetFactory.class);
        addFactory(JpaVersionAnnotationFacetFactory.class);
    }

}


// Copyright (c) Naked Objects Group Ltd.
