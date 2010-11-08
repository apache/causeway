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


package org.apache.isis.metamodel.facets.actions.prototype;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.FacetUtil;
import org.apache.isis.metamodel.facets.MethodRemover;
import org.apache.isis.metamodel.java5.AnnotationBasedFacetFactoryAbstract;


public class PrototypeAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public PrototypeAnnotationFacetFactory() {
        super(ObjectFeatureType.ACTIONS_ONLY);
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        final Prototype annotation = getAnnotation(method, Prototype.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }

    private PrototypeFacet create(final Prototype annotation, final FacetHolder holder) {
        return annotation == null ? null : new PrototypeFacetAnnotation(holder);
    }

}
