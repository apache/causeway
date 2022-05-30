package org.apache.isis.sessionlog.applib.spiimpl;

import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.events.metamodel.MetamodelEvent;
import org.apache.isis.applib.events.metamodel.MetamodelListener;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.session.SessionLogService;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntry;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SessionLogServiceInitializer implements MetamodelListener {

    final SessionLogEntryRepository<? extends SessionLogEntry> sessionLogEntryRepository;
    final InteractionService interactionService;
    final ClockService clockService;
    final IsisConfiguration isisConfiguration;

    @Override
    public void onMetamodelLoaded() {
        if (!isisConfiguration.getExtensions().getSessionLog().isAutoLogoutOnRestart()) {
            return;
        }

        interactionService.runAnonymous(() -> {
            val timestamp = clockService.getClock().nowAsJavaSqlTimestamp();
            sessionLogEntryRepository.logoutAllSessions(timestamp);
        });
    }

}
