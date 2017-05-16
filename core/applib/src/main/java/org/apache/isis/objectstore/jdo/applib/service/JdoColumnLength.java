/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.objectstore.jdo.applib.service;

import org.apache.isis.applib.annotation.CommandExecuteIn;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.publish.EventMetadata;
import org.apache.isis.applib.services.publish.EventType;
import org.apache.isis.applib.services.audit.AuditEntryPropertyIdType;
import org.apache.isis.applib.services.audit.AuditEntryPropertyValueType;
import org.apache.isis.applib.services.publish.PublishedEventStateType;
import org.apache.isis.applib.services.publish.StatusMessageMessageType;
import org.apache.isis.applib.types.DescriptionType;
import org.apache.isis.applib.services.settings.SettingTypes;
import org.apache.isis.applib.types.MemberIdentifierType;
import org.apache.isis.applib.types.TargetActionType;
import org.apache.isis.applib.types.TargetClassType;

/**
 * @deprecated
 */
@Deprecated
public final class JdoColumnLength {

    private JdoColumnLength() {
    }

    /**
     * @deprecated
     */
    @Deprecated
    public final static int TRANSACTION_ID = HasTransactionId.TransactionIdType.Meta.MAX_LEN;

    /**
     * ie OID str (based on the defacto limit of a request URL in web browsers such as IE8)
     *
     * @deprecated
     */
    @Deprecated
    public final static int BOOKMARK = Bookmark.AsStringType.Meta.MAX_LEN;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int MEMBER_IDENTIFIER = MemberIdentifierType.Meta.MAX_LEN;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int USER_NAME = UserMemento.NameType.Meta.MAX_LEN;
    /**
     * @deprecated
     */
    @Deprecated
    public final static int TARGET_CLASS = TargetClassType.Meta.MAX_LEN;
    /**
     * @deprecated
     */
    @Deprecated
    public final static int TARGET_ACTION = TargetActionType.Meta.MAX_LEN;

    /**
     * @deprecated
     */
    @Deprecated
    public static final int DESCRIPTION = DescriptionType.Meta.MAX_LEN;

    /**
     * @deprecated
     */
    @Deprecated
    public static final class SettingAbstract {
        private SettingAbstract(){}

        /**
         * @deprecated
         */
        @Deprecated
        public static final int SETTING_KEY = SettingTypes.KeyType.Meta.MAX_LEN;
        /**
         * @deprecated
         */
        @Deprecated
        public static final int SETTING_TYPE = SettingTypes.TypeType.Meta.MAX_LEN;
        /**
         * @deprecated
         */
        @Deprecated
        public static final int VALUE_RAW = SettingTypes.ValueRawType.Meta.MAX_LEN;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static final class Command {
        private Command() {
        }
        /**
         * @deprecated
         */
        @Deprecated
        public static final int EXECUTE_IN = CommandExecuteIn.Type.Meta.MAX_LEN;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static final class AuditEntry {

        private AuditEntry() {
        }
        /**
         * @deprecated
         */
        @Deprecated
        public static final int PROPERTY_ID = AuditEntryPropertyIdType.Meta.MAX_LEN;
        /**
         * @deprecated
         */
        @Deprecated
        public static final int PROPERTY_VALUE = AuditEntryPropertyValueType.Meta.MAX_LEN;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static final class PublishedEvent {

        private PublishedEvent() {
        }

        /**
         * @deprecated
         */
        @Deprecated
        public static final int TITLE = EventMetadata.TitleType.Meta.MAX_LEN;
        /**
         * @deprecated
         */
        @Deprecated
        public static final int EVENT_TYPE = EventType.Type.Meta.MAX_LEN;
        /**
         * @deprecated
         */
        @Deprecated
        public static final int STATE = PublishedEventStateType.Meta.MAX_LEN;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static final class StatusMessage {
        /**
         * @deprecated
         */
        @Deprecated
        public static final int MESSAGE = StatusMessageMessageType.Meta.MAX_LEN;
        /**
         * @deprecated
         */
        @Deprecated
        public static final int URI = BOOKMARK;
    }

}
