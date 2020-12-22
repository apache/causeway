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
package org.apache.isis.persistence.jdo.integration.persistence;

import java.util.List;

import org.apache.isis.persistence.jdo.integration.persistence.command.PersistenceCommand;

/**
 * 
 * @since 2.0
 */
public interface IsisPersistenceSessionJdo 
extends PersistenceSession {

    void execute(List<PersistenceCommand> persistenceCommandList);
    
    /**
     * to tell the underlying object store to start a transaction.
     */
    void startTransaction();

    /**
     * to tell the underlying object store to commit a transaction.
     */
    void endTransaction();

    /**
     * to tell the underlying object store to abort a transaction.
     */
    void abortTransaction();
    
    /**
     * to tell the underlying object store to flush a transaction.
     */
    void flushTransaction();
    
    /**
     * Not type safe. For type-safe queries use <br/><br/> {@code pm().newNamedQuery(cls, queryName)}
     * @param cls
     * @param queryName
     * @return
     */
    default <T> javax.jdo.Query<T> newJdoNamedQuery(Class<T> cls, String queryName){
        return getJdoPersistenceManager().newNamedQuery(cls, queryName);
    }

    /**
     * Not type safe. For type-safe queries use <br/><br/> {@code pm().newQuery(cls, queryName)}
     * @param cls
     * @return
     */
    default <T> javax.jdo.Query<T> newJdoQuery(Class<T> cls){
        return getJdoPersistenceManager().newQuery(cls);
    }

    /**
     * Not type safe. For type-safe queries use <br/><br/> {@code pm().newQuery(cls, filter)}
     * @param cls
     * @param filter
     * @return
     */
    default <T> javax.jdo.Query<T> newJdoQuery(Class<T> cls, String filter){
        return getJdoPersistenceManager().newQuery(cls, filter);
    }
    
    javax.jdo.PersistenceManager getJdoPersistenceManager();
    
}
