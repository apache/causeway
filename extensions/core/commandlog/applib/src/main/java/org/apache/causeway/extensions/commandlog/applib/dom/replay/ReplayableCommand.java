/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.jaxb.JavaTimeXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.services.command.CommandExecutorService.InteractionContextPolicy;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Refs.ObjectReference;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.commons.io.YamlUtils;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.MemberDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;
import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocBuilder;
import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory;
import org.springframework.transaction.annotation.Propagation;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Viewmodel that wraps a {@link CommandLogEntry}.
 */
@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout//(cssClassFa = "terminal")
@Named(ReplayableCommand.LOGICAL_TYPE_NAME)
@AllArgsConstructor
public final class ReplayableCommand implements ViewModel, Comparable<ReplayableCommand> {

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }

    private final UUID interactionId;
    @Programmatic
    public UUID interactionId() { return interactionId; }

	private final ReplayContext replayContext;
    @Programmatic
    public ReplayContext replayContext() { return replayContext; }


	private final ObjectReference<CommandRecord> recordRef;
    @Programmatic
    public ObjectReference<CommandRecord> recordRef() { return recordRef; }

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".ReplayableCommand";

    // decoupled from the underlying entity
    @Value @Accessors(fluent = true)
    final static class CommandRecord {

	    final CommandDto commandDto;
	    final ReplayState replayState;

        boolean canReplayOrRetryOrMarkForExclusion() {
            return replayState.isPendingOrFailed();
        }
        public String faQuickIcon() {
            switch(replayState) {
                case UNDEFINED: return "solid terminal .col-indigo";
                case EXPORTED:  return "solid terminal .col-indigo, solid circle-arrow-right .ov-size-80 .ov-right-45 .ov-bottom-45 .col-dodgerblue";
                case PENDING:   return "solid terminal .col-indigo, solid circle-pause       .ov-size-80 .ov-right-45 .ov-bottom-45 .col-gold";
                case OK:        return "solid terminal .col-indigo, solid circle-check       .ov-size-80 .ov-right-45 .ov-bottom-45 .col-green";
                case FAILED:    return "solid terminal .col-indigo, solid circle-exclamation .ov-size-80 .ov-right-45 .ov-bottom-45 .col-red";
                case EXCLUDED:  return "solid terminal .col-indigo, solid circle-xmark       .ov-size-80 .ov-right-45 .ov-bottom-45 .col-grey";
            };
			return null;
        }
        //v2 backport, using png screenshots from v4
        public String iconSuffix() {
            switch(replayState) {
                case UNDEFINED: return "";
                case EXPORTED:  return "exported";
                case PENDING:   return "pending";
                case OK:        return "ok";
                case FAILED:    return "failed";
                case EXCLUDED:  return "excluded";
            };
			return null;
        }
    }

    @Inject
    public ReplayableCommand(
            final String memento,
            final ReplayContext replayContext) {
        this(UUID.fromString(memento), replayContext);
    }

    ReplayableCommand(
            final UUID interactionId,
            final ReplayContext replayContext) {
        this(interactionId, replayContext, new ObjectReference<>(null));
    }

    @ObjectSupport public String title() {
        final var timestamp = getTimestampIfAny().map(ChronoZonedDateTime::toInstant).map(Instant::toString).map(x -> " @ " + x).orElse("");
        return getTargetType() + ":" + getTargetId() + " #" + getMember() + timestamp;
    }

//requires v4
//    @ObjectSupport public ObjectSupport.IconResource icon(final ObjectSupport.IconSize iconSize) {
//        return commandRecord()
//                .map(CommandRecord::faQuickIcon)
//                .map(FontAwesomeLayers::fromQuickNotation)
//                .map(ObjectSupport.FontAwesomeIconResource::new)
//                .orElse(null);
//    }
    @ObjectSupport public String iconName() {
    	return commandRecord()
    			.map(CommandRecord::iconSuffix)
    			.orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "1.1",
            fieldSetId = "details",
            describedAs = "UUID of the original (replayable) Command")
    public UUID getInteractionId() {
        return interactionId;
    }

    @Property
    @PropertyLayout(
            sequence = "1.2",
            fieldSetId = "details",
            describedAs = "Timestamp of the original (replayable) Command")
    public ZonedDateTime getTimestamp() {
        return getTimestampIfAny()
            .orElse(null);
    }

    @Programmatic
    public Optional<ZonedDateTime> getTimestampIfAny() {
        return commandRecord()
                .map(CommandRecord::commandDto)
                .map(CommandDto::getTimestamp)
                .map(JavaTimeXMLGregorianCalendarMarshalling::toZonedDateTime);
    }

    @Property
    @PropertyLayout(
            sequence = "2.1",
            fieldSetId = "details",
            describedAs = "Target Type of the original (replayable) Command")
    public String getTargetType() {
        return commandRecord()
            .map(CommandRecord::commandDto)
            .map(commandDto->commandDto.getTargets().getOid().get(0))
            .map(OidDto::getType)
            .orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "2.2",
            fieldSetId = "details",
            describedAs = "Target ID of the original (replayable) Command")
    public String getTargetId() {
        return commandRecord()
            .map(CommandRecord::commandDto)
            .map(commandDto->commandDto.getTargets().getOid().get(0))
            .map(OidDto::getId)
            .map(id->_Strings.ellipsifyAtEnd(id, 10, "..."))
            .orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "3.1",
            fieldSetId = "details",
            describedAs = "Replayable Action or Property, that was executed as captured by the original Command")
    public String getMember() {
        return commandRecord()
            .map(CommandRecord::commandDto)
            .map(CommandDto::getMember)
            .map(MemberDto::getLogicalMemberIdentifier)
            .map(TextUtils::cutter)
            .map(cutter->cutter.keepAfter("#").getValue())
            .orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "4",
            fieldSetId = "details",
            describedAs = "Replay State of the original (replayable) Command. "
                    + "When imported initially is PENDING. "
                    + "Then after replay its either OK or FAILED. "
                    + "Can be manually set to EXCLUDED, which marks it to be ignored for replay.")
    public ReplayState getReplayState() {
        return commandRecord()
            .map(CommandRecord::replayState)
            .orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "9",
            fieldSetId = "dto",
            hidden = Where.ALL_TABLES,
            labelPosition = LabelPosition.NONE,
            describedAs = "DTO of the original (replayable) Command")
    public AsciiDoc getDto() {
        return commandRecord()
            .map(CommandRecord::commandDto)
            .map(commandDto->YamlUtils.toStringUtf8(commandDto,
                JsonUtils::onlyIncludeNonNull))
            .map(yaml->new AsciiDocBuilder()
                    .append(doc->AsciiDocFactory.sourceBlock(doc, "yaml", yaml))
                    .buildAsValue())
            .orElseGet(()->new AsciiDoc("empty"));
    }

    // -- ACTIONS


    ReplayableCommand makeExportable() {
        if(disableMakeExportable()!=null) {
            return this; // safeguard when called programmatically
        }
        commandLogEntry()
                .filter(commandLogEntry->ReplayState.isExported(commandLogEntry.getReplayState()))
                .ifPresent(commandLogEntry->{
                    commandLogEntry.setReplayState(ReplayState.UNDEFINED);
                    invalidateCachedRecord();
                });
        return this;
    }
    String disableMakeExportable() {
        return commandRecord()
                .map(rec->ReplayState.isExported(rec.replayState()))
                .orElse(false)
                ? null
                : "Cannot make exportable, if not EXPORTED";
    }


    ReplayableCommand excludeFromReplay() {
        if(disableExcludeFromReplay()!=null) {
            return ReplayableCommand.this; // safeguard when called programmatically
        }
        commandLogEntry()
                .filter(ReplayableCommand::canReplayOrRetryOrMarkForExclusion)
                .ifPresent(commandLogEntry->{
                    commandLogEntry.setReplayState(ReplayState.EXCLUDED);
                    invalidateCachedRecord();
                });
        return ReplayableCommand.this;
    }
    String disableExcludeFromReplay() {
        return commandRecord()
                .map(CommandRecord::canReplayOrRetryOrMarkForExclusion)
                .orElse(false)
                ? null
                : "Cannot mark for exclusion, if neither PENDING nor FAILED";
    }


    @Programmatic
    void deleteObj() {
        commandLogEntry()
                .ifPresent(commandLogEntry->{
                    replayContext.repositoryService().remove(commandLogEntry);
                    invalidateCachedRecord();
                });
    }


    // -- EXECUTION ORDER GOVERNED BY TIMESTAMP

    private static final Comparator<ReplayableCommand> TIMESTAMP_COMPARATOR =
            Comparator.nullsLast(Comparator.comparing(ReplayableCommand::getTimestamp));

    @Override
    public int compareTo(final ReplayableCommand other) {
        return TIMESTAMP_COMPARATOR.compare(this, other);
    }

    // -- VM STATE

    @Override
    public String viewModelMemento() {
        return interactionId.toString();
    }

    // -- UTIL

    Try<ReplayableCommand> tryReplayOrRetry() {
        if(disableReplayOrRetry()!=null) {
            return Try.success(null); // guard against disallowed invocation
        }
        return commandLogEntry()
            .filter(ReplayableCommand::canReplayOrRetryOrMarkForExclusion)
            .map(commandLogEntry->{
                final var commandDto = commandLogEntry.getCommandDto();
                final var tryResultBookmark = replayContext.transactionService().callTransactional(Propagation.REQUIRES_NEW,
                        () -> {
                            final var bookmarkTry = replayContext.commandExecutorService().executeCommand(
                                    InteractionContextPolicy.SWITCH_USER_AND_TIME,
                                    commandDto);
                            return bookmarkTry.valueAsNullableElseFail();
                        });

                // handle the replay outcome
                tryResultBookmark.accept(
                        ex->onReplayError(commandLogEntry.getInteractionId(), ex),
                        bookmarkOpt->commandLogEntry.setReplayState(ReplayState.OK));

                invalidateCachedRecord();

                return tryResultBookmark
                        .mapSuccessAsNullable(__ -> this);
            })
            .orElseGet(()->Try.success(null));
    }
    String disableReplayOrRetry() {
        return commandRecord()
                .map(CommandRecord::canReplayOrRetryOrMarkForExclusion)
                .orElse(false)
                ? null
                : "Cannot replay, if neither PENDING nor FAILED";
    }

    // -- HELPER

    private void invalidateCachedRecord() {
        recordRef.update(__->null); // invalidate cache
    }

    private Optional<CommandRecord> commandRecord() {
        return Optional.ofNullable(recordRef.computeIfAbsent(()->
            commandLogEntry()
                .filter(commandLogEntry->commandLogEntry.getCommandDto()!=null)
                .filter(commandLogEntry->commandLogEntry.getReplayState()!=null)
                .map(commandLogEntry->new CommandRecord(
                        commandLogEntry.getCommandDto(),
                        commandLogEntry.getReplayState()))
                .orElse(null)));
    }

    Optional<CommandLogEntry> commandLogEntry() {
        return replayContext.commandLogEntryRepository().findByInteractionId(interactionId());
    }

    private static boolean canReplayOrRetryOrMarkForExclusion(final CommandLogEntry commandLogEntry) {
        return ReplayState.isPendingOrFailed(commandLogEntry.getReplayState());
    }

    private void onReplayError(final UUID interactionId, final Throwable ex) {
        Executors.newSingleThreadExecutor()
            .submit(()->replayContext.interactionService().runAnonymous(()->
                replayContext.commandLogEntryRepository().findByInteractionId(interactionId)
                    .ifPresent(entry->entry.saveAnalysis(ex.toString()))));
    }
}