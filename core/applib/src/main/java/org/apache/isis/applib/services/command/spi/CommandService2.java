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
package org.apache.isis.applib.services.command.spi;

import java.util.List;
import java.util.UUID;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.command.Command;

public interface CommandService2 extends CommandService {

    public static class NotFoundException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final String transactionId;

        public NotFoundException(final String transactionId) {
            this.transactionId = transactionId;
        }

        public String getTransactionId() {
            return transactionId;
        }
    }

    /**
     * Finds all {@link Command}s created since the specified command (identified by its transaction Id).
     *
     * @param transactionId
     * @param maxNumber
     * @return
     */
    @Programmatic
    List<Command> findSince(
            final UUID transactionId,
            int maxNumber)
            throws NotFoundException;

}
