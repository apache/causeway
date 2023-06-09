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
package org.apache.causeway.core.security.authentication.logout;

/**
 * To allow viewers to close their session when a logout is requested.
 */
public interface LogoutHandler {

    /**
     * Logout from the viewer's session, such that the application user has to sign in again.
     *
     * @apiNote Implementations perhaps should also check, whether they are called within a thread that belongs
     *   to a request-cycle originating from the appropriate viewer/servlet, this logout is meant to target.
     *   (Otherwise a logout originating from one viewer implementation might trigger a logout in another one.
     *   However, we have not yet specified whether thats undesired behavior or not.)
     */
    void logout();

}
