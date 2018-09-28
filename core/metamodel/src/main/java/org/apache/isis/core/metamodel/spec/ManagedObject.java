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

package org.apache.isis.core.metamodel.spec;

import static org.apache.isis.commons.internal.base._With.requires;

import java.util.function.Supplier;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

/**
 * Represents an instance of some element of the meta-model.
 *
 * <p>
 * Currently the only sub-interface is {@link ObjectAdapter}. However, the
 * intention is for associations, actions and action parameters to also inherit
 * from this interface.
 */
public interface ManagedObject extends Instance {

    /**
     * Returns the specification that details the structure (meta-model) of this object.<br>
     * Note: Refines {@link Instance#getSpecification()}.
     */
    @Override
    ObjectSpecification getSpecification();

    /**
     * Returns the adapted domain object, the 'plain old java' object this managed object 
     * represents with the framework.
     */
    Object getPojo();
    
    // -- GLUE CODE
    
    default Object getObject() {
        return getPojo();
    }
    
    // -- FACTORIES
    
    public static ManagedObject of(ObjectSpecification specification, Object pojo) {
        return new ManagedObject() {
            @Override
            public ObjectSpecification getSpecification() {
                return specification;
            }
            @Override
            public Object getPojo() {
                return pojo;
            }
        };
    }
    
    public static ManagedObject of(Supplier<ObjectSpecification> specificationSupplier, Object pojo) {
        requires(specificationSupplier, "specificationSupplier");
        return new ManagedObject() {
            private final _Lazy<ObjectSpecification> specification = _Lazy.of(specificationSupplier);
            @Override
            public ObjectSpecification getSpecification() {
                return specification.get();
            }
            @Override
            public Object getPojo() {
                return pojo;
            }
        };
    }

    default String titleString(Object object) {
        //FIXME
        return "TODO: ManagedObject.titleString(Object)";
    }

    default String titleString() {
        //FIXME
        return "TODO: ManagedObject.titleString()";
    }
    

}
