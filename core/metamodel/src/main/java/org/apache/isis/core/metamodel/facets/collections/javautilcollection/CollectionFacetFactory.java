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

import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;

import lombok.val;

public class CollectionFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public CollectionFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {

        if (_Collections.isCollectionType(processClassContaxt.getCls())) {
            processCollectionType(processClassContaxt);
        } else if (_Arrays.isArrayType(processClassContaxt.getCls())) {
            processAsArrayType(processClassContaxt);
        }

    }

    // -- HELPER

    private void processCollectionType(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        facetHolder.addFacet(new JavaCollectionFacet(facetHolder));
        //TODO facetHolder.addFacet(TypeOfFacet.inferredFromGenerics(Object.class, facetHolder));
    }

    private void processAsArrayType(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        facetHolder.addFacet(new JavaArrayFacet(facetHolder));
        facetHolder.addFacet(TypeOfFacet.inferredFromArray(cls.getComponentType(), facetHolder));
    }


}
