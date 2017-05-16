/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services;

import java.util.UUID;


/**
 * Mix-in interface for objects (usually created by service implementations) that are be persistable,
 * and so can be associated together using a unique identifier.
 *
 * <p>
 *     Prior to 1.13.0, this identifier was the GUID of the Isis transaction in which the object was created (hence
 *     the name).  As of 1.13.0, this identifier actually is for the request/interaction in which the object was
 *     created, so is misnamed.
 * </p>
 */
public interface HasTransactionId {

    /**
     * The unique identifier (a GUID) of the request/interaction.
     */
    UUID getTransactionId();

    void setTransactionId(final UUID transactionId);

    class TransactionIdType {

        private TransactionIdType() {}

        public static class Meta {

            public static final int MAX_LEN = 36;

            private Meta() {}

        }

    }
}
