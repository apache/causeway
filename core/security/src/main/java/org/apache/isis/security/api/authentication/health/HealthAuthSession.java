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

package org.apache.isis.security.api.authentication.health;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.isis.core.commons.internal.encoding.DataInputExtended;
import org.apache.isis.security.api.authentication.AuthenticationSessionAbstract;

public class HealthAuthSession extends AuthenticationSessionAbstract {

    private static final long serialVersionUID = 1L;

    private static final String USER_NAME = "__health";
    private static final String ROLE = "__health-role";
    private static final String CODE = "";

    public HealthAuthSession() {
        super(USER_NAME, Stream.of(ROLE), CODE);
    }

    public HealthAuthSession(final DataInputExtended input) throws IOException {
        super(input);
    }


}
