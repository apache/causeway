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

package org.apache.isis.core.metamodel.facets.object.ignore.javalang;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.lang.ClassExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

/**
 * Removes all superclass methods of the class, but doesn't add any
 * {@link Facet}s.
 */
public class RemoveSuperclassMethodsFacetFactory extends FacetFactoryAbstract {

    @SuppressWarnings("unused")
    private static final String JAVA_CLASS_PREFIX = "java.";

    public RemoveSuperclassMethodsFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        removeSuperclassMethods(processClassContext.getCls(), processClassContext);
    }

    private void removeSuperclassMethods(final Class<?> type, final MethodRemover methodRemover) {
        if (type == null) {
            return;
        }

        if (!ClassExtensions.isJavaClass(type)) {
            removeSuperclassMethods(type.getSuperclass(), methodRemover);
            return;
        }

        final Method[] methods = type.getMethods();
        for (final Method method : methods) {
            methodRemover.removeMethod(method);
        }

    }

}
