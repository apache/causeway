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

package org.apache.isis.core.runtime.persistence.objectstore.transaction;

import java.util.List;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

/**
 * Interface for the {@link IsisTransactionManager} to interact with some
 * transactional resource (ie a {@link org.apache.isis.core.runtime.system.persistence.ObjectStore}).
 */
public interface TransactionalResource {

    /**
     * Used by the {@link IsisTransactionManager} to tell the underlying
     * {@link org.apache.isis.core.runtime.system.persistence.ObjectStore} to start a transaction.
     */
    void startTransaction();

    /**
     * Used by the current {@link IsisTransaction} to flush changes to
     * the {@link org.apache.isis.core.runtime.system.persistence.ObjectStore} (either via a
     * {@link IsisTransactionManager#flushTransaction()} or a
     * {@link IsisTransactionManager#endTransaction()}).
     */
    void execute(List<PersistenceCommand> unmodifiableList);

    /**
     * Used by the {@link IsisTransactionManager} to tell the underlying
     * {@link org.apache.isis.core.runtime.system.persistence.ObjectStore} to commit a transaction.
     */
    void endTransaction();

    /**
     * Used by the {@link IsisTransactionManager} to tell the underlying
     * {@link org.apache.isis.core.runtime.system.persistence.ObjectStore} to abort a transaction.
     */
    void abortTransaction();

}
