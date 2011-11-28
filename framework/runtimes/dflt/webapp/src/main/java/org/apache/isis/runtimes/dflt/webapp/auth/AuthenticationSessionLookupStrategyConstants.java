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

package org.apache.isis.runtimes.dflt.webapp.auth;


public class AuthenticationSessionLookupStrategyConstants {

    /**
     * Default value for {@link AuthenticationSessionLookupStrategyConstants#AUTHENTICATION_SESSION_LOOKUP_STRATEGY_KEY} if not specified.
     */
    public static final String AUTHENTICATION_SESSION_LOOKUP_STRATEGY_DEFAULT =
        AuthenticationSessionLookupStrategyDefault.class.getName();

    /**
     * Recommended standard init parameter key for filters and servlets to lookup an implementation of {@link AuthenticationSessionLookupStrategy}.
     */
    public static final String AUTHENTICATION_SESSION_LOOKUP_STRATEGY_KEY = "authenticationSessionLookupStrategy";

    private AuthenticationSessionLookupStrategyConstants(){}
    
}
