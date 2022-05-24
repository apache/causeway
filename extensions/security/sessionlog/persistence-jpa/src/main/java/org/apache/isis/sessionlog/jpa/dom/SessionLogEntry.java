package org.apache.isis.sessionlog.jpa.dom;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.PrimaryKey;
import org.springframework.data.jpa.repository.Query;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.services.session.SessionLogService;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntry.Nq;

import lombok.NoArgsConstructor;

@Entity
@Table(
        schema = SessionLogEntry.SCHEMA,
        name = SessionLogEntry.TABLE)

@NamedQueries( {
        @NamedQuery(
                name  = Nq.FIND_BY_SESSION_ID,
                query = "SELECT e "
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.sessionId = :sessionId"),
        @NamedQuery(
                name  = Nq.FIND_BY_USERNAME_AND_TIMESTAMP_BETWEEN,
                query = "SELECT e"
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.username = :username "
                      + "   AND e.loginTimestamp >= :from "
                      + "   AND e.logoutTimestamp <= :to "
                      + " ORDER BY e.loginTimestamp DESC"),
        @NamedQuery(
                name  = Nq.FIND_BY_USERNAME_AND_TIMESTAMP_AFTER,
                query = "SELECT e"
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.username = :username "
                      + "   AND e.loginTimestamp >= :from "
                      + " ORDER BY e.loginTimestamp DESC"),
        @NamedQuery(
                name  = Nq.FIND_BY_USERNAME_AND_TIMESTAMP_BEFORE,
                query = "SELECT e"
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.username = :username "
                      + "   AND e.loginTimestamp <= :from "
                      + " ORDER BY e.loginTimestamp DESC"),
        @NamedQuery(
                name  = Nq.FIND_BY_USERNAME,
                query = "SELECT e"
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.username = :username "
                      + " ORDER BY e.loginTimestamp DESC"),
        @NamedQuery(
                name  = Nq.FIND_BY_TIMESTAMP_BETWEEN,
                query = "SELECT e"
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.loginTimestamp >= :from "
                      + "   AND e.logoutTimestamp <= :to "
                      + " ORDER BY e.loginTimestamp DESC"),
        @NamedQuery(
                name  = Nq.FIND_BY_TIMESTAMP_AFTER,
                query = "SELECT e"
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.loginTimestamp >= :from "
                      + " ORDER BY e.loginTimestamp DESC"),
        @NamedQuery(
                name  = Nq.FIND_BY_TIMESTAMP_BEFORE,
                query = "SELECT e"
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.loginTimestamp <= :to "
                      + " ORDER BY e.loginTimestamp DESC"),
        @NamedQuery(
                name  = Nq.FIND,
                query = "SELECT e"
                      + "  FROM SessionLogEntry e "
                      + " ORDER BY e.loginTimestamp DESC"),
        @NamedQuery(
                name  = Nq.FIND_BY_USERNAME_AND_TIMESTAMP_STRICTLY_BEFORE,
                query = "SELECT e"
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.username = :username "
                      + "   AND e.loginTimestamp < :from "
                      + " ORDER BY e.loginTimestamp DESC"),
        @NamedQuery(
                name  = Nq.FIND_BY_USERNAME_AND_TIMESTAMP_STRICTLY_AFTER,
                query = "SELECT e"
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.username = :username "
                      + "   AND e.loginTimestamp > :from "
                      + " ORDER BY e.loginTimestamp ASC"),
        @NamedQuery(
                name  = Nq.FIND_ACTIVE_SESSIONS,
                query = "SELECT e"
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.logoutTimestamp == null "
                      + " ORDER BY e.loginTimestamp ASC"),
        @NamedQuery(
                name  = Nq.FIND_RECENT_BY_USERNAME,
                query = "SELECT e"
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.username = :username "
                      + " ORDER BY e.loginTimestamp DESC "
                    /*+ " LIMIT 0,10"*/)  // instead use withRange on the Query.named(...) object in the repo.
})
@EntityListeners(IsisEntityListener.class)
@DomainObject(
        logicalTypeName = SessionLogEntry.LOGICAL_TYPE_NAME,
        editing = Editing.DISABLED
)
public class SessionLogEntry extends org.apache.isis.sessionlog.applib.dom.SessionLogEntry {


    public SessionLogEntry(
            final String sessionId,
            final String username,
            final SessionLogService.CausedBy causedBy,
            final Timestamp loginTimestamp) {
        super(sessionId, username, causedBy, loginTimestamp);
    }

    public SessionLogEntry() {
        super(null, null, null, null);
    }


    @Id
    @Column(nullable = false, length=15)
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



    @Column(nullable = false, length = Username.MAX_LENGTH)
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



    @Column(nullable = false)
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



    @Column(nullable = true)
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



    @Column(nullable = false)
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
