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
package org.apache.isis.viewer.json.viewer.resources.user;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.user.UserResource;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;

public class UserResourceServerside extends ResourceAbstract implements UserResource {

    @Override
    @Produces({ MediaType.APPLICATION_JSON })
    public Response user() {
        init();
        
        ResourceContext resourceContext = getResourceContext();
        UserRepBuilder builder = UserRepBuilder.newBuilder(resourceContext).withAuthenticationSession(getAuthenticationSession());

        return Response.ok()
                .entity(jsonFrom(builder))
                .cacheControl(CACHE_ONE_HOUR)
                .header(RestfulResponse.Header.X_REPRESENTATION_TYPE.getName(), RepresentationType.USER.getName())
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }



}
