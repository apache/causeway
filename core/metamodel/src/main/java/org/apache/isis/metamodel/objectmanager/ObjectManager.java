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
package org.apache.isis.metamodel.objectmanager;

import javax.annotation.Nullable;

import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.objectmanager.create.ObjectCreator;
import org.apache.isis.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
public interface ObjectManager {

    MetaModelContext getMetaModelContext();
    ObjectCreator getObjectCreator();
    ObjectLoader getObjectLoader();

    public static ObjectManager of(MetaModelContext metaModelContext) {
        val objectCreator = ObjectCreator.createDefault(metaModelContext);
        val objectLoader = ObjectLoader.createDefault(metaModelContext);
        val objectManager = new ObjectManager_default(metaModelContext, objectLoader, objectCreator);
        return objectManager;
    }

    public default ManagedObject createObject(ObjectCreator.Request objectCreateRequest) {
        return getObjectCreator().createObject(objectCreateRequest);
    }
    
    public default ManagedObject loadObject(ObjectLoader.Request objectLoadRequest) {
        return getObjectLoader().loadObject(objectLoadRequest);
    }
    
    @Nullable
    public default ManagedObject adapt(@Nullable Object pojo) {
        if(pojo==null) {
            return null; // don't propagate null into ManagedObject, null has no type 
        }
        return ManagedObject.of(this::loadSpecification, pojo);
    }

    @Nullable
    public default ObjectSpecification loadSpecification(@Nullable Object pojo) {
        if(pojo==null) {
            return null; 
        }
        return loadSpecification(pojo.getClass());
    }
    
    @Nullable
    default ObjectSpecification loadSpecification(@Nullable final Class<?> domainType) {
        return getMetaModelContext().getSpecificationLoader().loadSpecification(domainType);
    }
    
}
