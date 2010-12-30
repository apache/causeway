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


package org.apache.isis.core.progmodel.facets.actions.notinservicemenu;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;


public class NotInServiceMenuAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public NotInServiceMenuAnnotationFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        final NotInServiceMenu annotation = getAnnotation(method, NotInServiceMenu.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }

    private NotInServiceMenuFacet create(final NotInServiceMenu annotation, final FacetHolder holder) {
        return annotation == null ? null : new NotInServiceMenuFacetAnnotation(holder);
    }

}
