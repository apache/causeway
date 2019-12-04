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
package org.apache.isis.persistence.jdo.jdosupport;

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.persistence.jdo.metamodel.IsisJdoMetamodelPlugin;

public class IsisJdoMetamodelPlugin5 implements IsisJdoMetamodelPlugin {

    @Override
    public boolean isPersistenceEnhanced(Class<?> cls) {
        if(cls==null) {
            return false;
        }
        return org.datanucleus.enhancement.Persistable.class.isAssignableFrom(cls);
    }

    @Override
    public boolean isMethodProvidedByEnhancement(Method method) {
        if(method==null) {
            return false;
        }
        ensureInit();
        return /*methodStartsWith(method, "jdo") || */ 
                jdoMethodsProvidedByEnhancement.contains(method.toString());
    }
    
    // -- HELPER

    private final Set<String> jdoMethodsProvidedByEnhancement = _Sets.newHashSet();
    
    private static Method[] getMethodsProvidedByEnhancement() {
        return org.datanucleus.enhancement.Persistable.class.getDeclaredMethods();
    }

    private void ensureInit() {
        if(jdoMethodsProvidedByEnhancement.isEmpty()) {
            _NullSafe.stream(getMethodsProvidedByEnhancement())
            .map(Method::toString)
            .forEach(jdoMethodsProvidedByEnhancement::add);
        }
    }
    
    
}
