/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.plugins.jdo.dn5;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import org.apache.isis.core.metamodel.IsisJdoMetamodelPlugin;
import org.apache.isis.core.metamodel.IsisJdoRuntimePlugin;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory5;

public class IsisJdoSupportPlugin5 implements IsisJdoMetamodelPlugin, IsisJdoRuntimePlugin {

    @Override
    public boolean isPersistenceEnhanced(@Nullable Class<?> cls) {
        if(cls==null) {
            return false;
        }
        return org.datanucleus.enhancement.Persistable.class.isAssignableFrom(cls);
    }

    @Override
    public Method[] getMethodsProvidedByEnhancement() {
        return org.datanucleus.enhancement.Persistable.class.getDeclaredMethods();
    }

    @Override
    public PersistenceSessionFactory getPersistenceSessionFactory() {
        return new PersistenceSessionFactory5();
    }

}
