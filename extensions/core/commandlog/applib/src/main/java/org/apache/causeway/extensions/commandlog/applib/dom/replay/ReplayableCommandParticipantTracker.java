package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.scratchpad.Scratchpad;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

/**
 * Keeps track of the {@link ReplayableCommandParticipant}s that have been encountered from the baseline.
 *
 * <p>
 *     The {@link ReplayableCommand} uses this to determine if it is {@link ReplayableCommand#isKnownParticipants() exportable/replayable}.
 * </p>
 */
public interface ReplayableCommandParticipantTracker {

    String SCRATCHPAD_KEY = CommandManager.LOGICAL_TYPE_NAME + "#current";

    static void putTrackerOnScratchpad(ReplayableCommandParticipantTracker value, Scratchpad scratchpad) {
        if (scratchpad != null) {
            scratchpad.put(SCRATCHPAD_KEY, value);
        }
    }

    static Optional<ReplayableCommandParticipantTracker> current(final Scratchpad scratchpad) {
        return Optional.ofNullable(scratchpad)
                .map(sp -> sp.get(SCRATCHPAD_KEY))
                .filter(ReplayableCommandParticipantTracker.class::isInstance)
                .map(ReplayableCommandParticipantTracker.class::cast);
    }

    @Programmatic
    boolean isKnownParticipants(final CommandLogEntry commandLogEntry);


}
