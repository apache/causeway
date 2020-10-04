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
package org.apache.isis.extensions.quartz.jobs;


import java.util.Arrays;

import javax.inject.Inject;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.runtime.iactn.template.AbstractIsisInteractionTemplate;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.standard.SimpleSession;

import lombok.extern.log4j.Log4j2;

//tag::class[]
@Log4j2
class DemoIsisInteractionTemplate extends AbstractIsisInteractionTemplate {
    @Override
    protected void doExecuteWithTransaction(Object context) {
        log.debug("Running session via quartz as '{}'", userService.getUser().getName());
    }
    @Inject UserService userService;
}
//end::class[]
