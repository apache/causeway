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
package org.apache.isis.runtime.persistence.adapter;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "of") 
public class ObjectAdapterForBean implements ObjectAdapter {

    private final ManagedBeanAdapter bean;
    private final SpecificationLoader specificationLoader;

    private ObjectSpecification spec;

    @Override
    public ObjectSpecification getSpecification() {
        if(spec==null) {
            spec = specificationLoader.loadSpecification(bean.getBeanClass());
        }
        return spec;
    }

    @Override
    public Object getPojo() {
        return bean.getInstance().getFirst().orElseThrow(()->
            _Exceptions.unrecoverable("This adapter should not even exist: spec=" + spec));
    }

    @Override
    public Oid getOid() {
        val spec = getSpecification();
        return Oid.Factory.persistentOf(spec.getSpecId(), bean.getId());
    }

    @Override
    public ObjectAdapter getAggregateRoot() {
        return this;
    }

    @Override
    public String toString() {
        return String.format("ObjectAdapterForBean[specId=%s, featureType=%s, moSort=%s]", 
                spec.getSpecId(), 
                spec.getFeatureType(),
                spec.getBeanSort().name());
    }

}