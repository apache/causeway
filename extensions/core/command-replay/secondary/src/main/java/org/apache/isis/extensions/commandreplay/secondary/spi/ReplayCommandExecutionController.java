package org.apache.isis.extensions.commandreplay.secondary.spi;

/**
 * Optional SPI that allows the replicate and replay job to be paused if
 * required.
 *
 * <p>
 * If no implementation is configured, then replication/replay will continue
 * without interruption.
 * </p>
 */
public interface ReplayCommandExecutionController {

    enum State {
        RUNNING,
        PAUSED
    }

    /**
     * The current state, or <tt>null</tt> if the service implementing this SPI has not yet been initialized.
     * @return
     */
    State getState();

}
