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
public class DemoJob implements Job {

    public void execute(final JobExecutionContext context) {

        final AuthenticationSession authSession = newAuthSession(context);
        new DemoIsisInteractionTemplate().execute(authSession, null);

    }

    protected AuthenticationSession newAuthSession(JobExecutionContext context) {
        return new SimpleSession("isisModuleExtQuartzDemoUser", Arrays.asList("isisModuleExtQuartzDemoRole"));
    }

    @Inject IsisConfiguration isisConfiguration;

}
//end::class[]
