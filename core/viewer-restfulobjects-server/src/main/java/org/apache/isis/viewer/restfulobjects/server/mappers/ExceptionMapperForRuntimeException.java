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

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.google.common.base.Throwables;

import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;

@Provider
public class ExceptionMapperForRuntimeException extends ExceptionMapperAbstract<RuntimeException> {

    @Override
    public Response toResponse(final RuntimeException ex) {

        final Throwable rootCause = Throwables.getRootCause(ex);
        final List<Throwable> causalChain = Throwables.getCausalChain(ex);
        for (Throwable throwable : causalChain) {
            if(throwable == rootCause) {
                // since already rendered...
                final IsisSession currentSession = getIsisSessionFactory().getCurrentSession();
                if(currentSession != null) {
                    final IsisTransaction currentTransaction = currentSession
                            .getPersistenceSession().getTransactionManager().getCurrentTransaction();
                    currentTransaction.clearAbortCause();
                }
            }
        }

        return buildResponse(ex);
    }


    private IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

}
