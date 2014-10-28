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
import java.util.List;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

public class RemoveInjectMethodsFacetFactory extends FacetFactoryAbstract  {

    public RemoveInjectMethodsFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final List<Class<?>> serviceClasses = getSpecificationLoader().getServiceClasses();
        for (Class<? extends Object> serviceClass : serviceClasses) {
            Method[] methods = processClassContext.getCls().getMethods();
            for (Method method : methods) {
                if(getSpecificationLoader().isInjectorMethodFor(method, serviceClass)) {
                    processClassContext.removeMethod(method);
                }
            }
        }
    }

}
