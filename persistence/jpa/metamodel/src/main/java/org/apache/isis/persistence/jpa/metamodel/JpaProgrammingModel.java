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
package org.apache.isis.persistence.jpa.metamodel;

import org.springframework.stereotype.Component;

import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel.Marker;
import org.apache.isis.persistence.jpa.metamodel.facets.prop.column.MandatoryFromJpaColumnAnnotationFacetFactory;
import org.apache.isis.persistence.jpa.metamodel.facets.prop.transients.JpaTransientAnnotationFacetFactory;
import org.apache.isis.persistence.jpa.metamodel.object.table.JpaTableAnnotationFacetFactory;

import lombok.val;

@Component
public class JpaProgrammingModel implements MetaModelRefiner {

    //@Inject private IsisConfiguration config;

    @Override
    public void refineProgrammingModel(ProgrammingModel pm) {

        val step = ProgrammingModel.FacetProcessingOrder.A2_AFTER_FALLBACK_DEFAULTS;

        pm.addFactory(step, JpaTableAnnotationFacetFactory.class, Marker.JPA);
        pm.addFactory(step, JpaTransientAnnotationFacetFactory.class, Marker.JPA);
        pm.addFactory(step, MandatoryFromJpaColumnAnnotationFacetFactory.class, Marker.JPA);

    }

}
