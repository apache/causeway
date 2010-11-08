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


package org.apache.isis.metamodel.facets.object.immutable;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.java5.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;


public class ImmutableAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public ImmutableAnnotationFacetFactory() {
        super(ObjectFeatureType.OBJECTS_PROPERTIES_AND_COLLECTIONS);
    }

    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        final Immutable annotation = getAnnotation(cls, Immutable.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
    	ObjectSpecification spec = getSpecificationLoader().loadSpecification(method.getDeclaringClass());
        final ImmutableFacet immutableFacet = spec.getFacet(ImmutableFacet.class);
        if (immutableFacet != null && !immutableFacet.isNoop()) {
            return FacetUtil.addFacet(new DisabledFacetDerivedFromImmutable(immutableFacet, holder));
        }
        return false;
    }

    private ImmutableFacet create(final Immutable annotation, final FacetHolder holder) {
        return annotation == null ? null : new ImmutableFacetAnnotation(decodeWhen(annotation.value()), holder);
    }


}
