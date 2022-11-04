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
package org.apache.causeway.extensions.commandreplay.secondary.job;

import javax.inject.Inject;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.extensions.commandreplay.secondary.config.SecondaryConfig;
import org.apache.causeway.extensions.commandreplay.secondary.jobcallables.ReplicateAndRunCommands;
import org.apache.causeway.extensions.commandreplay.secondary.status.SecondaryStatus;
import org.springframework.stereotype.Component;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Component
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Log4j2
public class ReplicateAndReplayJob implements Job {

    @Inject SecondaryConfig secondaryConfig;

    InteractionContext authentication;

    @Override
    public void execute(final JobExecutionContext quartzContext) {

        // figure out if this instance is configured to run as primary or secondary
        new SecondaryStatusData(quartzContext);

        if(secondaryConfig.isConfigured()) {
            val user = UserMemento.ofNameAndRoleNames(
                    secondaryConfig.getPrimaryUser(),
                    secondaryConfig.getQuartzRoles().stream());

            authentication = InteractionContext.ofUserWithSystemDefaults(user);
            exec(quartzContext);
        }
    }

    @Inject protected InteractionService interactionService;

    private void exec(final JobExecutionContext quartzContext) {

        val ssh = new SecondaryStatusData(quartzContext);
        val secondaryStatus = ssh.getSecondaryStatus(SecondaryStatus.OK);

        switch (secondaryStatus) {
            case OK:
                val newStatus =
                        interactionService.call(authentication, new ReplicateAndRunCommands());

                if(newStatus != null) {
                    ssh.setSecondaryStatus(newStatus);
                }
                return;

            case REST_CALL_FAILING:
            case FAILED_TO_UNMARSHALL_RESPONSE:
            case UNKNOWN_STATE:
                log.warn("skipped - configured as secondary, however: {}" , secondaryStatus);
                return;
            default:
                throw new IllegalStateException("Unrecognised status: " + secondaryStatus);
        }
    }


}

