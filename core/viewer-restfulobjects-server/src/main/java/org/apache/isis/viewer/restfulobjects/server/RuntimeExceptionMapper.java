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
package org.apache.isis.viewer.restfulobjects.server;

import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import com.google.common.base.Throwables;
import org.jboss.resteasy.spi.Failure;
import org.apache.isis.core.commons.exceptions.ExceptionUtils;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonMapper;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(final RuntimeException ex) {
        // since have rendered...
        final IsisTransaction currentTransaction = getIsisSessionFactory().getCurrentSession()
                .getPersistenceSession().getTransactionManager().getCurrentTransaction();

        final Throwable rootCause = Throwables.getRootCause(ex);
        final List<Throwable> causalChain = Throwables.getCausalChain(ex);
        for (Throwable throwable : causalChain) {
            if(throwable == rootCause) {
                currentTransaction.clearAbortCause();
            }
        }
        HttpStatusCode statusCode = HttpStatusCode.INTERNAL_SERVER_ERROR;
        if(ex instanceof Failure) {
            Failure failure = (Failure) ex;
            statusCode = HttpStatusCode.statusFor(failure.getErrorCode());
        }
        final ResponseBuilder builder = Response.status(statusCode.getJaxrsStatusType()).type(RestfulMediaType.APPLICATION_JSON_ERROR).entity(jsonFor(ex));
        return builder.build();
    }

    static String jsonFor(final Exception ex) {
        try {
            return JsonMapper.instance().write(RuntimeExceptionPojo.create(ex));
        } catch (final Exception e) {
            // fallback
            return "{ \"exception\": \"" + ExceptionUtils.getFullStackTrace(ex) + "\" }";
        }
    }

    IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

}
