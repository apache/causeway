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

package org.apache.isis.security.api.authorization.manager;

import org.apache.isis.applib.Identifier;
import org.apache.isis.security.api.authentication.AuthenticationSession;

/**
 * Authorizes the user in the current session view and use members of an object.
 *
 */
public interface AuthorizationManager {

    /**
     * Returns true when the user represented by the specified session is
     * authorized to view the member of the class/object represented by the
     * member identifier. Normally the view of the specified field, or the
     * display of the action will be suppress if this returns false.
     */
    boolean isVisible(AuthenticationSession session, Identifier identifier);

    /**
     * Returns true when the use represented by the specified session is
     * authorized to change the field represented by the member identifier.
     * Normally the specified field will be not appear editable if this returns
     * false.
     */
    boolean isUsable(AuthenticationSession session, Identifier identifier);
}
