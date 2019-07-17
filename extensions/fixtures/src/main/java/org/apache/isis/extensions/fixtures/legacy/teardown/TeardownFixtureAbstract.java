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
package org.apache.isis.extensions.fixtures.legacy.teardown;

import javax.inject.Inject;
import javax.jdo.metadata.TypeMetadata;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.jdosupport.IsisJdoSupport;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;

@Programmatic
public abstract class TeardownFixtureAbstract extends FixtureScript {

    protected void deleteFrom(final Class<?> cls) {
        preDeleteFrom(cls);
        final TypeMetadata metadata = isisJdoSupport.getJdoPersistenceManager().getPersistenceManagerFactory()
                .getMetadata(cls.getName());
        if(metadata == null) {
            // fall-back
            deleteFrom(cls.getSimpleName());
        } else {
            final String schema = metadata.getSchema();
            String table = metadata.getTable();
            if(_Strings.isNullOrEmpty(table)) {
                table = cls.getSimpleName();
            }
            if(_Strings.isNullOrEmpty(schema)) {
                deleteFrom(table);
            } else {
                deleteFrom(schema, table);
            }
        }
        postDeleteFrom(cls);
    }

    protected Integer deleteFrom(final String schema, final String table) {
        return isisJdoSupport.executeUpdate(String.format("DELETE FROM \"%s\".\"%s\"", schema, table));
    }

    protected void deleteFrom(final String table) {
        isisJdoSupport.executeUpdate(String.format("DELETE FROM \"%s\"", table));
    }

    protected void preDeleteFrom(final Class<?> cls) {}

    protected void postDeleteFrom(final Class<?> cls) {}

    @Inject
    private IsisJdoSupport isisJdoSupport;

}
