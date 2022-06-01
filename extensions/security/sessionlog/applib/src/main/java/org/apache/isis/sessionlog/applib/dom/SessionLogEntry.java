package org.apache.isis.sessionlog.applib.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.session.SessionLogService;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.sessionlog.applib.IsisModuleExtSessionLogApplib;

import lombok.val;
import lombok.experimental.UtilityClass;

@Named(SessionLogEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED
)
@DomainObjectLayout(
        titleUiEvent = SessionLogEntry.TitleUiEvent.class,
        iconUiEvent = SessionLogEntry.IconUiEvent.class,
        cssClassUiEvent = SessionLogEntry.CssClassUiEvent.class,
        layoutUiEvent = SessionLogEntry.LayoutUiEvent.class
)
public abstract class SessionLogEntry implements HasUsername, Comparable<SessionLogEntry> {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtSessionLogApplib.NAMESPACE + ".SessionLogEntry";
    public static final String SCHEMA = IsisModuleExtSessionLogApplib.SCHEMA;
    public static final String TABLE = "SessionLogEntry";

    @UtilityClass
    public static class Nq {
        public static final String FIND_BY_SESSION_GUID_STR =  SessionLogEntry.LOGICAL_TYPE_NAME + ".findBySessionGuidStr";
        public static final String FIND_BY_HTTP_SESSION_ID =  SessionLogEntry.LOGICAL_TYPE_NAME + ".findByHttpSessionId";
        public static final String FIND_BY_USERNAME_AND_TIMESTAMP_BETWEEN = SessionLogEntry.LOGICAL_TYPE_NAME + ".findByUsernameAndTimestampBetween";
        public static final String FIND_BY_USERNAME_AND_TIMESTAMP_AFTER = SessionLogEntry.LOGICAL_TYPE_NAME + ".findByUsernameAndTimestampAfter";
        public static final String FIND_BY_USERNAME_AND_TIMESTAMP_BEFORE = SessionLogEntry.LOGICAL_TYPE_NAME + ".findByUsernameAndTimestampBefore";
        public static final String FIND_BY_USERNAME = SessionLogEntry.LOGICAL_TYPE_NAME + ".findByUsername";
        public static final String FIND_BY_TIMESTAMP_BETWEEN = SessionLogEntry.LOGICAL_TYPE_NAME + ".findByTimestampBetween";
        public static final String FIND_BY_TIMESTAMP_AFTER = SessionLogEntry.LOGICAL_TYPE_NAME + ".findByTimestampAfter";
        public static final String FIND_BY_TIMESTAMP_BEFORE = SessionLogEntry.LOGICAL_TYPE_NAME + ".findByTimestampBefore";
        public static final String FIND = SessionLogEntry.LOGICAL_TYPE_NAME + ".find";
        public static final String FIND_BY_USERNAME_AND_TIMESTAMP_STRICTLY_BEFORE = SessionLogEntry.LOGICAL_TYPE_NAME + ".findByUsernameAndTimestampStrictlyBefore";
        public static final String FIND_BY_USERNAME_AND_TIMESTAMP_STRICTLY_AFTER = SessionLogEntry.LOGICAL_TYPE_NAME + ".findByUsernameAndTimestampStrictlyAfter";
        public static final String FIND_ACTIVE_SESSIONS = SessionLogEntry.LOGICAL_TYPE_NAME + ".findActiveSessions";
        public static final String FIND_RECENT_BY_USERNAME = SessionLogEntry.LOGICAL_TYPE_NAME + ".findRecentByUsername";
    }

    // -- UI & DOMAIN EVENTS

    public static class TitleUiEvent extends IsisModuleExtSessionLogApplib.TitleUiEvent<SessionLogEntry> { }
    public static class IconUiEvent extends IsisModuleExtSessionLogApplib.IconUiEvent<SessionLogEntry> { }
    public static class CssClassUiEvent extends IsisModuleExtSessionLogApplib.CssClassUiEvent<SessionLogEntry> { }
    public static class LayoutUiEvent extends IsisModuleExtSessionLogApplib.LayoutUiEvent<SessionLogEntry> { }

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSessionLogApplib.PropertyDomainEvent<SessionLogEntry, T> { }

    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSessionLogApplib.CollectionDomainEvent<SessionLogEntry, T> { }

    public static abstract class ActionDomainEvent extends IsisModuleExtSessionLogApplib.ActionDomainEvent<SessionLogEntry> { }


    protected SessionLogEntry(
            final UUID sessionGuid,
            final String httpSessionId,
            final String username,
            final SessionLogService.CausedBy causedBy,
            final Timestamp loginTimestamp) {
        setSessionGuidStr(sessionGuid != null ? sessionGuid.toString() : null);
        setHttpSessionId(httpSessionId);
        setUsername(username);
        setCausedBy(causedBy);
        setLoginTimestamp(loginTimestamp);
    }


    public String title() {

        // nb: not thread-safe
        // formats defined in https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return String.format("%s: %s logged %s %s",
                format.format(getLoginTimestamp()),
                getUsername(),
                getLogoutTimestamp() == null ? "in": "out",
                getCausedBy() == SessionLogService.CausedBy.SESSION_EXPIRATION ? "(session expired)" : "");
    }

    public String cssClass() {
        return "sessionLogEntry-" + iconName();
    }

    public String iconName() {
        return getLogoutTimestamp() == null
                ? "login"
                :getCausedBy() != SessionLogService.CausedBy.SESSION_EXPIRATION
                    ? "logout"
                    : "expired";
    }



    @Property(
            hidden = Where.EVERYWHERE
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SessionGuidStr {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 36; // to hold UUID.randomUuid().toString()
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @SessionGuidStr
    public abstract String getSessionGuidStr();
    public abstract void setSessionGuidStr(String sessionGuidStr);



    @Property(
        domainEvent = SessionGuid.DomainEvent.class
    )
    @PropertyLayout(
            fieldSetId = "Identity",
            hidden = Where.PARENTED_TABLES,
            sequence = "1"
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SessionGuid {
        class DomainEvent extends PropertyDomainEvent<UUID> {}
    }
    @SessionGuid
    public UUID getSessionGuid() {return UUID.fromString(getSessionGuidStr());}


    @Property(
            domainEvent = HttpSessionId.DomainEvent.class
    )
    @PropertyLayout(
            fieldSetId = "Metadata",
            hidden = Where.PARENTED_TABLES,
            sequence = "2"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface HttpSessionId {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 32;
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @HttpSessionId
    public abstract String getHttpSessionId();
    public abstract void setHttpSessionId(String httpSessionId);




    @Property(
            domainEvent = Username.DomainEvent.class
    )
    @PropertyLayout(
            fieldSetId = "Who",
            hidden = Where.PARENTED_TABLES,
            sequence = "2"
    )
    @HasUsername.Username
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Username {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = HasUsername.Username.MAX_LENGTH;
        boolean NULLABLE = HasUsername.Username.NULLABLE;
        String ALLOWS_NULL = HasUsername.Username.ALLOWS_NULL;
    }
    @Username
    public abstract String getUsername();
    public abstract void setUsername(String username);



    @Property(
            domainEvent = LoginTimestamp.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            fieldSetId = "Duration",
            hidden = Where.PARENTED_TABLES,
            sequence = "1"
    )
    @Parameter(
            optionality = Optionality.MANDATORY
    )
    @ParameterLayout(
            named = "Login timestamp"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LoginTimestamp {
        class DomainEvent extends PropertyDomainEvent<String> {}
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @LoginTimestamp
    public abstract Timestamp getLoginTimestamp();
    public abstract void setLoginTimestamp(Timestamp loginTimestamp);




    @Property(
            domainEvent = LogoutTimestamp.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            fieldSetId = "Duration",
            hidden = Where.PARENTED_TABLES,
            sequence = "2"
    )
    @Parameter(
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            named = "Logout timestamp"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LogoutTimestamp {
        class DomainEvent extends PropertyDomainEvent<String> {}
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @LogoutTimestamp
    public abstract Timestamp getLogoutTimestamp();
    public abstract void setLogoutTimestamp(Timestamp logoutTimestamp);



    @Property(
            domainEvent = CausedBy.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            fieldSetId="Details",
            sequence = "2"
    )
    @Parameter(
            optionality = Optionality.MANDATORY
    )
    @ParameterLayout(
            named = "Caused by"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CausedBy {
        class DomainEvent extends PropertyDomainEvent<String> {}
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @CausedBy
    public abstract SessionLogService.CausedBy getCausedBy();
    public abstract void setCausedBy(SessionLogService.CausedBy causedBy);



    @Action(
            domainEvent = next.DomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            cssClassFa = "fa-step-forward",
            cssClassFaPosition = CssClassFaPosition.RIGHT
    )
    public class next {

        public class DomainEvent extends ActionDomainEvent {
        }

        @MemberSupport public SessionLogEntry act() {
            final List<SessionLogEntry> after = sessionLogEntryRepository.findByUsernameAndStrictlyAfter(getUsername(), getLoginTimestamp());
            return !after.isEmpty() ? after.get(0) : SessionLogEntry.this;
        }

        @MemberSupport public String disableAct() {
            return act() == SessionLogEntry.this ? "None after": null;
        }

        @Inject SessionLogEntryRepository sessionLogEntryRepository;
    }



    @Action(
            domainEvent = previous.DomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            cssClassFa = "fa-step-backward",
            cssClassFaPosition = CssClassFaPosition.RIGHT
    )
    public class previous {

        public class DomainEvent extends ActionDomainEvent {
        }

        @MemberSupport public SessionLogEntry act() {
            final List<SessionLogEntry> before = sessionLogEntryRepository.findByUsernameAndStrictlyBefore(getUsername(), getLoginTimestamp());
            return !before.isEmpty() ? before.get(0) : SessionLogEntry.this;
        }

        @MemberSupport public String disableAct() {
            return act() == SessionLogEntry.this ? "None before": null;
        }

        @Inject SessionLogEntryRepository sessionLogEntryRepository;
    }



    private static final ObjectContracts.ObjectContract<SessionLogEntry> contract	=
            ObjectContracts.contract(SessionLogEntry.class)
                    .thenUse("loginTimestamp", SessionLogEntry::getLoginTimestamp)
                    .thenUse("username", SessionLogEntry::getUsername)
                    .thenUse("sessionGuid", SessionLogEntry::getSessionGuidStr)
                    .thenUse("httpSessionId", SessionLogEntry::getHttpSessionId)
                    .thenUse("logoutTimestamp", SessionLogEntry::getLogoutTimestamp)
                    .thenUse("causedBy", SessionLogEntry::getCausedBy)
            ;


    @Override
    public String toString() {
        return contract.toString(SessionLogEntry.this);
    }

    @Override
    public int compareTo(final SessionLogEntry other) {
        return contract.compare(this,other);
    }



}
