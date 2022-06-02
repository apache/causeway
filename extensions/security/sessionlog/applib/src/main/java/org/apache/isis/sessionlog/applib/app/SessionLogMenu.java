package org.apache.isis.sessionlog.applib.app;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.sessionlog.applib.IsisModuleExtSessionLogApplib;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntry;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntryRepository;


/**
 * This service exposes a &lt;Sessions&gt; menu to the secondary menu bar for searching for sessions.
 */
@DomainService(nature = NatureOfService.VIEW)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        named = "Activity"
)
@Named("isissessionlogger.SessionLoggingServiceMenu")
public class SessionLogMenu {

    public static abstract class ActionDomainEvent<T> extends IsisModuleExtSessionLogApplib.ActionDomainEvent<T> { }

    @Action(
            domainEvent = activeSessions.ActiveEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            bookmarking = BookmarkPolicy.AS_ROOT,
            cssClassFa = "fa-bolt"
    )
    public class activeSessions {

        public class ActiveEvent extends ActionDomainEvent<activeSessions> { }

        @MemberSupport public List<SessionLogEntry> act() {
            return sessionLogEntryRepository.findActiveSessions();
        }
    }



    public class findSessions {
        public class ActionEvent extends ActionDomainEvent<findSessions> { }

        @Action(
                domainEvent = ActionEvent.class,
                semantics = SemanticsOf.SAFE
        )
        @ActionLayout(
                cssClassFa = "fa-search"
        )
        public List<SessionLogEntry> act(
                final @Nullable String user,
                final @Nullable LocalDate from,
                final @Nullable LocalDate to) {

            if(user == null) {
                return sessionLogEntryRepository.findByFromAndTo(from, to);
            } else {
                return sessionLogEntryRepository.findByUsernameAndFromAndTo(user, from, to);
            }
        }
    }


    @Inject SessionLogEntryRepository sessionLogEntryRepository;

}
