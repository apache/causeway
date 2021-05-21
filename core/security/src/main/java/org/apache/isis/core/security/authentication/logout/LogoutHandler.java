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
package org.apache.isis.core.security.authentication.logout;

/**
 *
 * @since Apr 9, 2020
 * TODO we are at early stages of the design, a better idea occurred:
 * actually model the SignIn page as a true ViewModel similar to how we
 * render the home-page; this should allow for the LogoutHandler to be called
 * from the framework more directly and not from within the LogoutMenu's
 * logout action, which is more complicated because, this happens within
 * the context of an IsisInteraction, where we cannot simply purge the
 * current session, when in the middle of an interaction
 */
public interface LogoutHandler {

    /**
     * logout from the viewer's session, such that the application user has to sign in again
     */
    void logout();

    /**
     *
     * @return whether this handler feels responsible for the viewer, that corresponds
     * to the current servlet request
     *
     * @implNote currently the Wicket logout also triggers a Vaadin logout and vice versa;
     * hence implementations should check whether they are called within a thread
     * that belongs to a request cycle originating from the appropriate viewer/servlet
     */
    boolean isHandlingCurrentThread();

}
