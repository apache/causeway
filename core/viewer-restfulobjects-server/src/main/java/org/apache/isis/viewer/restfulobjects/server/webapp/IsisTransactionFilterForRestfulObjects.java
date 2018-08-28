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
package org.apache.isis.viewer.restfulobjects.server.webapp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

@WebFilter(servletNames= {"RestfulObjectsRestEasyDispatcher"})
public class IsisTransactionFilterForRestfulObjects implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // no-op if no session available.
        final IsisSessionFactory isisSessionFactory = isisSessionFactoryFrom(request);
        if(!isisSessionFactory.inSession()) {
            chain.doFilter(request, response);
            return;
        }

        final IsisTransactionManager isisTransactionManager = transactionManagerFrom(isisSessionFactory);
        isisTransactionManager.startTransaction();
        try {
            chain.doFilter(request, response);
        } finally {
            final boolean inTransaction = isisSessionFactory.inTransaction();
            if(inTransaction) {
                // user/logout will have invalidated the current transaction and also persistence session.
                try {
                    isisTransactionManager.endTransaction();
                } catch (Exception ex) {
                    // ignore.  Any exceptions will have been mapped into a suitable response already.
                }
            }
        }
    }

    @Override
    public void destroy() {
    }


    // REVIEW: ought to be able to obtain from thread or as a request attribute
    protected IsisSessionFactory isisSessionFactoryFrom(final ServletRequest request) {
        return IsisContext.getSessionFactory();
    }

    protected IsisTransactionManager transactionManagerFrom(final IsisSessionFactory isisSessionFactory) {
        return isisSessionFactory.getCurrentSession().getPersistenceSession().getTransactionManager();
    }

}
