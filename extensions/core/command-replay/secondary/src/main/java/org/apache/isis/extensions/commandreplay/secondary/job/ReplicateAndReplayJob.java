package org.apache.isis.extensions.commandreplay.secondary.job;

import javax.inject.Inject;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;

import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.standard.SimpleSession;

import org.apache.isis.extensions.commandreplay.secondary.config.SecondaryConfig;
import org.apache.isis.extensions.commandreplay.secondary.jobcallables.IsTickingClockInitialized;
import org.apache.isis.extensions.commandreplay.secondary.jobcallables.ReplicateAndRunCommands;
import org.apache.isis.extensions.commandreplay.secondary.SecondaryStatus;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Log4j2
public class ReplicateAndReplayJob implements Job {

    @Inject SecondaryConfig secondaryConfig;

    AuthenticationSession authSession;

    public void execute(final JobExecutionContext quartzContext) {

        // figure out if this instance is configured to run as primary or secondary
        new SecondaryStatusData(quartzContext);

        if(secondaryConfig.isConfigured()) {
            authSession = new SimpleSession(secondaryConfig.getPrimaryUser(), secondaryConfig.getQuartzRoles());
            exec(quartzContext);
        }
    }

    @Inject protected IsisInteractionFactory isisInteractionFactory;

    private void exec(final JobExecutionContext quartzContext) {
        val ssh = new SecondaryStatusData(quartzContext);
        val secondaryStatus = ssh.getSecondaryStatus(SecondaryStatus.TICKING_CLOCK_STATUS_UNKNOWN);

        switch (secondaryStatus) {

            case TICKING_CLOCK_STATUS_UNKNOWN:
            case TICKING_CLOCK_NOT_YET_INITIALIZED:
                ssh.setSecondaryStatus(
                        isTickingClockInitialized(authSession)
                            ? SecondaryStatus.OK
                            : SecondaryStatus.TICKING_CLOCK_NOT_YET_INITIALIZED);
                if(ssh.getSecondaryStatus() == SecondaryStatus.OK) {
                    log.info("Ticking clock now initialised");
                } else {
                    log.info("Still waiting for ticking clock to be initialised: {}" , secondaryStatus);
                }
                return;

            case OK:
                val newStatus =
                        isisInteractionFactory.callAuthenticated(authSession, new ReplicateAndRunCommands());

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

    private boolean isTickingClockInitialized(final AuthenticationSession authSession) {
        return isisInteractionFactory.callAuthenticated(authSession, new IsTickingClockInitialized());
    }

}

