package org.apache.isis.extensions.commandreplay.secondary.analyser;

import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;

public interface CommandReplayAnalyser {

    /**
     *
     * @param commandJdo
     * @return - if not <code>null</code>, indicates the reason that there was an issue replaying the command.
     */
    String analyzeReplay(final CommandJdo commandJdo);

}
