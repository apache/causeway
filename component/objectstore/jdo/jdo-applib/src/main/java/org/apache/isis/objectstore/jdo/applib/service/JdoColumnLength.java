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

public final class JdoColumnLength {

    private JdoColumnLength() {
    }

    public final static int TRANSACTION_ID = 36;
    // ie OID str (based on the defacto limit of a request URL in web browsers such as IE8)
    public final static int BOOKMARK = 2000; 
    public static final int MEMBER_IDENTIFIER = 255;
    public static final int USER_NAME = 50;
    public final static int TARGET_CLASS = 50;
    public final static int TARGET_ACTION = 50;
    
    public static final int DESCRIPTION = 254;

    public static final class SettingAbstract {
        private SettingAbstract(){}
        
        public static final int SETTING_KEY = 128;
        public static final int SETTING_TYPE = 20;
        public static final int VALUE_RAW = 255;
    }

    public static final class Command {
        private Command() {
        }
        public static final int EXECUTE_IN = 10;
    }

    public static final class AuditEntry {

        private AuditEntry() {
        }
        public static final int PROPERTY_ID = 50;
        public static final int PROPERTY_VALUE = 255;
    }
    
    public static final class PublishedEvent {

        private PublishedEvent() {
        }
        
        public static final int TITLE = 255;
        public static final int EVENT_TYPE = 20;
        public static final int STATE = 20;
    }
    
}
