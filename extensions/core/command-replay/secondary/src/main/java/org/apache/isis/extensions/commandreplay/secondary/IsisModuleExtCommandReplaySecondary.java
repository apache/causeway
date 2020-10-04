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


import javax.inject.Inject;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;
import org.apache.isis.extensions.commandreplay.secondary.analyser.CommandReplayAnalyserException;
import org.apache.isis.extensions.commandreplay.secondary.analyser.CommandReplayAnalyserResult;
import org.apache.isis.extensions.commandreplay.secondary.analysis.CommandReplayAnalysisService;
import org.apache.isis.extensions.commandreplay.secondary.clock.TickingClockService;
import org.apache.isis.extensions.commandreplay.secondary.executor.CommandExecutorServiceWithTime;
import org.apache.isis.extensions.commandreplay.secondary.fetch.CommandFetcher;
import org.apache.isis.extensions.commandreplay.secondary.config.SecondaryConfig;
import org.apache.isis.extensions.commandreplay.secondary.job.ReplicateAndReplayJob;
import org.apache.isis.extensions.commandreplay.secondary.mixins.Object_openOnPrimary;
import org.apache.isis.extensions.commandreplay.secondary.ui.CommandReplayOnSecondaryService;
import org.apache.isis.extensions.quartz.IsisModuleExtQuartzImpl;
import org.apache.isis.extensions.quartz.spring.AutowiringSpringBeanJobFactory;

import lombok.val;

@Configuration
@Import({
        // @Configuration's
        IsisModuleExtCommandLogImpl.class,
        IsisModuleExtQuartzImpl.class,

        // @Service's
        CommandExecutorServiceWithTime.class,
        CommandFetcher.class,
        CommandReplayAnalyserResult.class,
        CommandReplayAnalyserException.class,
        CommandReplayAnalysisService.class,
        CommandReplayOnSecondaryService.class,
        TickingClockService.class,

        // @Service's
        SecondaryConfig.class,

        // mixins
        Object_openOnPrimary.class,

})
@Profile("secondary")
public class IsisModuleExtCommandReplaySecondary {

    public abstract static class ActionDomainEvent<S>
            extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> { }

    public abstract static class CollectionDomainEvent<S,T>
            extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S,T> { }

    public abstract static class PropertyDomainEvent<S,T>
            extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S,T> { }

    @Inject ApplicationContext applicationContext;
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
    public SimpleTriggerFactoryBean replicateAndReplayTriggerFactory(@Qualifier("ReplicateAndReplayJob") JobDetail job) {
        val triggerFactory = new SimpleTriggerFactoryBean();
        triggerFactory.setJobDetail(job);
        val config = isisConfiguration.getExtensions().getCommandReplay().getQuartzReplicateAndReplayJob();
        triggerFactory.setRepeatInterval(config.getRepeatInterval());
        triggerFactory.setStartDelay(config.getStartDelay());
        triggerFactory.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        return triggerFactory;
    }

    @Bean(name = "ReplicateAndReplaySbjf")
    public SpringBeanJobFactory springBeanJobFactory() {
        val jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean(name = "ReplicateAndReplaySfb")
    public SchedulerFactoryBean scheduler(
            @Qualifier("ReplicateAndReplayTrigger") final Trigger trigger,
            @Qualifier("ReplicateAndReplayJob") final JobDetail jobDetail,
            @Qualifier("ReplicateAndReplaySbjf") final SpringBeanJobFactory sbjf) {
        val schedulerFactory = new SchedulerFactoryBean();

        schedulerFactory.setJobFactory(sbjf);
        schedulerFactory.setJobDetails(jobDetail);
        schedulerFactory.setTriggers(trigger);

        return schedulerFactory;
    }

    @Bean(name = "ReplicateAndReplayScheduler")
    public Scheduler scheduler(
            @Qualifier("ReplicateAndReplayTrigger") final Trigger trigger,
            @Qualifier("ReplicateAndReplayJob") final JobDetail job,
            @Qualifier("ReplicateAndReplaySfb") final SchedulerFactoryBean factory)
            throws SchedulerException {
        val scheduler = factory.getScheduler();
        scheduler.start();
        return scheduler;
    }

}
