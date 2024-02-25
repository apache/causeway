/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.causeway.core.transaction.scope;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;

import org.apache.causeway.applib.annotation.TransactionScope;

/**
 * This service, which does nothing in and of itself, exists in order to ensure that the {@link StackedTransactionScope}
 * is always initialized, findinag at least one {@link TransactionScope transaction-scope}d service.
 */
@Service
@TransactionScope
public class NoopTransactionSynchronizationService implements TransactionSynchronization {
}
