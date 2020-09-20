package org.apache.isis.extensions.commandreplay.secondary.config;

import java.util.List;

import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isisExtensionsCommandReplaySecondary.SecondaryConfig")
@Order(OrderPrecedence.MIDPOINT)
@Log4j2
public class SecondaryConfig {

    @Getter final String primaryUser;
    @Getter final String primaryPassword;
    @Getter final String primaryBaseUrlRestful;
    @Getter final String primaryBaseUrlWicket;
    @Getter final int batchSize;

    @Getter final String quartzUser;
    @Getter final List<String> quartzRoles;

    public SecondaryConfig(@NotNull final IsisConfiguration isisConfiguration) {
        val config = isisConfiguration.getExtensions().getCommandReplay();

        val primaryAccess = config.getPrimaryAccess();
        primaryUser = primaryAccess.getUser().orElse(null);
        primaryPassword = primaryAccess.getPassword().orElse(null);
        primaryBaseUrlRestful = primaryAccess.getBaseUrlRestful().orElse(null);
        primaryBaseUrlWicket = primaryAccess.getBaseUrlWicket().orElse(null);
        batchSize = config.getBatchSize();

        quartzUser = config.getQuartzSession().getUser();
        quartzRoles = config.getQuartzSession().getRoles();
    }

    public boolean isConfigured() {
        return primaryUser != null &&
               primaryPassword != null &&
               primaryBaseUrlRestful != null &&
               quartzUser != null &&
               quartzRoles != null;
    }
}
