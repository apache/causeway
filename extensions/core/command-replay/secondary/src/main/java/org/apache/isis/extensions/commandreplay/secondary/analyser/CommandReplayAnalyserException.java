package org.apache.isis.extensions.commandreplay.secondary.analyser;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import com.google.common.base.Objects;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.commanddto.conmap.UserDataKeys;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isisExtensionsCommandReplaySecondary.CommandReplayAnalyserException")
@Order(OrderPrecedence.MIDPOINT)
@RequiredArgsConstructor
public class CommandReplayAnalyserException implements CommandReplayAnalyser {

    private final IsisConfiguration isisConfiguration;
    private boolean enabled;

    @PostConstruct
    public void init() {
        enabled = isisConfiguration.getExtensions().getCommandReplay().getAnalyser().getResult().isEnabled();
    }

    @Override
    public String analyzeReplay(final CommandJdo commandJdo) {
        if(!enabled) {
            return null;
        }

        val dto = commandJdo.getCommandDto();

        val primaryException = CommandDtoUtils.getUserData(dto, UserDataKeys.EXCEPTION);
        if (_Strings.isNullOrEmpty(primaryException)) {
            return null;
        }

        val replayedException = commandJdo.getException();

        val primaryExceptionTrimmed = trimmed(primaryException);
        val replayedExceptionTrimmed = trimmed(replayedException);
        return Objects.equal(primaryExceptionTrimmed, replayedExceptionTrimmed)
                ? null
                : String.format("Exceptions differ.  On primary system was '%s'", primaryException);
    }

    private String trimmed(final String str) {
        return withoutWhitespace(initialPartOfStackTrace(str));
    }

    // we only look at beginning of the stack trace because the latter part will differ when replayed
    private String initialPartOfStackTrace(final String str) {
        final int toInspectOfStackTrace = 500;
        return str.length() > toInspectOfStackTrace ? str.substring(0, toInspectOfStackTrace) : str;
    }

    private String withoutWhitespace(final String s) {
        return s.replaceAll("\\s", "");
    }

}
