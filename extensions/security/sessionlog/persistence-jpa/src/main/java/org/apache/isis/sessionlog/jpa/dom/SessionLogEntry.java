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
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.applib.services.session.SessionLogService;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntry.Nq;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
                      + " WHERE e.logoutTimestamp IS null "
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
    @Column(nullable = SessionId.NULLABLE, length = SessionId.MAX_LENGTH)
    @SessionId
    @Getter @Setter
    private String sessionId;


    @Column(nullable = Username.NULLABLE, length = Username.MAX_LENGTH)
    @Username
    @Getter @Setter
    private String username;


    @Column(nullable = LoginTimestamp.NULLABLE)
    @LoginTimestamp
    @Getter @Setter
    private Timestamp loginTimestamp;


    @Column(nullable = LogoutTimestamp.NULLABLE)
    @LogoutTimestamp
    @Getter @Setter
    private Timestamp logoutTimestamp;


    @Column(nullable = CausedBy.NULLABLE)
    @CausedBy
    @Getter @Setter
    private SessionLogService.CausedBy causedBy;


}
