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
