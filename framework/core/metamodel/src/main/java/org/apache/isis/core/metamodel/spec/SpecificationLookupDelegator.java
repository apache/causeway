/**
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
package org.apache.isis.core.metamodel.spec;

import java.util.Collection;

/**
 * Allows a SpecificationLookup to be provided even if the concrete
 * implementation is only available later.
 */
public class SpecificationLookupDelegator extends SpecificationLookupAbstract {

    private SpecificationLookup specificationLookupDelegate;

    public void setDelegate(final SpecificationLookup specificationLookupDelegate) {
        this.specificationLookupDelegate = specificationLookupDelegate;
    }

    @Override
    public ObjectSpecification loadSpecification(final Class<?> cls) {
        if (specificationLookupDelegate == null) {
            throw new IllegalStateException("No SpecificationLookup provided");
        }
        return specificationLookupDelegate.loadSpecification(cls);
    }

    @Override
    public Collection<ObjectSpecification> allSpecifications() {
        return specificationLookupDelegate.allSpecifications();
    }

    @Override
    public ObjectSpecification lookupBySpecId(ObjectSpecId objectSpecId) {
        return specificationLookupDelegate.lookupBySpecId(objectSpecId);
    }

    @Override
    public void injectInto(Object candidate) {
        super.injectInto(candidate);
        if(specificationLookupDelegate != null) {
            specificationLookupDelegate.injectInto(candidate);
        }
    }
}
