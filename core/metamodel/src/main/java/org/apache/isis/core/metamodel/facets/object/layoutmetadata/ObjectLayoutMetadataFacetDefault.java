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
package org.apache.isis.core.metamodel.facets.object.layoutmetadata;

import org.apache.isis.applib.layout.v1_0.ObjectLayoutMetadata;
import org.apache.isis.applib.services.layout.ObjectLayoutMetadataService;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class ObjectLayoutMetadataFacetDefault
            extends FacetAbstract
            implements ObjectLayoutMetadataFacet {

    private final ObjectLayoutMetadata metadata;
    private final ObjectLayoutMetadataService objectLayoutMetadataService;

    public static Class<? extends Facet> type() {
        return ObjectLayoutMetadataFacet.class;
    }


    public static ObjectLayoutMetadataFacet create(
            final FacetHolder facetHolder,
            final ObjectLayoutMetadata objectLayoutMetadata,
            final ObjectLayoutMetadataService objectLayoutMetadataService) {
        if(objectLayoutMetadata == null) {
            return null;
        }
        return new ObjectLayoutMetadataFacetDefault(facetHolder, objectLayoutMetadata, objectLayoutMetadataService);
    }

    private ObjectLayoutMetadataFacetDefault(
            final FacetHolder facetHolder,
            final ObjectLayoutMetadata metadata,
            final ObjectLayoutMetadataService objectLayoutMetadataService) {
        super(ObjectLayoutMetadataFacetDefault.type(), facetHolder, Derivation.NOT_DERIVED);
        this.metadata = metadata;
        this.objectLayoutMetadataService = objectLayoutMetadataService;
    }


    public ObjectLayoutMetadata getMetadata() {
        final ObjectSpecification objectSpecification = (ObjectSpecification) getFacetHolder();
        return objectLayoutMetadataService.normalize(metadata, objectSpecification.getCorrespondingClass());
    }

}
