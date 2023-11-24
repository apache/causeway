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
package org.apache.isis.extensions.commandreplay.secondary;


import lombok.val;

import javax.inject.Inject;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.runtime.IsisModuleCoreRuntime;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandreplay.secondary.analyser.CommandReplayAnalyserException;
import org.apache.isis.extensions.commandreplay.secondary.analyser.CommandReplayAnalyserResult;
import org.apache.isis.extensions.commandreplay.secondary.analysis.CommandReplayAnalysisService;
import org.apache.isis.extensions.commandreplay.secondary.config.SecondaryConfig;
import org.apache.isis.extensions.commandreplay.secondary.fetch.CommandFetcher;
import org.apache.isis.extensions.commandreplay.secondary.job.ReplicateAndReplayJob;
import org.apache.isis.extensions.commandreplay.secondary.mixins.Object_openOnPrimary;
import org.apache.isis.extensions.commandreplay.secondary.ui.CommandReplayOnSecondaryService;
import org.apache.isis.schema.IsisModuleSchema;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * Activates with <i>Spring profile</i> 'commandreplay-secondary'.
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // @Configuration's
        IsisModuleCoreRuntime.class,
        IsisModuleSchema.class,
        IsisModuleTestingFixturesApplib.class,
        IsisModuleExtCommandLogApplib.class,

        // @Component's
        ReplicateAndReplayJob.class,

        // @Service's
        CommandFetcher.class,
        CommandReplayAnalyserResult.class,
        CommandReplayAnalyserException.class,
        CommandReplayAnalysisService.class,
        CommandReplayOnSecondaryService.class,

        // @Service's
        SecondaryConfig.class,

        // Mixins
        Object_openOnPrimary.class,

})
@Profile("commandreplay-secondary")
public class IsisModuleExtCommandReplaySecondary {

    public static final String NAMESPACE = IsisModuleExtCommandLogApplib.NAMESPACE_REPLAY_SECONDARY;

    public abstract static class ActionDomainEvent<S>
            extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> { }

    public abstract static class CollectionDomainEvent<S,T>
            extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S,T> { }

    public abstract static class PropertyDomainEvent<S,T>
            extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S,T> { }

    @Inject IsisConfiguration isisConfiguration;

    @Bean(name = "ReplicateAndReplayJob")
    public JobDetailFactoryBean replicateAndReplayJobDetailFactory() {
        val jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(ReplicateAndReplayJob.class);
        jobDetailFactory.setDescription("Replicate commands from primary and replay on secondary");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    @Bean(name = "ReplicateAndReplayTrigger" )
    public SimpleTriggerFactoryBean replicateAndReplayTriggerFactory(
            final @Qualifier("ReplicateAndReplayJob") JobDetail job) {
        val triggerFactory = new SimpleTriggerFactoryBean();
        triggerFactory.setJobDetail(job);
        val config = isisConfiguration.getExtensions().getCommandReplay().getQuartzReplicateAndReplayJob();
        triggerFactory.setRepeatInterval(config.getRepeatInterval());
        triggerFactory.setStartDelay(config.getStartDelay());
        triggerFactory.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        return triggerFactory;
    }

}
