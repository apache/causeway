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
package demoapp.web.replay;

import javax.inject.Inject;

import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import org.apache.isis.extensions.quartz.jobs.DemoJob;
import org.apache.isis.extensions.quartz.spring.AutowiringSpringBeanJobFactory;

import lombok.val;
import lombok.extern.log4j.Log4j2;

//tag::class[]
@Configuration
@Log4j2
public class DemoJobQuartzConfigurerModule {

    @Bean
    public JobDetailFactoryBean jobDetail() {
        val jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(DemoJob.class);
        jobDetailFactory.setDescription("Run demo job");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    @Bean
    public SimpleTriggerFactoryBean trigger(JobDetail job) {
        val triggerFactory = new SimpleTriggerFactoryBean();
        triggerFactory.setJobDetail(job);
        triggerFactory.setRepeatInterval(10000); // 10seconds
        triggerFactory.setStartDelay(15000);     // 15 seconds approx boot up time
        triggerFactory.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        return triggerFactory;
    }

    @Bean
    public SchedulerFactoryBean scheduler(Trigger trigger, JobDetail job, SpringBeanJobFactory springBeanJobFactory) {
        val schedulerFactory = new SchedulerFactoryBean();
        // schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));

        schedulerFactory.setJobFactory(springBeanJobFactory);
        schedulerFactory.setJobDetails(job);

        schedulerFactory.setTriggers(trigger);

        return schedulerFactory;
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        val jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Inject ApplicationContext applicationContext;
}
//end::class[]

