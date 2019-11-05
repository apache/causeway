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
package org.apache.isis.metamodel.adapter;

import javax.annotation.Nullable;

import org.apache.isis.metamodel.spec.ObjectSpecification;

/**
 * 
 * @since 2.0
 *
 */
public interface ObjectAdapterProvider {

    // -- INTERFACE

    /**
     * @return standalone (value) or root adapter
     */
    @Nullable ObjectAdapter adapterFor(@Nullable Object pojo);


    // -- DOMAIN OBJECT CREATION SUPPORT

    /**
     * <p>
     * Creates a new instance of the specified type and returns it.
     *
     * <p>
     * The returned object will be initialized (had the relevant callback
     * lifecycle methods invoked).
     *
     * <p>
     * While creating the object it will be initialized with default values and
     * its created lifecycle method (its logical constructor) will be invoked.
     *
     */
    ObjectAdapter newTransientInstance(ObjectSpecification objectSpec);

    // -- FOR THOSE THAT IMPLEMENT THROUGH DELEGATION

    public static interface Delegating extends ObjectAdapterProvider {

        ObjectAdapterProvider getObjectAdapterProvider();

        @Override
        default ObjectAdapter adapterFor(Object domainObject) {
            return getObjectAdapterProvider().adapterFor(domainObject);
        }

        @Override
        default ObjectAdapter newTransientInstance(ObjectSpecification objectSpec) {
            return getObjectAdapterProvider().newTransientInstance(objectSpec);
        }

    }


}
