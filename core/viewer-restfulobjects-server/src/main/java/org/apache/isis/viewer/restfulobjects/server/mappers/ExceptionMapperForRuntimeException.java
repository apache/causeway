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
package org.apache.isis.viewer.restfulobjects.server.mappers;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

@Provider
public class ExceptionMapperForRuntimeException extends ExceptionMapperAbstract<RuntimeException> {

    @Override
    public Response toResponse(final RuntimeException ex) {

        final Throwable rootCause = _Exceptions.getRootCause(ex);
        final List<Throwable> causalChain = _Exceptions.getCausalChain(ex);
        for (Throwable throwable : causalChain) {
            if(throwable == rootCause) {
                
                // since already rendered...
                getCurrentTransaction()
                    .ifPresent(IsisTransaction::clearAbortCause);
            }
        }

        return buildResponse(ex);
    }

    // -- HELPER
    
    private Optional<IsisTransaction> getCurrentTransaction() {
        return getCurrentSession()
                .map(IsisSession::getPersistenceSession)
                .map(PersistenceSession::getTransactionManager)
                .map(IsisTransactionManager::getCurrentTransaction);
    }
    
    private Optional<IsisSession> getCurrentSession() {
        return getIsisSessionFactory()
                .map(IsisSessionFactory::getCurrentSession);
    }
    
    private Optional<IsisSessionFactory> getIsisSessionFactory() {
        try {
            return Optional.ofNullable(IsisContext.getSessionFactory());    
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

}
