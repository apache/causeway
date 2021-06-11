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
package org.apache.isis.persistence.jdo.metamodel.facets.prop.primarykey;

import javax.inject.Inject;
import javax.jdo.annotations.PrimaryKey;

import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.Setter;
import lombok.val;

public class JdoPrimaryKeyAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject @Setter private JdoFacetContext jdoFacetContext;

    public JdoPrimaryKeyAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {

        // ignore any view models
        val cls = processMethodContext.getCls();
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return;
        }

        //      val method = processMethodContext.getMethod();
        //       _Assert.assertEquals("expected same on method=" + method , annotation,
        //                Annotations.getAnnotation(method, PrimaryKey.class));

        val primaryKeyIfAny = processMethodContext.synthesizeOnMethod(PrimaryKey.class);
        if (!primaryKeyIfAny.isPresent()) {
            return;
        }

        val facetHolder = processMethodContext.getFacetHolder();
        addFacetIfPresent(new JdoPrimaryKeyFacetAnnotation(facetHolder));
        addFacetIfPresent(new OptionalFacetInferredFromJdoPrimaryKeyAnnotation(facetHolder));
        addFacetIfPresent(new DisabledFacetDerivedFromJdoPrimaryKeyAnnotation(facetHolder));
    }
}
