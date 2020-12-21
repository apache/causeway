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
package org.apache.isis.core.runtimeservices.xactn;

import java.util.stream.Collectors;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import org.apache.isis.commons.collections.Can;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
final class _GlobalPlatformTransactionManager implements PlatformTransactionManager {

    private final Can<PlatformTransactionManager> localPlatformTransactionManagers;
    
    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        return localPlatformTransactionManagers.getFirstOrFail().getTransaction(definition);
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        localPlatformTransactionManagers.getFirstOrFail().commit(status);
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        localPlatformTransactionManagers.getFirstOrFail().rollback(status);
    }

    @Override
    public String toString() {
        return String.format("local PlatformTransactionManagers [%s]", 
                localPlatformTransactionManagers
                .stream()
                .map(PlatformTransactionManager::getClass)
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", ")));
    }
    
}
