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

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;
import org.apache.isis.core.runtime.sessiontemplate.AbstractIsisSessionTemplate;
import org.apache.isis.core.runtime.system.session.IsisSession;

public class AbstractIsisQuartzJob implements Job {

    public static enum ConcurrentInstancesPolicy {
        /**
         * Only a single instance of this job is allowed to run.  
         * 
         * <p>
         * That is, if the job is invoked again before a previous instance has completed, then silently skips.
         */
        SINGLE_INSTANCE_ONLY,
        /**
         * Multiple instances of this job are allowed to run concurrently.
         * 
         * <p>
         * That is, it is not required for the previous instance of this job to have completed before this one starts.
         */
        MULTIPLE_INSTANCES
    }
    
    private final AbstractIsisSessionTemplate isisRunnable;

    private final ConcurrentInstancesPolicy concurrentInstancesPolicy;
    private boolean executing;

    public AbstractIsisQuartzJob(AbstractIsisSessionTemplate isisRunnable) {
        this(isisRunnable, ConcurrentInstancesPolicy.SINGLE_INSTANCE_ONLY);
    }
    public AbstractIsisQuartzJob(AbstractIsisSessionTemplate isisRunnable, ConcurrentInstancesPolicy concurrentInstancesPolicy) {
        this.isisRunnable = isisRunnable;
        this.concurrentInstancesPolicy = concurrentInstancesPolicy;
    }

    // //////////////////////////////////////

    /**
     * Sets up an {@link IsisSession} then delegates to the {@link #doExecute(JobExecutionContext) hook}. 
     */
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        final AuthenticationSession authSession = newAuthSession(context);
        try {
            if(concurrentInstancesPolicy == ConcurrentInstancesPolicy.SINGLE_INSTANCE_ONLY && executing) {
                return;
            }
            executing = true;

            isisRunnable.execute(authSession, context);
        } finally {
            executing = false;
        }
    }

    AuthenticationSession newAuthSession(JobExecutionContext context) {
        String user = getKey(context, SchedulerConstants.USER_KEY);
        String rolesStr = getKey(context, SchedulerConstants.ROLES_KEY);
        String[] roles = Iterables.toArray(
                Splitter.on(",").split(rolesStr), String.class);
        return new SimpleSession(user, roles);
    }

    String getKey(JobExecutionContext context, String key) {
        return context.getMergedJobDataMap().getString(key);
    }
}
