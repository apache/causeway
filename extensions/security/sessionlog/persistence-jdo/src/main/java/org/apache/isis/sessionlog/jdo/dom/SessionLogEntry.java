package org.apache.isis.sessionlog.jdo.dom;

import java.sql.Timestamp;
import java.util.UUID;

import javax.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.services.session.SessionLogService;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntry.Nq;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
        identityType=IdentityType.APPLICATION,
        schema = SessionLogEntry.SCHEMA,
        table = SessionLogEntry.TABLE)
@Queries( {
        @Query(
                name  = Nq.FIND_BY_SESSION_GUID_STR,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE sessionGuidStr == :sessionGuidStr"),
        @Query(
                name  = Nq.FIND_BY_HTTP_SESSION_ID,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE httpSessionId == :httpSessionId"),
        @Query(
                name  = Nq.FIND_BY_USERNAME_AND_TIMESTAMP_BETWEEN,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + "WHERE username == :username "
                      + "&& loginTimestamp >= :from "
                      + "&& logoutTimestamp <= :to "
                      + "ORDER BY loginTimestamp DESC"),
        @Query(
                name  = Nq.FIND_BY_USERNAME_AND_TIMESTAMP_AFTER,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE username == :username "
                      + "    && loginTimestamp >= :from "
                      + " ORDER BY loginTimestamp DESC"),
        @Query(
                name  = Nq.FIND_BY_USERNAME_AND_TIMESTAMP_BEFORE,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE username == :username "
                      + "    && loginTimestamp <= :from "
                      + " ORDER BY loginTimestamp DESC"),
        @Query(
                name  = Nq.FIND_BY_USERNAME,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE username == :username "
                      + " ORDER BY loginTimestamp DESC"),
        @Query(
                name  = Nq.FIND_BY_TIMESTAMP_BETWEEN,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE loginTimestamp >= :from "
                      + "    && logoutTimestamp <= :to "
                      + " ORDER BY loginTimestamp DESC"),
        @Query(
                name  = Nq.FIND_BY_TIMESTAMP_AFTER,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE loginTimestamp >= :from "
                      + " ORDER BY loginTimestamp DESC"),
        @Query(
                name  = Nq.FIND_BY_TIMESTAMP_BEFORE,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE loginTimestamp <= :to "
                      + " ORDER BY loginTimestamp DESC"),
        @Query(
                name  = Nq.FIND,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " ORDER BY loginTimestamp DESC"),
        @Query(
                name  = Nq.FIND_BY_USERNAME_AND_TIMESTAMP_STRICTLY_BEFORE,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE username == :username "
                      + "    && loginTimestamp < :from "
                      + " ORDER BY loginTimestamp DESC"),
        @Query(
                name  = Nq.FIND_BY_USERNAME_AND_TIMESTAMP_STRICTLY_AFTER,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE username == :username "
                      + "    && loginTimestamp > :from "
                      + " ORDER BY loginTimestamp ASC"),
        @Query(
                name  = Nq.FIND_ACTIVE_SESSIONS,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE logoutTimestamp == null "
                      + " ORDER BY loginTimestamp ASC"),
        @Query(
                name  = Nq.FIND_RECENT_BY_USERNAME,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE username == :username "
                      + " ORDER BY loginTimestamp DESC "
                      + " RANGE 0,10")
})
@Named(SessionLogEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED
)
public class SessionLogEntry extends org.apache.isis.sessionlog.applib.dom.SessionLogEntry {

    public static final String FQCN = "org.apache.isis.sessionlog.jdo.dom.SessionLogEntry";

    public SessionLogEntry(
            final UUID sessionGuid,
            final String httpSessionId,
            final String username,
            final SessionLogService.CausedBy causedBy,
            final Timestamp loginTimestamp) {
        super(sessionGuid, httpSessionId, username, causedBy, loginTimestamp);
    }

    public SessionLogEntry() {
        super(null, null, null, null, null);
    }

    @PrimaryKey
    @Column(allowsNull = SessionGuidStr.ALLOWS_NULL, length = SessionGuidStr.MAX_LENGTH)
    @SessionGuidStr
    @Getter @Setter
    private String sessionGuidStr;


    @Column(allowsNull = HttpSessionId.ALLOWS_NULL, length = HttpSessionId.MAX_LENGTH)
    @HttpSessionId
    @Getter @Setter
    private String httpSessionId;


    @Column(allowsNull = Username.ALLOWS_NULL, length = Username.MAX_LENGTH)
    @Username
    @Getter @Setter
    private String username;


    @Column(allowsNull = LoginTimestamp.ALLOWS_NULL)
    @LoginTimestamp
    @Getter @Setter
    private Timestamp loginTimestamp;


    @Column(allowsNull = LogoutTimestamp.ALLOWS_NULL)
    @LogoutTimestamp
    @Getter @Setter
    private Timestamp logoutTimestamp;


    @Column(allowsNull = CausedBy.ALLOWS_NULL)
    @CausedBy
    @Getter @Setter
    private SessionLogService.CausedBy causedBy;


}
