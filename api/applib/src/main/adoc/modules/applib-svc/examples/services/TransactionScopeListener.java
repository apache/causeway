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

package org.apache.isis.applib.services;

import org.apache.isis.applib.annotation.IsisInteractionScope;

/**
 * Domain services that need to be aware of transaction boundaries can
 * implement this interface.
 * 
 * @apiNote Implementing services most likely need to be scoped in a way that
 * binds the scope to the current thread (eg. {@link IsisInteractionScope})
 *  
 * @since 2.0 (renamed from WithTransactionScope)
 */
// tag::refguide[]
public interface TransactionScopeListener {
    
    default void onTransactionStarted() {
        // default: do nothing
    }
    
    void onTransactionEnded();
}
// end::refguide[]
