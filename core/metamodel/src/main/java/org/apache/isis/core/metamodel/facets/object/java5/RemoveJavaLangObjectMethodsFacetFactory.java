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


package org.apache.isis.core.metamodel.facets.object.java5;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.facets.MethodScope;
import org.apache.isis.core.metamodel.feature.FeatureType;


/**
 * Removes all methods inherited from {@link Object}.
 */
public class RemoveJavaLangObjectMethodsFacetFactory extends FacetFactoryAbstract {

    public RemoveJavaLangObjectMethodsFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    private static final String[] objectMethodNames;
    private static final Class<?>[] objectMethodReturnTypes;
    private static final Class<?>[][] objectMethodParameters;

    static {
        final Method[] methods = Object.class.getMethods();
        final int size = methods.length;
        objectMethodNames = new String[size];
        objectMethodReturnTypes = new Class[size];
        objectMethodParameters = new Class[size][];
        for (int i = 0; i < methods.length; i++) {
            final Method method = methods[i];
            objectMethodNames[i] = method.getName();
            objectMethodReturnTypes[i] = method.getReturnType();
            objectMethodParameters[i] = method.getParameterTypes();
        }
    }

    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        final Method[] methods = Object.class.getMethods();
        for (int i = 0; i < methods.length; i++) {
            methodRemover.removeMethod(MethodScope.OBJECT, objectMethodNames[i], null, objectMethodParameters[i]);
        }

        return false;
    }

}

