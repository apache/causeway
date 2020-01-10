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
package org.apache.isis.persistence.jdo.datanucleus5.objectadapter;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.session.RuntimeContext;

import lombok.val;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: provides ObjectAdapterProvider implementation
 * </p> 
 * @since 2.0
 */
class ObjectAdapterContext_ObjectAdapterProvider implements ObjectAdapterProvider {

    private final ObjectAdapterContext objectAdapterContext;
    private final SpecificationLoader specificationLoader; 
    private final ObjectManager objectManager; 

    ObjectAdapterContext_ObjectAdapterProvider(
            ObjectAdapterContext objectAdapterContext,
            RuntimeContext runtimeContext) {

        this.objectAdapterContext = objectAdapterContext;
        this.specificationLoader = runtimeContext.getSpecificationLoader();
        this.objectManager = runtimeContext.getMetaModelContext().getObjectManager(); 
    }

    @Override
    public ObjectAdapter adapterFor(Object pojo) {

        if(pojo == null) {
            return null;
        }

        val rootOid = objectManager.identifyObject(ManagedObject.of(specificationLoader::loadSpecification, pojo));
        val newAdapter = objectAdapterContext.getFactories().createRootAdapter(pojo, rootOid);
        return objectAdapterContext.injectServices(newAdapter);
    }


}