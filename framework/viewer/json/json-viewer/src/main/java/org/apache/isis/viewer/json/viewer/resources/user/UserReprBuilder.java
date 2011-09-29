/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.user;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.TypedReprBuilderAbstract;

public class UserReprBuilder extends TypedReprBuilderAbstract<UserReprBuilder, AuthenticationSession> {

    public static UserReprBuilder newBuilder(ResourceContext resourceContext) {
        return new UserReprBuilder(resourceContext);
    }

    private boolean includesSelf;

    private UserReprBuilder(ResourceContext resourceContext) {
        super(resourceContext);
    }

    @Override
    public UserReprBuilder with(AuthenticationSession authenticationSession) {
        representation.mapPut("username", authenticationSession.getUserName());
        JsonRepresentation roles = JsonRepresentation.newArray();
        for (String role : authenticationSession.getRoles()) {
            roles.arrayAdd(role);
        }
        representation.mapPut("roles", roles);
        return this;
    }

    public JsonRepresentation build() {
        if(includesSelf) {
            withSelf("user");
        }
        withLinks();
        withExtensions();
        return representation;
    }

    public UserReprBuilder withSelf() {
        this.includesSelf = true;
        return this;
    }

}