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

package org.apache.isis.core.metamodel.facets.object.ignore.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.google.common.eventbus.Subscribe;

import org.apache.isis.core.commons.lang.ClassUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

public class RemoveSubscriberMethodsFacetFactory extends FacetFactoryAbstract {

    public RemoveSubscriberMethodsFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        removeIgnoredMethods(processClassContext.getCls(), processClassContext);
    }

    @SuppressWarnings("deprecation")
    private void removeIgnoredMethods(final Class<?> cls, final MethodRemover methodRemover) {
        if (cls == null) {
            return;
        }

        final Method[] methods = cls.getMethods();
        for (final Method method : methods) {
            removeAnnotatedMethods(methodRemover, method, Subscribe.class);
            try {
                // doing this reflectively so that don't bring in a dependency on axon.
                final Class annotationClass = ClassUtil.forName("org.axonframework.eventhandling.annotation.EventHandler");
                removeAnnotatedMethods(methodRemover, method, annotationClass);
            } catch(Exception ignore) {
                // iignore
            }
        }
    }

    private static <T extends Annotation> void removeAnnotatedMethods(final MethodRemover methodRemover, final Method method, Class<T> annotationClass) {
        if (!Annotations.isAnnotationPresent(method, annotationClass)) {
            return;
        } 
        methodRemover.removeMethod(method);
    }

}
