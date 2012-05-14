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

package org.apache.isis.core.metamodel.specloader;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;

class SpecificationCacheDefault {
    
    private final Map<String, ObjectSpecification> specByClassName = Maps.newHashMap();
    private Map<ObjectSpecId, ObjectSpecification> specById;

    public ObjectSpecification get(final String className) {
        return specByClassName.get(className);
    }

    public void cache(final String className, final ObjectSpecification spec) {
        specByClassName.put(className, spec);
    }

    public void clear() {
        specByClassName.clear();
    }

    public Collection<ObjectSpecification> allSpecifications() {
        return Collections.unmodifiableCollection(specByClassName.values());
    }

    public ObjectSpecification getByObjectType(ObjectSpecId objectSpecID) {
        if (specById == null) {
            throw new IllegalStateException("SpecificationCache by object type has not yet been initialized");
        }
        return specById.get(objectSpecID);
    }

    /**
     * Populated as a result of running {@link MetaModelValidator#validate() validation} after all specs have been loaded. 
     */
    void setCacheBySpecId(Map<ObjectSpecId, ObjectSpecification> specById) {
        this.specById = Maps.newHashMap();
        this.specById.putAll(specById);
    }

}
