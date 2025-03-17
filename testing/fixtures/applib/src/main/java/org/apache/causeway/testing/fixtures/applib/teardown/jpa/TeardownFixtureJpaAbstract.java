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
package org.apache.causeway.testing.fixtures.applib.teardown.jpa;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;

/**
 * @since 2.x {@index}
 */
@Programmatic
public abstract class TeardownFixtureJpaAbstract extends FixtureScript {

    protected void deleteFrom(Class<?> cls) {

        preDeleteFrom(cls);
        repositoryService.removeAll(cls);
        postDeleteFrom(cls);
    }

    protected void preDeleteFrom(final Class<?> cls) {}

    protected void postDeleteFrom(final Class<?> cls) {}

//legacy JDO code, needs conversion    
//  
//    protected Integer deleteFrom(final String schema, final String table) {
//        if (_Strings.isNullOrEmpty(schema)) {
//            return deleteFrom(table);
//        } else {
//            return jdoSupport.executeUpdate(String.format("DELETE FROM \"%s\".\"%s\"", schema, table));
//        }
//    }
//
//    protected Integer deleteFrom(final String table) {
//        return jdoSupport.executeUpdate(String.format("DELETE FROM \"%s\"", table));
//    }
//
//    protected Integer deleteFromWhere(String schema, String table, String column, String value) {
//        if (_Strings.isNullOrEmpty(schema)) {
//            return deleteFromWhere(table, column, value);
//        } else {
//            final String sql = String.format(
//                    "DELETE FROM \"%s\".\"%s\" WHERE \"%s\"='%s'",
//                    schema, table, column, value);
//            return this.jdoSupport.executeUpdate(sql);
//        }
//    }
//
//    protected Integer deleteFromWhere(String table, String column, String value) {
//        final String sql = String.format(
//                "DELETE FROM \"%s\" WHERE \"%s\"='%s'",
//                table, column, value);
//        return this.jdoSupport.executeUpdate(sql);
//    }
//
//    private String schemaOf(final Class<?> cls) {
//        TypeMetadata metadata = getPersistenceManagerFactory().getMetadata(cls.getName());
//        if(metadata == null) {
//            return null;
//        }
//        final InheritanceMetadata inheritanceMetadata = metadata.getInheritanceMetadata();
//        if(inheritanceMetadata != null && inheritanceMetadata.getStrategy() == InheritanceStrategy.SUPERCLASS_TABLE) {
//            return schemaOf(cls.getSuperclass());
//        }
//        return metadata.getSchema();
//    }
//
//    private String tableOf(final Class<?> cls) {
//        final TypeMetadata metadata = getPersistenceManagerFactory().getMetadata(cls.getName());
//        if(metadata == null) {
//            return cls.getSimpleName();
//        }
//        final InheritanceMetadata inheritanceMetadata = metadata.getInheritanceMetadata();
//        if(inheritanceMetadata != null && inheritanceMetadata.getStrategy() == InheritanceStrategy.SUPERCLASS_TABLE) {
//            return tableOf(cls.getSuperclass());
//        }
//        final String table = metadata.getTable();
//        return !_Strings.isNullOrEmpty(table) ? table : cls.getSimpleName();
//    }
//
//    private String discriminatorValueOf(final Class<?> cls) {
//        TypeMetadata metadata = getPersistenceManagerFactory().getMetadata(cls.getName());
//        if(metadata == null) {
//            return null;
//        }
//        final InheritanceMetadata inheritanceMetadata = metadata.getInheritanceMetadata();
//        if(inheritanceMetadata == null ||
//                inheritanceMetadata.getStrategy() != InheritanceStrategy.SUPERCLASS_TABLE) {
//            return null;
//        }
//        final DiscriminatorMetadata discriminatorMetadata = inheritanceMetadata.getDiscriminatorMetadata();
//        if(discriminatorMetadata == null ||
//                discriminatorMetadata.getStrategy() != DiscriminatorStrategy.VALUE_MAP) {
//            return null;
//        }
//        return discriminatorMetadata.getValue();
//    }
//
//    private String discriminatorColumnOf(final Class<?> cls) {
//        final String discriminator = doDiscriminatorOf(cls);
//        return discriminator != null ? discriminator : "discriminator";
//    }
//
//    private String doDiscriminatorOf(final Class<?> cls) {
//        final TypeMetadata metadata = getPersistenceManagerFactory().getMetadata(cls.getName());
//        if(metadata == null) {
//            return null;
//        }
//        final InheritanceMetadata inheritanceMetadata = metadata.getInheritanceMetadata();
//        if(inheritanceMetadata == null ||
//                inheritanceMetadata.getStrategy() != InheritanceStrategy.SUPERCLASS_TABLE) {
//            return null;
//        }
//        final DiscriminatorMetadata discriminatorMetadata = inheritanceMetadata.getDiscriminatorMetadata();
//        if(discriminatorMetadata == null ||
//                discriminatorMetadata.getStrategy() != DiscriminatorStrategy.VALUE_MAP) {
//            return null;
//        }
//        return discriminatorMetadata.getColumn();
//    }
//
//    private PersistenceManagerFactory getPersistenceManagerFactory() {
//        return jdoSupport.getPersistenceManager().getPersistenceManagerFactory();
//    }
//
//    @Inject private JdoSupportService jdoSupport;

    
}
