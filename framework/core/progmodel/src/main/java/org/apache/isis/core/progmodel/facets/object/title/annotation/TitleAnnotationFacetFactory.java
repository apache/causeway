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

package org.apache.isis.core.progmodel.facets.object.title.annotation;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.progmodel.facets.MethodFinderUtils;
import org.apache.isis.core.progmodel.facets.fallback.FallbackFacetFactory;

public class TitleAnnotationFacetFactory extends FacetFactoryAbstract {

    public TitleAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    /**
     * If no method tagged with {@link Title} annotation then will use Facets provided by {@link FallbackFacetFactory} instead.
     */
    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        // TODO - MNour: Add method in MethodFinderUtils to find methods with a specified annotation. 
        List<Method> methods = MethodFinderUtils.findMethodsWithAnnotation(cls, MethodScope.OBJECT, Title.class);

        if (!methods.isEmpty()) {
	        for (Method method : methods) {
	            processClassContext.removeMethod(method);
	        }
	        FacetUtil.addFacet(new TitleFacetViaTitleAnnotation(methods, facetHolder));
	        return;
        }
    }
}
