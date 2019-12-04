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
package org.apache.isis.persistence.jdo.entities;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.persistence.jdo.metamodel.JdoMetamodelUtil;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.NoArgsConstructor;
import lombok.Synchronized;
import lombok.val;

@NoArgsConstructor
public class JdoEntityTypeRegistry {

    private Set<String> entityTypes;

    @Synchronized
    public Set<String> getEntityTypes(IsisBeanTypeRegistry isisBeanTypeRegistry) {
        if(entityTypes==null) {
            entityTypes = Collections.unmodifiableSet(findEntityTypes(isisBeanTypeRegistry));
        }
        return entityTypes;
    }

    // -- HELPER

    private static Set<String> findEntityTypes(IsisBeanTypeRegistry isisBeanTypeRegistry) {

        val entityTypes = new LinkedHashSet<String>();

        Set<Class<?>> persistenceCapableTypes = isisBeanTypeRegistry.getEntityTypes();

        val classNamesNotEnhanced = _Lists.<String>newArrayList();
        for (Class<?> persistenceCapableType : persistenceCapableTypes) {
            if(ignore(persistenceCapableType)) {
                continue;
            }
            if(!JdoMetamodelUtil.isPersistenceEnhanced(persistenceCapableType)) {
                classNamesNotEnhanced.add(persistenceCapableType.getCanonicalName());
            }
            entityTypes.add(persistenceCapableType.getCanonicalName());
        }

        if(!classNamesNotEnhanced.isEmpty()) {
            final String classNamesNotEnhancedStr = 
                    stream(classNamesNotEnhanced).collect(Collectors.joining("\n* "));
            throw new IllegalStateException("Non-enhanced @PersistenceCapable classes found, will abort.  The classes in error are:\n\n* " + classNamesNotEnhancedStr + "\n\nDid the DataNucleus enhancer run correctly?\n");
        }

        return entityTypes;
    }


    private static boolean ignore(final Class<?> entityType) {
        try {
            if(entityType.isAnonymousClass() || entityType.isLocalClass() || entityType.isMemberClass() ||
                    entityType.isInterface() || entityType.isAnnotation()) {
                return true;
            }
            final PersistenceCapable persistenceCapable = entityType.getAnnotation(PersistenceCapable.class);
            return persistenceCapable == null; // ignore if doesn't have @PersistenceCapable
        } catch (NoClassDefFoundError ex) {
            return true;
        }
    }


}
