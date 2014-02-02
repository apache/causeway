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
package webapp.scheduler;

import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosure;

public abstract class AbstractIsisJob implements Job {

    private boolean executing;

    /**
     * Sets up an {@link IsisSession} then delegates to the {@link #doExecute(JobExecutionContext) hook}. 
     */
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        final AuthenticationSession authSession = newAuthSession(context);
        try {
            if(executing) {
                return;
            }
            executing = true;

            IsisContext.openSession(authSession);
            PersistenceSession persistenceSession = IsisContext.getPersistenceSession();
            persistenceSession.getServicesInjector().injectServicesInto(this);
            IsisTransactionManager transactionManager = persistenceSession.getTransactionManager();
            transactionManager.executeWithinTransaction(new TransactionalClosure() {
                
                @Override
                public void preExecute() {
                }
                
                @Override
                public void execute() {
                    doExecute(context);
                }
                
                @Override
                public void onSuccess() {
                }
                
                @Override
                public void onFailure() {
                }
            });
        } finally {
            executing = false;
            IsisContext.closeSession();
        }
    }

    AuthenticationSession newAuthSession(JobExecutionContext context) {
        String user = getKey(context, SchedulerConstants.USER_KEY);
        String rolesStr = getKey(context, SchedulerConstants.ROLES_KEY);
        String[] roles = Iterables.toArray(
                Splitter.on(",").split(rolesStr), String.class);
        return new SimpleSession(user, roles);
    }

    
    /**
     * Mandatory hook.
     */
    protected abstract void doExecute(JobExecutionContext context);

    /**
     * Helper method for benefit of subclasses
     */
    protected String getKey(JobExecutionContext context, String key) {
        return context.getMergedJobDataMap().getString(key);
    }

    /**
     * Helper method for benefit of subclasses
     */
    protected <T> T getService(Class<T> cls) {
        List<Object> services = IsisContext.getServices();
        for (Object service : services) {
            if(cls.isAssignableFrom(service.getClass())) {
                return asT(service);
            }
        }
        throw new IllegalArgumentException("No service of type '" + cls.getName() + "' was found");
    }

    @SuppressWarnings("unchecked")
    private static <T> T asT(Object service) {
        return (T) service;
    }

}
