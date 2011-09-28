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
package org.apache.isis.viewer.json.viewer.resources.home;

import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulMediaType;
import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.apache.isis.viewer.json.viewer.representations.LinkReprBuilder;
import org.apache.isis.viewer.json.viewer.representations.ReprBuilder;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.DomainServiceResourceHelper;
import org.apache.isis.viewer.json.viewer.resources.user.UserReprBuilder;
import org.apache.isis.viewer.json.viewer.resources.user.UserResourceHelper;

/**
 * Implementation note: it seems to be necessary to annotate the implementation with {@link Path} rather than the
 * interface (at least under RestEasy 1.0.2 and 1.1-RC2).
 */
public class HomePageResourceServerside extends ResourceAbstract implements HomePageResource {


    @Override
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_HOME_PAGE} )
    public Response resources() {
        init();
        
        JsonRepresentation representation = JsonRepresentation.newMap();
        
        // self
        representation.mapPut("self", LinkReprBuilder.newBuilder(getResourceContext(), "self", "").build());

        // user
        putLinkToUser(representation);

        // services
        putLinkToServices(representation);
        
        // capabilities
        representation.mapPut("capabilities", LinkReprBuilder.newBuilder(getResourceContext(), "capabilities", "capabilities").build());

        //
        representation.mapPut("links", JsonRepresentation.newArray());
        representation.mapPut("extensions", JsonRepresentation.newMap());

        return responseOfOk(RepresentationType.HOME_PAGE, Caching.ONE_DAY, representation).build();
    }

    private void putLinkToServices(JsonRepresentation representation) {

        final LinkReprBuilder servicesLinkBuilder = LinkReprBuilder.newBuilder(getResourceContext(), "services", "services");
        
        final List<String> followLinks = getResourceContext().getArg(QueryParameter.FOLLOW_LINKS);
        if(followLinks.contains("services")) {
            final ReprBuilder reprBuilder = 
                    new DomainServiceResourceHelper(getResourceContext()).services();
            servicesLinkBuilder.withValue(reprBuilder.build());
        }
        
        representation.mapPut("services", servicesLinkBuilder.build());
    }

    private void putLinkToUser(JsonRepresentation representation) {
        final LinkReprBuilder userLinkBuilder = LinkReprBuilder.newBuilder(getResourceContext(), "user", "user");
        
        final List<String> followLinks = getResourceContext().getArg(QueryParameter.FOLLOW_LINKS);
        if(followLinks.contains("user")) {
            final ReprBuilder reprBuilder = 
                    new UserResourceHelper(getResourceContext()).user();
            userLinkBuilder.withValue(reprBuilder.build());
        }
        
        representation.mapPut("user", userLinkBuilder.build());
    }



}