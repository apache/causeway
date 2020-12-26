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

import org.apache.isis.applib.services.xactn.TransactionalProcessor;
import org.apache.isis.core.interaction.session.InteractionSession;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.persistence.jdo.provider.persistence.HasPersistenceManager;

public interface JdoPersistenceSession 
extends 
    HasMetaModelContext,
    HasPersistenceManager {

    /**
     * Binds this {@link JdoPersistenceSession} to the current {@link InteractionSession}.
     */
    void open();
    
    /**
     * Commits the current transaction and unbinds this 
     * {@link JdoPersistenceSession} from the current {@link InteractionSession}.
     */
    void close();
    
    TransactionalProcessor getTransactionalProcessor();

}
