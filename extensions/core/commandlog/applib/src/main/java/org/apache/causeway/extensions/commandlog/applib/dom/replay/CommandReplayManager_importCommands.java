package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import java.util.List;

import org.apache.causeway.applib.annotation.*;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.schema.cmd.v2.CommandDto;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        sequence = "1.1",
        cssClass = "btn-secondary",
        describedAs = "Imports commands from yaml format, then persists them with a replayState of PENDING."
)
public class CommandReplayManager_importCommands {
    private final CommandReplayManager commandReplayManager;

    public CommandReplayManager_importCommands(CommandReplayManager commandReplayManager) {
        this.commandReplayManager = commandReplayManager;
    }

    public class DomainEvent extends CommandReplayManager.ActionDomainEvent<CommandReplayManager_importCommands> {
    }

    public CommandReplayManager act(
            @Parameter(fileAccept = ".yml,.yaml") final Blob commandsYaml,
            @ParameterLayout(describedAs = "Change the baseline to the timestamp of the oldest, so that they are listed at top") final boolean moveBaselineToOldest) {
        var yamlDs = commandsYaml.asDataSource();

        final List<CommandDto> commandDtos = CommandDtoUtils.fromYaml(yamlDs);
        commandDtos.forEach(commandReplayManager.commandLogEntryRepository()::saveForReplay);

        return commandDtos.stream()
                .filter(x -> moveBaselineToOldest)
                .map(CommandDto::getTimestamp)
                .map(CommandReplayManager::toJavaSqlTimestamp)
                .sorted()
                .findFirst()
                .map(timestamp -> new CommandReplayManager(timestamp, commandReplayManager.replayContext))
                .orElse(commandReplayManager);
    }

    @MemberSupport
    public boolean defaultMoveBaselineToOldest() {
        return true;
    }

}
