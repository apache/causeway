package org.apache.isis.core.runtime.logging;

import org.apache.logging.log4j.core.LogEvent;

/**
 *
 */
public interface LogEventEvaluator {
    boolean isTriggeringEvent(final LogEvent event);
}
