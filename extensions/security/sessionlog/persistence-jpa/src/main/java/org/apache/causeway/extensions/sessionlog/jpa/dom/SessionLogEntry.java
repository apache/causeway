/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.sessionlog.jpa.dom;

import java.sql.Timestamp;
import java.util.UUID;

import javax.inject.Named;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.services.session.SessionSubscriber;
import org.apache.causeway.extensions.sessionlog.applib.dom.SessionLogEntry.Nq;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        schema = SessionLogEntry.SCHEMA,
        name = SessionLogEntry.TABLE
)
@NamedQueries( {
        @NamedQuery(
                name  = Nq.FIND_BY_SESSION_GUID,
                query = "SELECT e "
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.pk.sessionGuid = :sessionGuid"),
        @NamedQuery(
                name  = Nq.FIND_BY_HTTP_SESSION_ID,
                query = "SELECT e "
                      + "  FROM SessionLogEntry e "
                      + " WHERE e.httpSessionId = :httpSessionId"),
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
                      + " ORDER BY e.loginTimestamp DESC ")  // range 0,10 programmatically
})
@EntityListeners(CausewayEntityListener.class)
@Named(SessionLogEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED
)
public class SessionLogEntry extends org.apache.causeway.extensions.sessionlog.applib.dom.SessionLogEntry {

    public SessionLogEntry(
            final UUID sessionGuid,
            final String httpSessionId,
            final String username,
            final SessionSubscriber.CausedBy causedBy,
            final Timestamp loginTimestamp) {
        super(sessionGuid, httpSessionId, username, causedBy, loginTimestamp);
    }

    public SessionLogEntry() {
        this(null, null, null, null, null);
    }


    @EmbeddedId
    private SessionLogEntryPK pk;

    @Transient
    @SessionGuid
    @Override
    public UUID getSessionGuid() {
        return pk != null ? pk.getSessionGuid() : null;
    }
    @Transient
    @Override
    public void setSessionGuid(UUID sessionGuid) {
        this.pk = new SessionLogEntryPK(sessionGuid);
    }

    @Column(nullable = HttpSessionId.NULLABLE, length = HttpSessionId.MAX_LENGTH)
    @HttpSessionId
    @Getter @Setter
    private String httpSessionId;


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
    private SessionSubscriber.CausedBy causedBy;


}
