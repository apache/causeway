package org.apache.isis.core.runtime.headless.logging;

import org.slf4j.Logger;
import org.slf4j.event.Level;

public class LeveledLogger {

    private Logger logger;
    private Level level;

    public LeveledLogger(final Logger logger, final Level level) {
        this.logger = logger;
        this.level = level;
    }

    public void log(final String message) {
        switch (level) {
            case ERROR:
                logger.error(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case TRACE:
                logger.trace(message);
                break;
        }
    }

}
