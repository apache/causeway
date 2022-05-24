package org.apache.isis.sessionlog.applib.contributions;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.sessionlog.applib.IsisModuleExtSessionLogApplib;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntry;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntryRepository;

import lombok.RequiredArgsConstructor;


@Action(
        semantics = SemanticsOf.SAFE,
        domainEvent = HasUsername_recentSessionsForUser.ActionDomainEvent.class
)
@ActionLayout(
        fieldSetId = "username"
)
@RequiredArgsConstructor
public class HasUsername_recentSessionsForUser {

    public static class ActionDomainEvent
            extends IsisModuleExtSessionLogApplib.ActionDomainEvent<HasUsername_recentSessionsForUser> { }

    private final HasUsername hasUsername;

    @MemberSupport public List<SessionLogEntry> act() {
        if(hasUsername == null || hasUsername.getUsername() == null) {
            return Collections.emptyList();
        }
        return sessionLogEntryRepository.findRecentByUsername(hasUsername.getUsername());
    }
    @MemberSupport public boolean hideAct() {
        return hasUsername == null || hasUsername.getUsername() == null;
    }

    @Inject SessionLogEntryRepository sessionLogEntryRepository;

}
