package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.Builder;
import lombok.Singular;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListener;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.common.v2.OidDto;

import org.apache.causeway.schema.common.v2.ValueType;

import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ResultRemappingService {

    final List<CommandReplayMappingListener> commandReplayMappingListeners;

    @Builder
    public ResultRemappingService(
            final @Singular("commandReplayMappingListener") List<CommandReplayMappingListener> commandReplayMappingListeners) {
        this.commandReplayMappingListeners = commandReplayMappingListeners;
    }



    /**
     * Called when replaying, whereby we rewrite any targets and referenced params.
     * @param commandDto
     * @return
     */
    @Programmatic
    public CommandDto remapped(CommandDto commandDto) {
        final CommandDto commandDtoCopy = copyCommandDto(commandDto);
        updateTargetsAndReferenceParametersWithActual(commandDtoCopy);
        return commandDtoCopy;
    }

    private static CommandDto copyCommandDto(final CommandDto commandDto) {
        return Try.call(() -> CommandDtoUtils.dtoMapper().read(CommandDtoUtils.dtoMapper().toString(commandDto)))
                .ifFailureFail()
                .getValue()
                .orElse(commandDto);
    }

    private void updateTargetsAndReferenceParametersWithActual(CommandDto commandDtoCopy) {
        updateTargetsWithActual(commandDtoCopy);
        updateReferenceParametersWithActual(commandDtoCopy);
    }

    private void updateTargetsWithActual(
            final CommandDto commandDto) {
        if(commandDto == null) {
            return;
        }
        Optional.ofNullable(commandDto.getTargets())
                .stream()
                .flatMap(targets -> targets.getOid().stream())
                .forEach(this::updateTargetWithActual);
    }

    private void updateTargetWithActual(final OidDto target) {
        if(target == null) {
            return;
        }
        final Bookmark recordedTarget = Bookmark.forOidDto(target);
        _NullSafe.stream(commandReplayMappingListeners)
                .forEach(listener -> updateTargetWithActual(listener, target, recordedTarget));
    }

    private void updateTargetWithActual(
            final CommandReplayMappingListener listener,
            final OidDto target,
            final Bookmark recordedTarget) {
        try {
            Optional.ofNullable(listener.lookup(recordedTarget))
                    .orElseGet(Optional::empty)
                    .ifPresent(replacement -> ResultRemappingService.copyBookmarkToOidDto(replacement, target));
        } catch (Exception ex) {
            log.warn("Command replay target remapping listener failed", ex);
        }
    }

    private void updateTargetWithActual(final CommandDtoUtils.BookmarkDto target) {
        if(target == null) {
            return;
        }
        final Bookmark recordedTarget = Bookmark.forLogicalTypeNameAndIdentifier(target.getType(), target.getId());
        _NullSafe.stream(commandReplayMappingListeners)
                .forEach(listener -> updateTargetWithActual(listener, target, recordedTarget));
    }

    private void updateTargetWithActual(
            final CommandReplayMappingListener listener,
            final CommandDtoUtils.BookmarkDto target,
            final Bookmark recordedTarget) {
        try {
            Optional.ofNullable(listener.lookup(recordedTarget))
                    .orElseGet(Optional::empty)
                    .ifPresent(replacement -> ResultRemappingService.copyBookmarkToBookmarkDto(replacement, target));
        } catch (Exception ex) {
            log.warn("Command replay target remapping listener failed", ex);
        }
    }

    /**
     * @param commandDto - updated in-situ
     */
    private void updateReferenceParametersWithActual(final CommandDto commandDto) {
        if (commandDto == null || !(commandDto.getMember() instanceof ActionDto)) {
            return;
        }
        Optional.ofNullable(((ActionDto) commandDto.getMember()).getParameters())
                .stream()
                .flatMap(parameters -> parameters.getParameter().stream())
                .forEach(this::updateReferenceParameterWithActual);
    }

    private void updateReferenceParameterWithActual(final ParamDto parameter) {
        if (parameter == null || parameter.getType() != ValueType.REFERENCE || parameter.getReference() == null) {
            return;
        }
        final Bookmark recordedReference = Bookmark.forOidDto(parameter.getReference());
        _NullSafe.stream(commandReplayMappingListeners)
                .forEach(listener -> ResultRemappingService.updateReferenceParameterWithActual(
                        listener, parameter, recordedReference));
    }

    static void updateReferenceParameterWithActual(
            final CommandReplayMappingListener listener,
            final ParamDto parameter,
            final Bookmark recordedReference) {
        try {
            Optional.ofNullable(listener.lookup(recordedReference))
                    .orElseGet(Optional::empty)
                    .ifPresent(replacement -> copyBookmarkToOidDto(replacement, parameter.getReference()));
        } catch (Exception ex) {
            log.warn("Command replay reference parameter remapping listener failed", ex);
        }
    }

    static void copyBookmarkToOidDto(
            final Bookmark bookmark,
            final OidDto oidDto) {
        if(bookmark == null) {
            return;
        }
        oidDto.setType(bookmark.getLogicalTypeName());
        oidDto.setId(bookmark.getIdentifier());
    }

    static void copyBookmarkToBookmarkDto(
            final Bookmark bookmark,
            final CommandDtoUtils.BookmarkDto bookmarkDto) {
        if(bookmark == null) {
            return;
        }
        bookmarkDto.setType(bookmark.getLogicalTypeName());
        bookmarkDto.setId(bookmark.getIdentifier());
    }



    /**
     * Called when exporting, whereby we rewrite any targets, referenced params and results
     *
     * @param commandExportDto
     * @return
     */
    @Programmatic
    public CommandDtoUtils.CommandExportDto remapped(final CommandDtoUtils.CommandExportDto commandExportDto) {
        final var commandExportDtoCopy = new CommandDtoUtils.CommandExportDto();

        // commandDto
        updateTargetsAndReferenceParametersWithActual(commandExportDtoCopy);
        commandExportDto.setCommand(commandExportDtoCopy.getCommand());

        // result
        final var resultBookmarkDto = commandExportDto.getResult();
        updateTargetWithActual(resultBookmarkDto);
        commandExportDto.setResult(resultBookmarkDto);
        return commandExportDtoCopy;
    }

    private void updateTargetsAndReferenceParametersWithActual(CommandDtoUtils.CommandExportDto commandExportDto) {
        updateTargetsAndReferenceParametersWithActual(commandExportDto.getCommand());
    }


    @Programmatic
    public Optional<Bookmark> findActualBookmark(
            final Bookmark recordedBookmark) {
        return _NullSafe.stream(commandReplayMappingListeners)
                .map(listener -> lookupActualBookmark(listener, recordedBookmark))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<Bookmark> lookupActualBookmark(
            final CommandReplayMappingListener listener,
            final Bookmark recordedBookmark) {
        try {
            return Optional.ofNullable(listener.lookup(recordedBookmark))
                    .orElseGet(Optional::empty);
        } catch (Exception ex) {
            log.warn("Command replay participant mapping listener failed", ex);
            return Optional.empty();
        }
    }


    @Programmatic
    public void notifyReplayResult(Bookmark recordedResult, Bookmark actualResult, UUID interactionId) {
        if (recordedResult == null || actualResult == null) {
            return;
        }
        _NullSafe.stream(commandReplayMappingListeners)
                .forEach(listener -> listener.onReplayResult(recordedResult, actualResult, interactionId ));

    }

}
