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
package org.apache.isis.testing.fixtures.applib.legacy.teardown;

import javax.inject.Inject;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.metadata.DiscriminatorMetadata;
import javax.jdo.metadata.InheritanceMetadata;
import javax.jdo.metadata.TypeMetadata;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.persistence.jdo.applib.services.IsisJdoSupport;

@Programmatic
public abstract class TeardownFixtureAbstract extends FixtureScript {


    protected void deleteFrom(Class<?> cls) {

        preDeleteFrom(cls);

        final String column = discriminatorColumnOf(cls);
        final String value = discriminatorValueOf(cls);

        if(column == null || value == null) {

            doDeleteFrom(cls);

        } else {

            final String schema = schemaOf(cls);
            final String table = tableOf(cls);

            if (_Strings.isNullOrEmpty(schema)) {
                deleteFromWhere(table, column, value);
            } else {
                deleteFromWhere(schema, table, column, value);
            }
        }

        postDeleteFrom(cls);
    }

    private void doDeleteFrom(Class<?> cls) {
        final TypeMetadata metadata = isisJdoSupport.getJdoPersistenceManager()
                        .getPersistenceManagerFactory().getMetadata(cls.getName());
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
    }

    protected void preDeleteFrom(final Class<?> cls) {}

    protected void postDeleteFrom(final Class<?> cls) {}


    protected Integer deleteFrom(final String schema, final String table) {
        return isisJdoSupport.executeUpdate(String.format("DELETE FROM \"%s\".\"%s\"", schema, table));
    }

    protected void deleteFrom(final String table) {
        isisJdoSupport.executeUpdate(String.format("DELETE FROM \"%s\"", table));
    }



    private String schemaOf(final Class<?> cls) {
        TypeMetadata metadata = getPersistenceManagerFactory().getMetadata(cls.getName());
        if(metadata == null) {
            return null;
        }
        final InheritanceMetadata inheritanceMetadata = metadata.getInheritanceMetadata();
        if(inheritanceMetadata != null && inheritanceMetadata.getStrategy() == InheritanceStrategy.SUPERCLASS_TABLE) {
            return schemaOf(cls.getSuperclass());
        }
        return metadata.getSchema();
    }

    private String tableOf(final Class<?> cls) {
        final TypeMetadata metadata = getPersistenceManagerFactory().getMetadata(cls.getName());
        if(metadata == null) {
            return cls.getSimpleName();
        }
        final InheritanceMetadata inheritanceMetadata = metadata.getInheritanceMetadata();
        if(inheritanceMetadata != null && inheritanceMetadata.getStrategy() == InheritanceStrategy.SUPERCLASS_TABLE) {
            return tableOf(cls.getSuperclass());
        }
        final String table = metadata.getTable();
        return !_Strings.isNullOrEmpty(table) ? table : cls.getSimpleName();
    }

    private String discriminatorValueOf(final Class<?> cls) {
        TypeMetadata metadata = getPersistenceManagerFactory().getMetadata(cls.getName());
        if(metadata == null) {
            return null;
        }
        final InheritanceMetadata inheritanceMetadata = metadata.getInheritanceMetadata();
        if(inheritanceMetadata == null ||
                inheritanceMetadata.getStrategy() != InheritanceStrategy.SUPERCLASS_TABLE) {
            return null;
        }
        final DiscriminatorMetadata discriminatorMetadata = inheritanceMetadata.getDiscriminatorMetadata();
        if(discriminatorMetadata == null ||
                discriminatorMetadata.getStrategy() != DiscriminatorStrategy.VALUE_MAP) {
            return null;
        }
        return discriminatorMetadata.getValue();
    }

    private String discriminatorColumnOf(final Class<?> cls) {
        final String discriminator = doDiscriminatorOf(cls);
        return discriminator != null ? discriminator : "discriminator";
    }

    private String doDiscriminatorOf(final Class<?> cls) {
        final TypeMetadata metadata = getPersistenceManagerFactory().getMetadata(cls.getName());
        if(metadata == null) {
            return null;
        }
        final InheritanceMetadata inheritanceMetadata = metadata.getInheritanceMetadata();
        if(inheritanceMetadata == null ||
                inheritanceMetadata.getStrategy() != InheritanceStrategy.SUPERCLASS_TABLE) {
            return null;
        }
        final DiscriminatorMetadata discriminatorMetadata = inheritanceMetadata.getDiscriminatorMetadata();
        if(discriminatorMetadata == null ||
                discriminatorMetadata.getStrategy() != DiscriminatorStrategy.VALUE_MAP) {
            return null;
        }
        return discriminatorMetadata.getColumn();
    }

    private PersistenceManagerFactory getPersistenceManagerFactory() {
        return isisJdoSupport.getJdoPersistenceManager().getPersistenceManagerFactory();
    }

    protected Integer deleteFromWhere(String schema, String table, String column, String value) {
        final String sql = String.format(
                "DELETE FROM \"%s\".\"%s\" WHERE \"%s\"='%s'",
                schema, table, column, value);
        return this.isisJdoSupport.executeUpdate(sql);
    }

    protected void deleteFromWhere(String table, String column, String value) {
        final String sql = String.format(
                "DELETE FROM \"%s\" WHERE \"%s\"='%s'",
                table, column, value);
        this.isisJdoSupport.executeUpdate(sql);
    }

    @Inject private IsisJdoSupport isisJdoSupport;


}
