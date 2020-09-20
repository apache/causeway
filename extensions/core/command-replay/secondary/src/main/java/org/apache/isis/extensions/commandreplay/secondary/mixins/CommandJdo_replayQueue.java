package org.apache.isis.extensions.commandreplay.secondary.mixins;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;
import org.apache.isis.extensions.commandreplay.secondary.IsisModuleExtCommandReplaySecondary;
import org.apache.isis.extensions.commandreplay.secondary.config.SecondaryConfig;

import lombok.RequiredArgsConstructor;

@Collection(
    domainEvent = CommandJdo_replayQueue.CollectionDomainEvent.class
)
@CollectionLayout(
    defaultView = "table"
)
@RequiredArgsConstructor
public class CommandJdo_replayQueue {

    public static class CollectionDomainEvent
            extends IsisModuleExtCommandReplaySecondary.CollectionDomainEvent<CommandJdo_replayQueue, CommandJdo> { }

    final CommandJdo commandJdo;

    @MemberOrder(sequence = "100.100")
    public List<CommandJdo> coll() {
        return commandJdoRepository.findReplayedOnSecondary();
    }
    public boolean hideColl() {
        return !secondaryConfig.isConfigured();
    }

    @Inject SecondaryConfig secondaryConfig;
    @Inject CommandJdoRepository commandJdoRepository;

}
