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
package org.apache.causeway.core.security.authentication.login;

/**
 * To allow login to be completed.
 *
 * <p>
 *     Provided as a hook for some of the more sophisticated
 *     {@link org.apache.causeway.core.security.authentication.Authenticator}s
 *     (eg keycloak) to synchronise the state of the
 *     {@link org.apache.causeway.core.security.authentication.manager.AuthenticationManager}
 *     on successful external login.
 * </p>
 */
public interface LoginSuccessHandlerUNUSED {

    /**
     * Indicates a successful login
     */
    void onSuccess();

}
