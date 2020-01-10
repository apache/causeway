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
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.commons.ClassUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

import static org.apache.isis.core.commons.internal.base._Casts.uncheckedCast;

public class RemoveAnnotatedMethodsFacetFactory extends FacetFactoryAbstract {

    private final List<String> eventHandlerClassNames = _Lists.of(
            "org.axonframework.eventhandling.EventHandler", // axon 3.x
            "org.axonframework.eventhandling.annotation.EventHandler", // axon 2.x
            "com.google.common.eventbus.Subscribe" // guava
            );

    private final List<Class<? extends Annotation>> eventHandlerClasses;

    public RemoveAnnotatedMethodsFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);

        eventHandlerClasses = eventHandlerClassNames.stream()
                .map(name->{
                    Class<? extends Annotation> eventHandlerAnnotationClass;
                    // doing this reflectively so that don't bring in a dependency on axon.
                    eventHandlerAnnotationClass = uncheckedCast(ClassUtil.forNameElseNull(name));
                    return eventHandlerAnnotationClass;
                })
                .filter(_NullSafe::isPresent)
                .collect(Collectors.toList());

    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        removeIgnoredMethods(processClassContext.getCls(), processClassContext);
    }

    private void removeIgnoredMethods(final Class<?> cls, final MethodRemover methodRemover) {
        if (cls == null) {
            return;
        }

        final Method[] methods = cls.getMethods();
        for (final Method method : methods) {
            removeAnnotatedMethods(methodRemover, method, PreDestroy.class);
            removeAnnotatedMethods(methodRemover, method, PostConstruct.class);
            removeAnnotatedMethods(methodRemover, method, Programmatic.class);
            eventHandlerClasses.forEach(eventHandlerClass->{
                removeAnnotatedMethods(methodRemover, method, eventHandlerClass);
            });
        }
    }

    private static <T extends Annotation> void removeAnnotatedMethods(
            final MethodRemover methodRemover, 
            final Method method, 
            final Class<T> annotationClass) {

        if (!Annotations.isAnnotationPresent(method, annotationClass)) {
            return;
        }
        methodRemover.removeMethod(method);
    }


}
