package org.apache.isis.sessionlog.jdo.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

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

@PersistenceCapable(
        identityType=IdentityType.APPLICATION,
        schema = SessionLogEntry.SCHEMA,
        table = SessionLogEntry.TABLE)
@Queries( {
        @Query(
                name= SessionLogEntry.Nq.FIND_BY_SESSION_ID,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                      + "WHERE sessionId == :sessionId"),
        @Query(
                name= SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_BETWEEN,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                        + "WHERE user == :user "
                        + "&& loginTimestamp >= :from "
                        + "&& logoutTimestamp <= :to "
                        + "ORDER BY loginTimestamp DESC"),
        @Query(
                name= SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_AFTER,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                        + "WHERE user == :user "
                        + "&& loginTimestamp >= :from "
                        + "ORDER BY loginTimestamp DESC"),
        @Query(
                name= SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_BEFORE,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                        + "WHERE user == :user "
                        + "&& loginTimestamp <= :from "
                        + "ORDER BY loginTimestamp DESC"),
        @Query(
                name= SessionLogEntry.Nq.FIND_BY_USER,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                        + "WHERE user == :user "
                        + "ORDER BY loginTimestamp DESC"),
        @Query(
                name= SessionLogEntry.Nq.FIND_BY_TIMESTAMP_BETWEEN,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                        + "WHERE loginTimestamp >= :from "
                        + "&&    logoutTimestamp <= :to "
                        + "ORDER BY loginTimestamp DESC"),
        @Query(
                name= SessionLogEntry.Nq.FIND_BY_TIMESTAMP_AFTER,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                        + "WHERE loginTimestamp >= :from "
                        + "ORDER BY loginTimestamp DESC"),
        @Query(
                name= SessionLogEntry.Nq.FIND_BY_TIMESTAMP_BEFORE,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                        + "WHERE loginTimestamp <= :to "
                        + "ORDER BY loginTimestamp DESC"),
        @Query(
                name= SessionLogEntry.Nq.FIND,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                        + "ORDER BY loginTimestamp DESC"),
        @Query(
                name= SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_STRICTLY_BEFORE,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                        + "WHERE user == :user "
                        + "&& loginTimestamp < :from "
                        + "ORDER BY loginTimestamp DESC"),
        @Query(
                name= SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_STRICTLY_AFTER,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                        + "WHERE user == :user "
                        + "&& loginTimestamp > :from "
                        + "ORDER BY loginTimestamp ASC"),
        @Query(
                name= SessionLogEntry.Nq.FIND_ACTIVE_SESSIONS,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                      + "WHERE logoutTimestamp == null "
                      + "ORDER BY loginTimestamp ASC"),
        @Query(
                name= SessionLogEntry.Nq.FIND_RECENT_BY_USER,
                value="SELECT "
                        + "FROM " + SessionLogEntry.FQCN + " "
                        + "WHERE user == :user "
                        + "ORDER BY loginTimestamp DESC "
                        + "RANGE 0,10")
})
@DomainObject(
        logicalTypeName = SessionLogEntry.LOGICAL_TYPE_NAME,
        editing = Editing.DISABLED
)
public class SessionLogEntry implements HasUsername, Comparable<SessionLogEntry> {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtSessionLogApplib.NAMESPACE + ".SessionLogEntry";
    public static final String FQCN = "org.apache.isis.sessionlog.jdo.dom.SessionLogEntry";
    public static final String SCHEMA = IsisModuleExtSessionLogApplib.SCHEMA;
    public static final String TABLE = "SessionLogEntry";

    @UtilityClass
    static class Nq {
        static final String FIND_BY_SESSION_ID = "findBySessionId";
        static final String FIND_BY_USER_AND_TIMESTAMP_BETWEEN = "findByUserAndTimestampBetween";
        static final String FIND_BY_USER_AND_TIMESTAMP_AFTER = "findByUserAndTimestampAfter";
        static final String FIND_BY_USER_AND_TIMESTAMP_BEFORE = "findByUserAndTimestampBefore";
        static final String FIND_BY_USER = "findByUser";
        static final String FIND_BY_TIMESTAMP_BETWEEN = "findByTimestampBetween";
        static final String FIND_BY_TIMESTAMP_AFTER = "findByTimestampAfter";
        static final String FIND_BY_TIMESTAMP_BEFORE = "findByTimestampBefore";
        static final String FIND = "find";
        static final String FIND_BY_USER_AND_TIMESTAMP_STRICTLY_BEFORE = "findByUserAndTimestampStrictlyBefore";
        static final String FIND_BY_USER_AND_TIMESTAMP_STRICTLY_AFTER = "findByUserAndTimestampStrictlyAfter";
        static final String FIND_ACTIVE_SESSIONS = "findActiveSessions";
        static final String FIND_RECENT_BY_USER = "findRecentByUser";
    }

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSessionLogApplib.PropertyDomainEvent<SessionLogEntry, T> { }

    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSessionLogApplib.CollectionDomainEvent<SessionLogEntry, T> { }

    public static abstract class ActionDomainEvent extends IsisModuleExtSessionLogApplib.ActionDomainEvent<SessionLogEntry> { }

    public SessionLogEntry(
            final String sessionId,
            final String username,
            final SessionLogService.CausedBy causedBy,
            final Timestamp loginTimestamp) {
        this.sessionId = sessionId;
        this.username = username;
        this.causedBy = causedBy;
        this.loginTimestamp = loginTimestamp;
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
            domainEvent = SessionId.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = SessionId.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId="identity",
            hidden = Where.PARENTED_TABLES,
            sequence = "1"
    )
    @Parameter(
            maxLength = SessionId.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Session Id"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SessionId {
        int MAX_LENGTH = 15;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @PrimaryKey
    @Column(allowsNull="false", length=15)
    private String sessionId;

    @SessionId
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }





    @Property(
            domainEvent = Username.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = Username.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId="Identity",
            hidden = Where.PARENTED_TABLES,
            sequence = "2"
    )
    @Parameter(
            maxLength = Username.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Username"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Username {
        int MAX_LENGTH = 120;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Column(allowsNull = "false", length = Username.MAX_LENGTH)
    private String username;

    @Username
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }




    @Property(
            domainEvent = LoginTimestamp.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId="Identity",
            hidden = Where.PARENTED_TABLES,
            sequence = "3"
    )
    @ParameterLayout(
            named = "Login timestamp"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LoginTimestamp {
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Column(allowsNull="false")
    private Timestamp loginTimestamp;

    @LoginTimestamp
    public Timestamp getLoginTimestamp() {
        return loginTimestamp;
    }

    public void setLoginTimestamp(Timestamp loginTimestamp) {
        this.loginTimestamp = loginTimestamp;
    }



    @Property(
            domainEvent = LogoutTimestamp.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            fieldSetId="Identity",
            hidden = Where.PARENTED_TABLES,
            sequence = "3"
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
    }


    @Column(allowsNull="true")
    private Timestamp logoutTimestamp;

    @LogoutTimestamp
    public Timestamp getLogoutTimestamp() {
        return logoutTimestamp;
    }

    public void setLogoutTimestamp(Timestamp logoutTimestamp) {
        this.logoutTimestamp = logoutTimestamp;
    }





    @Property(
            domainEvent = CausedBy.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId="Details",
            sequence = "1"
    )
    @ParameterLayout(
            named = "Caused by"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CausedBy {
        class DomainEvent extends PropertyDomainEvent<String> {}

    }

    @Column(allowsNull = "false")
    private SessionLogService.CausedBy causedBy;

    @CausedBy
    public SessionLogService.CausedBy getCausedBy() {
        return causedBy;
    }

    public void setCausedBy(SessionLogService.CausedBy causedBy) {
        this.causedBy = causedBy;
    }




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
            final List<SessionLogEntry> after = sessionLogEntryRepository.findByUserAndStrictlyAfter(getUsername(), getLoginTimestamp());
            return !after.isEmpty() ? after.get(0) : SessionLogEntry.this;
        }

        @MemberSupport public String disableNext() {
            val next = factoryService.mixin(next.class, SessionLogEntry.this);
            return next.act() == SessionLogEntry.this ? "None after": null;
        }

        @Inject FactoryService factoryService;
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
            final List<SessionLogEntry> before = sessionLogEntryRepository.findByUserAndStrictlyBefore(getUsername(), getLoginTimestamp());
            return !before.isEmpty() ? before.get(0) : SessionLogEntry.this;
        }

        @MemberSupport public String disablePrevious() {
            val previous = factoryService.mixin(previous.class, SessionLogEntry.this);
            return previous.act() == SessionLogEntry.this ? "None before": null;
        }

        @Inject FactoryService factoryService;
    }



    private static final ObjectContracts.ObjectContract<SessionLogEntry> contract	=
            ObjectContracts.contract(SessionLogEntry.class)
                    .thenUse("loginTimestamp", SessionLogEntry::getLoginTimestamp)
                    .thenUse("username", SessionLogEntry::getUsername)
                    .thenUse("sessionId", SessionLogEntry::getSessionId)
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


    @Inject SessionLogEntryRepository sessionLogEntryRepository;

}
