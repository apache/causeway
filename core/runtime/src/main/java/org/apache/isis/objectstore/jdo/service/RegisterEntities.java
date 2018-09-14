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
package org.apache.isis.objectstore.jdo.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.JdoMetamodelUtil;

public class RegisterEntities {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(RegisterEntities.class);

    /**
     * @deprecated - no longer used; instead an AppManifest must be specified.
     */
    @Deprecated
    public final static String PACKAGE_PREFIX_KEY = "isis.persistor.datanucleus.RegisterEntities.packagePrefix";

    private final _Lazy<Set<String>> entityTypes = _Lazy.threadSafe(this::findEntityTypes);

    public Set<String> getEntityTypes() {
        return entityTypes.get();
    }

    // -- HELPER

    private Set<String> findEntityTypes() {
        
        Set<String> entityTypes = new LinkedHashSet<>();

        Set<Class<?>> persistenceCapableTypes = AppManifest.Registry.instance().getPersistenceCapableTypes();

        if(persistenceCapableTypes == null) {
            throw new IllegalStateException("AppManifest is required");
        }

        final List<String> classNamesNotEnhanced = Lists.newArrayList();
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
            final String classNamesNotEnhancedStr = Joiner.on("\n* ").join(classNamesNotEnhanced);
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
