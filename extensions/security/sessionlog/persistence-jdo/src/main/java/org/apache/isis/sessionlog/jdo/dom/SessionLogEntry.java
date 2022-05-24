package org.apache.isis.sessionlog.jdo.dom;

import java.sql.Timestamp;

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

@PersistenceCapable(
        identityType=IdentityType.APPLICATION,
        schema = SessionLogEntry.SCHEMA,
        table = SessionLogEntry.TABLE)
@Queries( {
        @Query(
                name  = Nq.FIND_BY_SESSION_ID,
                value = "SELECT "
                      + "  FROM " + SessionLogEntry.FQCN + " "
                      + " WHERE sessionId == :sessionId"),
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
@DomainObject(
        logicalTypeName = SessionLogEntry.LOGICAL_TYPE_NAME,
        editing = Editing.DISABLED
)
public class SessionLogEntry extends org.apache.isis.sessionlog.applib.dom.SessionLogEntry {

    public static final String FQCN = "org.apache.isis.sessionlog.jdo.dom.SessionLogEntry";

    public SessionLogEntry(
            final String sessionId,
            final String username,
            final SessionLogService.CausedBy causedBy,
            final Timestamp loginTimestamp) {
        super(sessionId, username, causedBy, loginTimestamp);
    }


    @PrimaryKey
    @Column(allowsNull = "false", length=15)
    private String sessionId;
    @Override
    @SessionId
    public String getSessionId() {
        return sessionId;
    }
    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }



    @Column(allowsNull = "false", length = Username.MAX_LENGTH)
    private String username;
    @Override
    @Username
    public String getUsername() {
        return username;
    }
    @Override
    public void setUsername(String username) {
        this.username = username;
    }



    @Column(allowsNull = "false")
    private Timestamp loginTimestamp;
    @Override
    @LoginTimestamp
    public Timestamp getLoginTimestamp() {
        return loginTimestamp;
    }
    @Override
    public void setLoginTimestamp(Timestamp loginTimestamp) {
        this.loginTimestamp = loginTimestamp;
    }



    @Column(allowsNull = "true")
    private Timestamp logoutTimestamp;
    @Override
    @LogoutTimestamp
    public Timestamp getLogoutTimestamp() {
        return logoutTimestamp;
    }
    @Override
    public void setLogoutTimestamp(Timestamp logoutTimestamp) {
        this.logoutTimestamp = logoutTimestamp;
    }



    @Column(allowsNull = "false")
    private SessionLogService.CausedBy causedBy;
    @Override
    @CausedBy
    public SessionLogService.CausedBy getCausedBy() {
        return causedBy;
    }
    @Override
    public void setCausedBy(SessionLogService.CausedBy causedBy) {
        this.causedBy = causedBy;
    }


}
