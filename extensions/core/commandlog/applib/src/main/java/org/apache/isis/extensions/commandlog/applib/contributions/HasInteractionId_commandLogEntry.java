package org.apache.isis.extensions.commandlog.applib.contributions;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import lombok.RequiredArgsConstructor;

@Property(
        domainEvent = HasInteractionId_commandLogEntry.PropertyDomainEvent.class
)
@RequiredArgsConstructor
public class HasInteractionId_commandLogEntry {

    private final HasInteractionId hasInteractionId;

    public static class PropertyDomainEvent
            extends IsisModuleExtCommandLogApplib.PropertyDomainEvent<HasInteractionId_commandLogEntry, CommandLogEntry> { }


    public CommandLogEntry prop() {
        return queryResultsCache.execute(this::doProp, getClass(), "prop");
    }

    private CommandLogEntry doProp() {
        return commandLogEntryRepository.findByInteractionId(hasInteractionId.getInteractionId()).orElse(null);
    }

    public boolean hideProp() {
        return prop() == hasInteractionId;
    }

    @Inject CommandLogEntryRepository<CommandLogEntry> commandLogEntryRepository;
    @Inject QueryResultsCache queryResultsCache;

}
