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


package org.apache.isis.extensions.nosql;

import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.PersistenceCommandContext;


final class DestroyObjectCommandImplementation implements DestroyObjectCommand {
    private final ObjectAdapter object;
    private final KeyCreator keyCreator;
    private final VersionCreator versionCreator;

    public DestroyObjectCommandImplementation(KeyCreator keyCreator, VersionCreator versionCreator, ObjectAdapter object) {
        this.keyCreator = keyCreator;
        this.versionCreator = versionCreator;
        this.object = object;
    }

    public void execute(final PersistenceCommandContext context) {
        String key = keyCreator.key(object.getOid());
        String version = versionCreator.versionString(object.getVersion());
        String specificationName = object.getSpecification().getFullName();

        ((NoSqlCommandContext) context).delete(specificationName, key, version);
    }

    public ObjectAdapter onObject() {
        return object;
    }

    public String toString() {
        ToString toString = new ToString(this);
        toString.append("oid", object.getOid());
        return toString.toString();
    }
}

