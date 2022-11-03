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

package org.apache.causeway.extensions.sessionlog.applib.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.applib.mixins.security.HasUsername;
import org.apache.causeway.applib.services.session.SessionSubscriber;
import org.apache.causeway.applib.util.ObjectContracts;
import org.apache.causeway.commons.internal.base._Temporals;
import org.apache.causeway.extensions.sessionlog.applib.CausewayModuleExtSessionLogApplib;

import lombok.experimental.UtilityClass;

/**
 * Represents the session of an end-user, in other words the span of time from them logging into the application until
 * they are log out either explicitly or through some timeout.
 *
 * @since 2.0 {@index}
 */
@Named(SessionLogEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@DomainObjectLayout(
        titleUiEvent = SessionLogEntry.TitleUiEvent.class,
        iconUiEvent = SessionLogEntry.IconUiEvent.class,
        cssClassUiEvent = SessionLogEntry.CssClassUiEvent.class,
        layoutUiEvent = SessionLogEntry.LayoutUiEvent.class
)
public abstract class SessionLogEntry implements HasUsername, Comparable<SessionLogEntry> {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtSessionLogApplib.NAMESPACE + ".SessionLogEntry";
    public static final String SCHEMA = CausewayModuleExtSessionLogApplib.SCHEMA;
    public static final String TABLE = "SessionLogEntry";

    @UtilityClass
    public static class Nq {
        public static final String FIND_BY_SESSION_GUID = LOGICAL_TYPE_NAME + ".findBySessionGuid";
        public static final String FIND_BY_HTTP_SESSION_ID = LOGICAL_TYPE_NAME + ".findByHttpSessionId";
        public static final String FIND_BY_USERNAME_AND_TIMESTAMP_BETWEEN = LOGICAL_TYPE_NAME + ".findByUsernameAndTimestampBetween";
        public static final String FIND_BY_USERNAME_AND_TIMESTAMP_AFTER = LOGICAL_TYPE_NAME + ".findByUsernameAndTimestampAfter";
        public static final String FIND_BY_USERNAME_AND_TIMESTAMP_BEFORE = LOGICAL_TYPE_NAME + ".findByUsernameAndTimestampBefore";
        public static final String FIND_BY_USERNAME = LOGICAL_TYPE_NAME + ".findByUsername";
        public static final String FIND_BY_TIMESTAMP_BETWEEN = LOGICAL_TYPE_NAME + ".findByTimestampBetween";
        public static final String FIND_BY_TIMESTAMP_AFTER = LOGICAL_TYPE_NAME + ".findByTimestampAfter";
        public static final String FIND_BY_TIMESTAMP_BEFORE = LOGICAL_TYPE_NAME + ".findByTimestampBefore";
        public static final String FIND = LOGICAL_TYPE_NAME + ".find";
        public static final String FIND_BY_USERNAME_AND_TIMESTAMP_STRICTLY_BEFORE = LOGICAL_TYPE_NAME + ".findByUsernameAndTimestampStrictlyBefore";
        public static final String FIND_BY_USERNAME_AND_TIMESTAMP_STRICTLY_AFTER = LOGICAL_TYPE_NAME + ".findByUsernameAndTimestampStrictlyAfter";
        public static final String FIND_ACTIVE_SESSIONS = LOGICAL_TYPE_NAME + ".findActiveSessions";
        public static final String FIND_RECENT_BY_USERNAME = LOGICAL_TYPE_NAME + ".findRecentByUsername";
    }

    // -- UI & DOMAIN EVENTS

    public static class TitleUiEvent extends CausewayModuleExtSessionLogApplib.TitleUiEvent<SessionLogEntry> { }
    public static class IconUiEvent extends CausewayModuleExtSessionLogApplib.IconUiEvent<SessionLogEntry> { }
    public static class CssClassUiEvent extends CausewayModuleExtSessionLogApplib.CssClassUiEvent<SessionLogEntry> { }
    public static class LayoutUiEvent extends CausewayModuleExtSessionLogApplib.LayoutUiEvent<SessionLogEntry> { }

    public static abstract class PropertyDomainEvent<T> extends CausewayModuleExtSessionLogApplib.PropertyDomainEvent<SessionLogEntry, T> { }

    public static abstract class CollectionDomainEvent<T> extends CausewayModuleExtSessionLogApplib.CollectionDomainEvent<SessionLogEntry, T> { }

    public static abstract class ActionDomainEvent extends CausewayModuleExtSessionLogApplib.ActionDomainEvent<SessionLogEntry> { }


    protected SessionLogEntry(
            final UUID sessionGuid,
            final String httpSessionId,
            final String username,
            final SessionSubscriber.CausedBy causedBy,
            final Timestamp loginTimestamp) {
        setSessionGuid(sessionGuid);
        setHttpSessionId(httpSessionId);
        setUsername(username);
        setCausedBy(causedBy);
        setLoginTimestamp(loginTimestamp);
    }

    @ObjectSupport public String title() {
        return String.format("%s: %s logged %s %s",
                _Temporals.DEFAULT_LOCAL_DATETIME_FORMATTER
                    .format(getLoginTimestamp().toLocalDateTime()),
                getUsername(),
                getLogoutTimestamp() == null ? "in": "out",
                getCausedBy() == SessionSubscriber.CausedBy.SESSION_EXPIRATION ? "(session expired)" : "");
    }

    @ObjectSupport public String cssClass() {
        return "sessionLogEntry-" + iconName();
    }

    @ObjectSupport public String iconName() {
        return getLogoutTimestamp() == null
                ? "login"
                :getCausedBy() != SessionSubscriber.CausedBy.SESSION_EXPIRATION
                    ? "logout"
                    : "expired";
    }



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
        String NAME = "sessionGuid";
        int MAX_LENGTH = 36; // to hold UUID#toString()
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @SessionGuid
    public abstract UUID getSessionGuid();
    public abstract void setSessionGuid(UUID sessionGuid);





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
    @Override
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
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            fieldSetId="Details",
            sequence = "2"
    )
    @Parameter(
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            named = "Caused by"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CausedBy {
        class DomainEvent extends PropertyDomainEvent<String> {}
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @CausedBy
    public abstract SessionSubscriber.CausedBy getCausedBy();
    public abstract void setCausedBy(SessionSubscriber.CausedBy causedBy);



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
                    .thenUse("sessionGuid", SessionLogEntry::getSessionGuid)
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
