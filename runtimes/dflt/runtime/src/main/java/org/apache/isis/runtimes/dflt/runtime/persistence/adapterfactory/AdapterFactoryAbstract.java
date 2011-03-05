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


package org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;

public abstract class AdapterFactoryAbstract implements AdapterFactory {

    public abstract ObjectAdapter createAdapter(Object pojo, Oid oid);

    /**
     * Default implementation does nothing.
     */
    public void open() {}

    /**
     * Default implementation does nothing.
     */
    public void close() {}
    
    /**
     * Injects.
     */
    public void injectInto(Object candidate) {
        if (AdapterFactoryAware.class.isAssignableFrom(candidate.getClass())) {
            AdapterFactoryAware cast = AdapterFactoryAware.class.cast(candidate);
            cast.setAdapterFactory(this);
        }
    }

}


