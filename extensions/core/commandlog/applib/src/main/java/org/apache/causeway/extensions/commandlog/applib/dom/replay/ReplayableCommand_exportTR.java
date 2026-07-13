package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.value.Clob;

import lombok.RequiredArgsConstructor;

@Action(
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = ReplayableCommand_export.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        named = "Export",
        cssClassFa = "solid share-from-square",
        cssClass = "btn-primary",
        describedAs = "Exports the commands, optionally applying result remappings.",
        hidden = Where.OBJECT_FORMS
)
@RequiredArgsConstructor
public class ReplayableCommand_exportTR {

    public static class DomainEvent extends ReplayableCommand.ActionDomainEvent<ReplayableCommand_export> {
    }

    private final ReplayableCommand replayableCommand;

    @MemberSupport
    public Clob act(
            @ParameterLayout(describedAs = "File name for the exported file.") final String filenamePrefix,
            @ParameterLayout(describedAs = "Whether to add a timestamp suffix to the exported file's name.") final boolean filenameTimestamp,
            @ParameterLayout(describedAs = "Whether to remap recording results with actuals.") final boolean remapResults) {

        return mixin().act(filenamePrefix, filenameTimestamp, remapResults);
    }

    @MemberSupport
    public String disableAct() {
        return mixin().disableAct();
    }

    @MemberSupport
    public String defaultFilenamePrefix() {
        return mixin().defaultFilenamePrefix();
    }

    @MemberSupport
    public boolean defaultFilenameTimestamp() {
        return mixin().defaultFilenameTimestamp();
    }

    @MemberSupport
    public boolean defaultRemapResults() {
        return mixin().defaultRemapResults();
    }

    private ReplayableCommand_export mixin() {
        return factoryService.mixin(ReplayableCommand_export.class, replayableCommand);
    }

    @Inject private FactoryService factoryService;

}
