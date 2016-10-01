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

package org.apache.isis.core.metamodel.facets.actions.action.factorymethod;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;

public class FactoryMethodFacetForActionAnnotation  extends SingleValueFacetAbstract<Class<?>> implements FactoryMethodFacet {

    public static FactoryMethodFacet create(
            final Action action,
            final Method method,
            final FacetHolder holder) {

        if (action == null) {
            return null;
        }

        final boolean factoryMethod = action.factoryMethod();

        Class<?> returnType = method.getReturnType();

        if (!factoryMethod) {
            return null;
        } else {
            if (returnType == void.class) {
                throw new MetaModelException("The Factory Method action '" + method.toString() + "' cannot return void");
            } else {
                return new FactoryMethodFacetForActionAnnotation(returnType, holder);
            }
        }
    }

    private FactoryMethodFacetForActionAnnotation(final Class<?> forClass, final FacetHolder holder) {
        super(FactoryMethodFacet.class, forClass, holder);
    }

    public Class<? extends Facet>[] facetTypes() {
        return new Class[]{facetType(), FactoryMethodFacet.class};
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facet) {
        return (T) this;
    }

    @Override
    public boolean containsFacetTypeOf(final Class<? extends Facet> requiredFacetType) {
        for (final Class<? extends Facet> facetType : facetTypes()) {
            if(facetType == requiredFacetType) {
                return true;
            }
        }
        return false;
    }
}
