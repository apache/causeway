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

package org.apache.isis.metamodel.adapter.oid.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.isis.metamodel.adapter.oid.factory.OidFactory.OidFactoryBuilder;
import org.apache.isis.metamodel.adapter.oid.factory.OidFactory.OidProvider;

import lombok.val;

class OidFactory_Builder implements OidFactoryBuilder {

    private final List<OidProvider> handler = new ArrayList<>();

    @Override
    public OidFactoryBuilder add(OidProvider oidProvider) {
        handler.add(oidProvider);
        return this;
    }

    @Override
    public OidFactory build() {
        return managedObject -> {

            val rootOid = handler.stream()
                    .filter(h->h.isHandling(managedObject))
                    .findFirst()
                    .map(h->h.oidFor(managedObject))
                    .orElse(null);

            Objects.requireNonNull(rootOid, 
                    () -> "Could not create an Oid for managedObject: " + managedObject);

            return rootOid;
        };
    }


}
