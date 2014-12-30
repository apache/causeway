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

package org.apache.isis.core.metamodel.facets.collections.collection;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class CollectionAnnotationFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware {

    private ServicesInjector servicesInjector;

    public CollectionAnnotationFacetFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final Collection collection = Annotations.getAnnotation(method, Collection.class);
        if (collection == null) {
            return;
        }

        processInteraction(processMethodContext);
        processHidden(processMethodContext);
        processEditing(processMethodContext);
        processTypeOf(processMethodContext);
    }

    private void processInteraction(final ProcessMethodContext processMethodContext) {

        // interaction is handled by CollectionInteractionFacetFactory, because the
        // deprecated annotations must also be supported.

    }

    private void processHidden(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Collection collection = Annotations.getAnnotation(method, Collection.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                HiddenFacetForCollectionAnnotation.create(collection, holder));
    }

    private void processEditing(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Collection collection = Annotations.getAnnotation(method, Collection.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                DisabledFacetForCollectionAnnotation.create(collection, holder));
    }

    private void processTypeOf(final ProcessMethodContext processMethodContext) {

        // typeOf is handled by TypeOfFacetOnActionAnnotationFactory, because the
        // deprecated annotations etc that must also be supported.
    }


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }
}
