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
package org.apache.isis.applib.services.userreg;

/**
 * Provides the ability for users to sign-up to access an application by
 * providing a valid email address.
 * Also provides the capability for users to reset their password if forgotten.
 *
 * @since 1.x {@index}
 */
public interface UserRegistrationService {

    /**
     * Checks if there is already a user with the specified username
     *
     * @param username
     * @return
     */
    boolean usernameExists(String username);

    /**
     * Checks if there is already a user with the specified email address.
     *
     * @param emailAddress
     * @return
     */
    boolean emailExists(String emailAddress);

    /**
     * Creates the user, with specified password and email address.
     *
     * <p>
     * The username and email address must both be unique (not being used by an
     * existing user).
     * </p>
     *
     * @param userDetails
     */
    void registerUser(UserDetails userDetails);

    /**
     * Allows the user to reset their password.
     *
     * @param emailAddress
     * @param password
     * @return
     */
    boolean updatePasswordByEmail(String emailAddress, String password);

}
