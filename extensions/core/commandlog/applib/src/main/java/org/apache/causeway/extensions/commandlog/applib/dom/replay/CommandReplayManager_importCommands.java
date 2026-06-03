package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.causeway.applib.annotation.*;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.schema.cmd.v2.CommandDto;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandReplayManager_importCommands.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        sequence = "1.1",
        cssClass = "btn-secondary",
        describedAs = "Imports commands from yaml format, then persists them with a replayState of PENDING."
)
@RequiredArgsConstructor
public class CommandReplayManager_importCommands {

    public static class DomainEvent extends CommandReplayManager.ActionDomainEvent<CommandReplayManager_importCommands> {
    }

    private final CommandReplayManager commandReplayManager;

    public CommandReplayManager act(
            @Parameter(fileAccept = ".yml,.yaml") final Blob commandsYaml,
            @ParameterLayout(describedAs = "Change the baseline to the timestamp of the oldest, so that they are listed at top") final boolean moveBaselineToOldest) {
        var yamlDs = commandsYaml.asDataSource();

        final List<CommandDto> commandDtos = CommandDtoUtils.fromYaml(yamlDs);
        commandDtos.forEach(commandLogEntryRepository::saveForReplay);

        return commandDtos.stream()
                .filter(x -> moveBaselineToOldest)
                .map(CommandDto::getTimestamp)
                .map(CommandReplayManager_importCommands::toJavaSqlTimestamp)
                .sorted()
                .findFirst()
                .map(timestamp -> new CommandReplayManager(timestamp, commandReplayManager.replayContext))
                .orElse(commandReplayManager);
    }

    @MemberSupport
    public boolean defaultMoveBaselineToOldest() {
        return true;
    }

    private static Timestamp toJavaSqlTimestamp(XMLGregorianCalendar xgc) {
        if (xgc == null) return null;
        Instant instant = xgc.toGregorianCalendar().toZonedDateTime().toInstant();
        return Timestamp.from(instant);
    }

    @Inject CommandLogEntryRepository commandLogEntryRepository;

}
