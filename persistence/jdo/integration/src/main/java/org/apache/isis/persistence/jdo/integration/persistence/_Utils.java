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
package org.apache.isis.persistence.jdo.integration.persistence;

import javax.annotation.Nullable;
import javax.jdo.listener.InstanceLifecycleEvent;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.NonNull;
import lombok.val;

final class _Utils {

    @SuppressWarnings("unused")
    private static Object jdoObjectIdFor(InstanceLifecycleEvent event) {
        Persistable persistenceCapable = _Utils.persistableFor(event);
        Object jdoObjectId = persistenceCapable.dnGetObjectId();
        return jdoObjectId;
    }

    static Persistable persistableFor(InstanceLifecycleEvent event) {
        return (Persistable)event.getSource();
    }
    
    static boolean ensureRootObject(final Persistable pojo) {
//        final Oid oid = adapterFor(pojo).getOid();
//        if (!(oid instanceof RootOid)) {
//            throw new IsisException(MessageFormat.format("Not a RootOid: oid={0}, for {1}", oid, pojo));
//        }
        return pojo!=null; // why would a Persistable ever be something different?
    }
    
    static boolean isJUnitTest() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }           
        }
        return false;
    }

    // -- LOW LEVEL
    
    @Nullable
    static ManagedObject injectServices(
            final @NonNull ServiceInjector serviceInjector,
            final @Nullable ManagedObject adapter) {
        
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            return adapter; 
        }
        
        val spec = adapter.getSpecification();
        if(spec==null 
                || spec.isValue()) {
            return adapter; // guard against value objects
        }
        serviceInjector.injectServicesInto(adapter.getPojo());
        return adapter;
    }
    
}
