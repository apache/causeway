package org.apache.isis.extensions.commandreplay.secondary.job;

import org.quartz.JobExecutionContext;

import org.apache.isis.extensions.commandreplay.secondary.SecondaryStatus;
import org.apache.isis.extensions.quartz.context.JobExecutionData;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
class SecondaryStatusData {

    private static final String KEY_SECONDARY_STATUS = SecondaryStatusData.class.getCanonicalName();

    private final JobExecutionData jobExecutionData;

    SecondaryStatusData(final JobExecutionContext jobExecutionContext) {
        this.jobExecutionData = new JobExecutionData((jobExecutionContext));
    }

    SecondaryStatus getSecondaryStatus() {
        return getSecondaryStatus(SecondaryStatus.UNKNOWN_STATE);
    }

    SecondaryStatus getSecondaryStatus(final SecondaryStatus defaultStatus) {
        val mode = jobExecutionData.getString( KEY_SECONDARY_STATUS, defaultStatus.name());
        return SecondaryStatus.valueOf(mode);
    }

    void setSecondaryStatus(final SecondaryStatus mode) {
        jobExecutionData.setString(KEY_SECONDARY_STATUS, mode.name());
    }

}

