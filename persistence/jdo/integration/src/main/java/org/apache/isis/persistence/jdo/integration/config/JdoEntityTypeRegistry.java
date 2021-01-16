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
package org.apache.isis.persistence.jdo.integration.config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.persistence.jdo.integration.metamodel.JdoMetamodelUtil;

import lombok.NoArgsConstructor;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@NoArgsConstructor
@Log4j2
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

        val classNamesNotEnhanced = _Lists.<String>newArrayList();
        
        val entityTypes = isisBeanTypeRegistry.getEntityTypes()
        .stream()
        .filter(JdoEntityTypeRegistry::isJdo)
        .filter(entityType->{
            if(!JdoMetamodelUtil.isPersistenceEnhanced(entityType)) {
                classNamesNotEnhanced.add(entityType.getCanonicalName());
                return false;
            }
            return true;
        })
        .map(Class::getCanonicalName)
        .collect(Collectors.toCollection(LinkedHashSet::new));

        if(!classNamesNotEnhanced.isEmpty()) {
            val classNamesNotEnhancedStr = classNamesNotEnhanced.stream()
            .collect(Collectors.joining("\n* "));
            throw new IllegalStateException(
                    "Non-enhanced @PersistenceCapable classes found, will abort.  "
                    + "The classes in error are:\n\n* " + classNamesNotEnhancedStr + "\n\n"
                            + "Did the DataNucleus enhancer run correctly?\n");
        }

        return entityTypes;
    }


    private static boolean isJdo(final Class<?> entityType) {
        try {
            if(entityType.isAnonymousClass() || entityType.isLocalClass() || entityType.isMemberClass() ||
                    entityType.isInterface() || entityType.isAnnotation()) {
                return false;
            }
            final PersistenceCapable persistenceCapable = entityType.getAnnotation(PersistenceCapable.class);
            return persistenceCapable != null; // false if doesn't have @PersistenceCapable
        } catch (NoClassDefFoundError ex) {
            log.error("failed to determine whether entity is a type to be managed by JDO", ex);
            return false; // silently ignore
        }
    }


}
