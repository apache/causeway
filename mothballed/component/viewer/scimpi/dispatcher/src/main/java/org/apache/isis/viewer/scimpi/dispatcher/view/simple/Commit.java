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
package org.apache.isis.viewer.scimpi.dispatcher.view.simple;

import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class Commit extends AbstractElementProcessor {

    @Override
    public String getName() {
        return "commit";
    }

    @Override
    public void process(final Request request) {
        // Note - the session will have changed since the earlier call if a user
        // has logged in or out in the action
        // processing above.
        final IsisTransactionManager transactionManager = IsisContext.getPersistenceSession().getTransactionManager();
        if (transactionManager.getTransaction().getState().canCommit()) {
            transactionManager.endTransaction();
            transactionManager.startTransaction();
        }
    }

}
