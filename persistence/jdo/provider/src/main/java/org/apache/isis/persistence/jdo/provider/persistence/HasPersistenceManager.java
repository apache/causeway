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
package org.apache.isis.persistence.jdo.provider.persistence;

@FunctionalInterface
public interface HasPersistenceManager {

    // -- INTERFACE

    javax.jdo.PersistenceManager getPersistenceManager();

    // -- QUERY SHURTCUTS

    /**
     * Not type safe. For type-safe queries use <br/><br/> {@code pm().newNamedQuery(cls, queryName)}
     * @param cls
     * @param queryName
     */
    default <T> javax.jdo.Query<T> newJdoNamedQuery(Class<T> cls, String queryName){
        return getPersistenceManager().newNamedQuery(cls, queryName);
    }

    /**
     * Not type safe. For type-safe queries use <br/><br/> {@code pm().newQuery(cls, queryName)}
     * @param cls
     */
    default <T> javax.jdo.Query<T> newJdoQuery(Class<T> cls){
        return getPersistenceManager().newQuery(cls);
    }

    /**
     * Not type safe. For type-safe queries use <br/><br/> {@code pm().newQuery(cls, filter)}
     * @param cls
     * @param filter
     */
    default <T> javax.jdo.Query<T> newJdoQuery(Class<T> cls, String filter){
        return getPersistenceManager().newQuery(cls, filter);
    }

    // -- TX SHURTCUTS

    /**
     * to tell the underlying object store to start a transaction.
     */
    default void startTransaction() {
        final javax.jdo.Transaction transaction = getPersistenceManager().currentTransaction();
        if (transaction.isActive()) {
            throw new IllegalStateException("Transaction already active");
        }
        transaction.begin();
    }

    /**
     * to tell the underlying object store to commit a transaction.
     */
    default void endTransaction() {
        final javax.jdo.Transaction transaction = getPersistenceManager().currentTransaction();
        if (transaction.isActive()) {
            transaction.commit();
        }
    }

    /**
     * to tell the underlying object store to abort a transaction.
     */
    default void abortTransaction() {
        final javax.jdo.Transaction transaction = getPersistenceManager().currentTransaction();
        if (transaction.isActive()) {
            transaction.rollback();
        }
    }

    /**
     * to tell the underlying object store to flush a transaction.
     */
    default void flushTransaction() {
        final javax.jdo.Transaction transaction = getPersistenceManager().currentTransaction();
        if (transaction.isActive()) {
            transaction.getPersistenceManager().flush();
        }
    }
}
