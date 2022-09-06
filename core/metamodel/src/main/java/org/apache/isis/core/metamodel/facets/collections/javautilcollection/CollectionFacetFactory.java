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
package org.apache.isis.core.metamodel.facets.collections.javautilcollection;

import javax.inject.Inject;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

import lombok.val;

public class CollectionFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public CollectionFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        ProgrammingModelConstants.CollectionType.valueOf(cls)
        .ifPresent(collectionType->{
            if (collectionType.isArray()) {
                addFacet(new JavaArrayFacet(facetHolder));
                return;
            }

            addFacet(new JavaCollectionFacet(facetHolder));
            if(cls.isInterface()) {
                return;
            }

            throw _Exceptions.unrecoverable("non-scalar object %s", cls);
            // addFacetIfPresent(TypeOfFacet.inferFromObjectType(cls, facetHolder));
        });
    }

}
