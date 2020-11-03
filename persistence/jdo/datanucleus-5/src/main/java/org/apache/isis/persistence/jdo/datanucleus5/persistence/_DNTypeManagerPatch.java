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
package org.apache.isis.persistence.jdo.datanucleus5.persistence;

import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.AbstractNucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.PersistenceUnitMetaData;

import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.runtime.context.IsisContext;

import lombok.SneakyThrows;
import lombok.val;

/**
 * TODO remove once fixed upstream! (DN-core 5.2.5)
 * <p>
 * Patched from DN original in support of priority based type registration.
 * While documented, was never implemented.
 * <p>
 * See also
 * https://github.com/datanucleus/datanucleus-core/pull/360
 */
@Deprecated
public class _DNTypeManagerPatch {

    public static PersistenceManagerFactory newPersistenceManagerFactory(Map<String, Object> datanucleusProps) {
        
        datanucleusProps.put(
                "javax.jdo.PersistenceManagerFactoryClass", 
                "org.apache.isis.persistence.jdo.datanucleus5.persistence._DNTypeManagerPatch$PersistenceManagerFactory2");
        
        val pmFactory =  JDOHelper.getPersistenceManagerFactory(datanucleusProps, IsisContext.getClassLoader());
        
        return pmFactory;
    }

    public static class PersistenceManagerFactory2 extends JDOPersistenceManagerFactory {
        private static final long serialVersionUID = 1L;
        
        public PersistenceManagerFactory2(Map props) {
            super(props);
        }
        
        @Override @SneakyThrows
        protected void initialiseMetaData(PersistenceUnitMetaData pumd) {
            
            val typeManager2 = new TypeManagerImpl2(nucleusContext);
            val tmField = AbstractNucleusContext.class.getDeclaredField("typeManager");
            
            _Reflect.setFieldOn(tmField, nucleusContext, typeManager2);
            
            super.initialiseMetaData(pumd);
        }
        
    }
    
}
