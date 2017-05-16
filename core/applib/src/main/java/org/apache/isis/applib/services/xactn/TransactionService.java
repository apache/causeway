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

package org.apache.isis.applib.services.xactn;

import org.apache.isis.applib.annotation.Programmatic;

public interface TransactionService {

    /**
     * Flush all changes to the object store.
     *
     * <p>
     * Occasionally useful to ensure that newly persisted domain objects
     * are flushed to the database prior to a subsequent repository query.
     * </p>
     *
     * <p>
     *     Equivalent to {@link Transaction#flush()} (with {@link Transaction} obtained using {@link #currentTransaction()}).
     * </p>
     */
    @Programmatic
    void flushTransaction();

    /**
     * Intended only for use by fixture scripts and integration tests; commits this transaction and starts a new one.
     */
    @Programmatic
    void nextTransaction();

    /**
     * Returns a representation of the current transaction.
     */
    @Programmatic
    Transaction currentTransaction();

}
