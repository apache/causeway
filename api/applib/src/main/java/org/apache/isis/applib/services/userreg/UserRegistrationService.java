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
 * Required API to enable users to register an account on the system (aka &quot;sign up&quot;).
 *
 * <p>
 *     User registration also requires that the {@link EmailNotificationService} and
 *     {@link org.apache.isis.applib.services.email.EmailService} to be configured.  The framework provides default
 *     implementations of both of these services.  The notification service requires no further configuration.
 *     The email service (<code>EmailServiceDefault</code>) <i>does</i> require a couple of configuration properties
 *     to be set (specifying the SMTP mail server/accounts/password).
 * </p>
 * 
 * @since 1.x {@index}
 */
public interface UserRegistrationService {

    boolean usernameExists(String username);

    boolean emailExists(String emailAddress);

    void registerUser(UserDetails userDetails);

    boolean updatePasswordByEmail(String emailAddress, String password);

}
