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

package org.apache.isis.runtimes.dflt.objectstores.nosql;

import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;

final class NoSqlDestroyObjectCommand implements DestroyObjectCommand {
    private final ObjectAdapter object;
    private final KeyCreator keyCreator;
    private final VersionCreator versionCreator;

    public NoSqlDestroyObjectCommand(final KeyCreator keyCreator, final VersionCreator versionCreator,
        final ObjectAdapter object) {
        this.keyCreator = keyCreator;
        this.versionCreator = versionCreator;
        this.object = object;
    }

    @Override
    public void execute(final PersistenceCommandContext context) {
        final String key = keyCreator.key(object.getOid());
        final String version = versionCreator.versionString(object.getVersion());
        final String specificationName = object.getSpecification().getFullIdentifier();

        ((NoSqlCommandContext) context).delete(specificationName, key, version);
    }

    @Override
    public ObjectAdapter onObject() {
        return object;
    }

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        toString.append("spec", object.getSpecification().getFullIdentifier());
        toString.append("oid", object.getOid());
        return toString.toString();
    }
}
