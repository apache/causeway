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


package org.apache.isis.metamodel.facets.hide;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.FacetUtil;
import org.apache.isis.metamodel.facets.MethodRemover;
import org.apache.isis.metamodel.java5.AnnotationBasedFacetFactoryAbstract;


public class HiddenAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public HiddenAnnotationFacetFactory() {
        super(ObjectFeatureType.EVERYTHING_BUT_PARAMETERS);
    }

    
    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        final Hidden annotation = getAnnotation(cls, Hidden.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        final Hidden annotation = getAnnotation(method, Hidden.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }

    private HiddenFacet create(final Hidden annotation, final FacetHolder holder) {
        return annotation == null ? null : new HiddenFacetAnnotation(decodeWhen(annotation.value()), holder);
    }

}
