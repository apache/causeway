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

package org.apache.isis.objectstore.sql;

import java.util.Date;

import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;


public abstract class AbstractMapper {
    
    public abstract void createTables(final DatabaseConnector connector);

    protected boolean needsTables(final DatabaseConnector connector) {
        return false;
    }

    public void startup(final DatabaseConnector connector) {
        if (needsTables(connector)) {
            createTables(connector);
        }
    }

    public final void shutdown() {
    }

    protected String asSqlName(final String name) {
        return Sql.sqlName(name);
    }
    
    protected Version createVersion(final long versionSequence) {
        return SerialNumberVersion.create(versionSequence, "", new Date());
    }

}
