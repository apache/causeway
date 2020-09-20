package org.apache.isis.extensions.commandreplay.secondary.mixins;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.ReplayState;
import org.apache.isis.extensions.commandreplay.secondary.config.SecondaryConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Action(
    semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE,
    domainEvent = CommandJdo_exclude.ActionDomainEvent.class
)
@RequiredArgsConstructor
@Log4j2
public class CommandJdo_exclude {

    public static class ActionDomainEvent
            extends IsisModuleExtCommandLogImpl.ActionDomainEvent<CommandJdo_exclude> { }

    final CommandJdo commandJdo;

    @MemberOrder(name = "executeIn", sequence = "2")
    public CommandJdo act() {
        commandJdo.setReplayState(ReplayState.EXCLUDED);
        return commandJdo;
    }

    public boolean hideAct() {
        return !secondaryConfig.isPresent() || !secondaryConfig.get().isConfigured() ;
    }
    public String disableAct() {
        final boolean notInError =
                commandJdo.getReplayState() == null || !commandJdo.getReplayState().isFailed();
        return notInError
                ? "This command is not in error, so cannot be excluded."
                : null;
    }

    @Inject Optional<SecondaryConfig> secondaryConfig;

}
