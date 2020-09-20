package org.apache.isis.extensions.commandreplay.primary.mixins;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandreplay.primary.IsisModuleExtCommandReplayPrimary;
import org.apache.isis.extensions.commandreplay.primary.ui.CommandReplayOnPrimaryService;


import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.NON_IDEMPOTENT,
    domainEvent = CommandJdo_download.ActionDomainEvent.class
)
@ActionLayout(
    cssClassFa = "fa-download",
    position = ActionLayout.Position.PANEL
)
@RequiredArgsConstructor
public class CommandJdo_download {

    public static class ActionDomainEvent
            extends IsisModuleExtCommandReplayPrimary.ActionDomainEvent<CommandJdo_download> { }

    private final CommandJdo commandJdo;

    @MemberOrder(name = "arguments", sequence = "1")
    public Clob act(
            @ParameterLayout(named="Filename prefix")
            final String fileNamePrefix) {
        return commandReplayOnPrimaryService.downloadCommandById(commandJdo.getUniqueId(), fileNamePrefix);
    }
    public String default0Act() {
        return "command";
    }

    @Inject
    CommandReplayOnPrimaryService commandReplayOnPrimaryService;


}
