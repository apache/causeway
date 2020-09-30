package org.apache.isis.extensions.commandreplay.primary.config;

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
@Named("isisExtensionsCommandReplayPrimary.PrimaryConfig")
@Order(OrderPrecedence.MIDPOINT)
@Log4j2
public class PrimaryConfig {

    @Getter final String secondaryBaseUrlWicket;

    public PrimaryConfig(@NotNull final IsisConfiguration isisConfiguration) {
        val config = isisConfiguration.getExtensions().getCommandReplay();

        val secondaryAccess = config.getSecondaryAccess();
        secondaryBaseUrlWicket = secondaryAccess.getBaseUrlWicket().orElse(null);
    }

    public boolean isConfigured() {
        return secondaryBaseUrlWicket != null;
    }
}
