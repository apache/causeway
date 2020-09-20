package org.apache.isis.extensions.quartz.context;

import org.quartz.JobExecutionContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Requires that the job is annotated with the {@link org.quartz.PersistJobDataAfterExecution} annotation.
 */
@Log4j2
@RequiredArgsConstructor
public class JobExecutionData {

    private final JobExecutionContext context;

    /**
     * Lookup property from the job detail.
     */
    public String getString(String key, final String defaultValue) {
        try {
            String v = context.getJobDetail().getJobDataMap().getString(key);
            return v != null ? v : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    /**
     * Save key into the job detail obtained from context.
     */
    public void setString(String key, String value) {
        context.getJobDetail().getJobDataMap().put(key, value);
    }

}

