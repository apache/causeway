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
package org.apache.isis.persistence.jdo.datanucleus.entities;

import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.Nullable;

import org.datanucleus.enhancement.Persistable;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.JdoEntityFacet;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.val;

@Component
public class DnEntityStateProvider implements JdoFacetContext {

    @Override
    public EntityState getEntityState(final Object pojo) {
        return entityState(pojo);
    }

    @Override
    public boolean isPersistenceEnhanced(@Nullable final Class<?> cls) {
        if(cls==null) {
            return false;
        }
        return org.datanucleus.enhancement.Persistable.class.isAssignableFrom(cls);
    }

    @Override
    public boolean isMethodProvidedByEnhancement(@Nullable final Method method) {
        if(method==null) {
            return false;
        }
        ensureInit();
        return /*methodStartsWith(method, "jdo") || */
                jdoMethodsProvidedByEnhancement.contains(method.toString());
    }

    public static EntityState entityState(final Object pojo) {

        if(pojo==null) {
            return EntityState.NOT_PERSISTABLE;
        }

        if (pojo!=null && pojo instanceof Persistable) {
            val persistable = (Persistable) pojo;
            val isDeleted = persistable.dnIsDeleted();
            if(isDeleted) {
                return EntityState.PERSISTABLE_REMOVED;
            }
            val isPersistent = persistable.dnIsPersistent();
            if(isPersistent) {
                return EntityState.PERSISTABLE_ATTACHED;
            }
            return EntityState.PERSISTABLE_DETACHED;
        }
        return EntityState.NOT_PERSISTABLE;
    }

    // -- HELPER

    private static final Set<String> jdoMethodsProvidedByEnhancement = _Sets.newHashSet();

    private static Method[] getMethodsProvidedByEnhancement() {
        return org.datanucleus.enhancement.Persistable.class.getDeclaredMethods();
    }

    private static void ensureInit() {
        if(jdoMethodsProvidedByEnhancement.isEmpty()) {
            _NullSafe.stream(getMethodsProvidedByEnhancement())
            .map(Method::toString)
            .forEach(jdoMethodsProvidedByEnhancement::add);
        }
    }

    @Override
    public EntityFacet createEntityFacet(final FacetHolder facetHolder) {
        return new JdoEntityFacet(facetHolder);
    }

}
