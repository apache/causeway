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

package org.apache.isis.objectstore.nosql;

import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.objectstore.nosql.keys.KeyCreatorDefault;
import org.apache.isis.objectstore.nosql.versions.VersionCreator;

final class NoSqlDestroyObjectCommand implements DestroyObjectCommand {
    
    private final KeyCreatorDefault keyCreator = new KeyCreatorDefault();
    
    private final ObjectAdapter adapter;
    private final VersionCreator versionCreator;

    public NoSqlDestroyObjectCommand(final VersionCreator versionCreator, final ObjectAdapter adapter) {
        this.versionCreator = versionCreator;
        this.adapter = adapter;
    }

    @Override
    public void execute(final PersistenceCommandContext context) {
        final String key = keyCreator.getIdentifierForPersistentRoot(adapter.getOid());
        final String version = versionCreator.versionString(adapter.getVersion());
        final ObjectSpecification objectSpec = adapter.getSpecification();

        final NoSqlCommandContext noSqlCommandContext = (NoSqlCommandContext) context;
        noSqlCommandContext.delete(objectSpec.getSpecId(), key, version, adapter.getOid());
    }

    @Override
    public ObjectAdapter onAdapter() {
        return adapter;
    }

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        toString.append("spec", adapter.getSpecification().getFullIdentifier());
        toString.append("oid", adapter.getOid());
        return toString.toString();
    }
}
