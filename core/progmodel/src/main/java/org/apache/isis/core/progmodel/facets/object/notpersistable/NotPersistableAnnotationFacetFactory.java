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


package org.apache.isis.core.progmodel.facets.object.notpersistable;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.NotPersistable;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.java5.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;


public class NotPersistableAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public NotPersistableAnnotationFacetFactory() {
        super(ObjectFeatureType.OBJECTS_ONLY);
    }

    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        final NotPersistable annotation = getAnnotation(cls, NotPersistable.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        final NotPersistable annotation = getAnnotation(method, NotPersistable.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }

    private NotPersistableFacet create(final NotPersistable annotation, final FacetHolder holder) {
        return annotation != null ? new NotPersistableFacetAnnotation(decodeBy(annotation.value()), holder) : null;
    }

    private InitiatedBy decodeBy(final NotPersistable.By by) {
        if (by == NotPersistable.By.USER) {
            return InitiatedBy.USER;
        }
        if (by == NotPersistable.By.USER_OR_PROGRAM) {
            return InitiatedBy.USER_OR_PROGRAM;
        }
        return null;
    }

}
