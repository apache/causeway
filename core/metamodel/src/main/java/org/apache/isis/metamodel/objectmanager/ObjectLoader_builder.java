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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.objectmanager.ObjectLoader.ObjectLoaderBuilder;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
class ObjectLoader_builder implements ObjectLoaderBuilder {
    
    private final MetaModelContext metaModelContext;

    private final List<ObjectLoader.Handler> handlers = new ArrayList<>();

    @Override
    public ObjectLoader_builder add(ObjectLoader.Handler handler) {
        handler.setMetaModelContext(metaModelContext);
        handlers.add(handler);
        return this;
    }

    @Override
    public ObjectLoader build() {
        return oid -> {

            val managedObject = handlers.stream()
                    .filter(h->h.isHandling(oid))
                    .findFirst()
                    .map(h->h.loadObject(oid))
                    .orElse(null);

            Objects.requireNonNull(managedObject, 
                    () -> "Could not create a ManagedObject for Oid: " + oid);

            return managedObject;
        };
    }


}
