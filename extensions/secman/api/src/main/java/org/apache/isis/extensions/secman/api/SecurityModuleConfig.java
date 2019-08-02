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
package org.apache.isis.extensions.secman.api;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
public class SecurityModuleConfig {

    // -- ROLES

    @Getter @Builder.Default @NonNull
    final String regularUserRoleName = "isis-module-security-regular-user";

    @Getter @Builder.Default @NonNull
    final String fixtureRoleName = "isis-module-security-fixtures";

    @Getter @Builder.Default @NonNull
    final String adminRoleName = "isis-module-security-admin";

    // -- ADMIN

    @Getter @Builder.Default @NonNull
    final String adminUserName = "isis-module-security-admin";

    @Getter @Builder.Default @NonNull
    final String adminPassword = "pass";

    @Getter @Builder.Default @NonNull
    final String[] adminStickyPackagePermissions = new String[]{
            "org.apache.isis.extensions.secman.api",
            "org.apache.isis.extensions.secman.jdo.dom",
    };


}
