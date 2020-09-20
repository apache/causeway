package org.apache.isis.extensions.commandreplay.secondary.analyser;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import com.google.common.base.Objects;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.commanddto.conmap.UserDataKeys;
import org.apache.isis.applib.util.schema.CommandDtoUtils;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isisExtensionsCommandReplaySecondary.CommandReplayAnalyserResult")
@Order(OrderPrecedence.MIDPOINT)
@RequiredArgsConstructor
public class CommandReplayAnalyserResult implements CommandReplayAnalyser {

    private final IsisConfiguration isisConfiguration;
    private boolean enabled;

    @PostConstruct
    public void init() {
        enabled = isisConfiguration.getExtensions().getCommandReplay().getAnalyser().getException().isEnabled();
    }

    @Override
    public String analyzeReplay(final CommandJdo commandJdo) {
        if(!enabled) {
            return null;
        }

        final CommandDto dto = commandJdo.getCommandDto();

        // see if the outcome was the same...
        // ... either the same result when replayed
        val primaryResultStr = CommandDtoUtils.getUserData(dto, UserDataKeys.RESULT);

        val secondaryResult = commandJdo.getResult();
        val secondaryResultStr =
                secondaryResult != null ? secondaryResult.toString() : null;
        return Objects.equal(primaryResultStr, secondaryResultStr)
                ? null
                : String.format(
                        "Results differ.  Primary was '%s', secondary is '%s'",
                        primaryResultStr, secondaryResultStr);
    }

}
