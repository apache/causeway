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
package org.apache.isis.core.runtime.persistence.adaptermanager;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.runtime.PersistorImplementation;

public class PojoRecreatorUnified implements PojoRecreator {

    private final PojoRecreator pojoRecreator;

    public PojoRecreatorUnified(final IsisConfiguration configuration) {
        this.pojoRecreator =
                PersistorImplementation.from(configuration).isDataNucleus()
                    ? new PojoRecreatorForDataNucleus()
                    : new PojoRecreatorDefault();
    }

    public Object recreatePojo(final TypedOid oid) {
        return pojoRecreator.recreatePojo(oid);
    }

    public ObjectAdapter lazilyLoaded(Object pojo) {
        return pojoRecreator.lazilyLoaded(pojo);
    }
    
}
