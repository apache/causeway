package org.apache.isis.core.metamodel.services.command;

import org.apache.isis.applib.services.command.Command;

import lombok.NonNull;

public interface CommandPublisher {

    /**
     * &quot;Complete&quot; the command, providing an opportunity ot persist
     * a memento of the command if the
     * {@link Command#isSystemStateChanged() system state has changed}.
     *
     * <p>
     *     The framework will automatically have set the {@link Command#getCompletedAt()} property.
     * </p>
     */
    void complete(@NonNull Command command);

}
