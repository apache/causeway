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
package org.apache.isis.applib.annotation;

public enum PublishingChangeKind {
    CREATE,
    UPDATE,
    DELETE;

    @Deprecated
    public static PublishedObject.ChangeKind from(final PublishingChangeKind publishingChangeKind) {
        if(publishingChangeKind == null) return null;
        if(publishingChangeKind == CREATE) return PublishedObject.ChangeKind.CREATE;
        if(publishingChangeKind == UPDATE) return PublishedObject.ChangeKind.UPDATE;
        if(publishingChangeKind == DELETE) return PublishedObject.ChangeKind.DELETE;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized changeKind: " + publishingChangeKind);
    }
    @Deprecated
    public static PublishingChangeKind from(final PublishedObject.ChangeKind  publishingChangeKind) {
        if(publishingChangeKind == null) return null;
        if(publishingChangeKind == PublishedObject.ChangeKind.CREATE) return CREATE;
        if(publishingChangeKind == PublishedObject.ChangeKind.UPDATE) return UPDATE;
        if(publishingChangeKind == PublishedObject.ChangeKind.DELETE) return DELETE;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized changeKind: " + publishingChangeKind);
    }
}
