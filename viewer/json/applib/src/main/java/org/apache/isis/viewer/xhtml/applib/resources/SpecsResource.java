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
package org.apache.isis.viewer.xhtml.applib.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public interface SpecsResource {

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON })
    public abstract String specs();

    @GET
    @Path("/{specFullName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public abstract String spec(@PathParam("specFullName") final String specFullName);

    @GET
    @Path("/{specFullName}/facet/{facetType}")
    @Produces({ MediaType.APPLICATION_JSON })
    public abstract String specFacet(@PathParam("specFullName") final String specFullName,
        @PathParam("facetType") final String facetTypeName);

    @GET
    @Path("/{specFullName}/property/{propertyName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public abstract String specProperty(@PathParam("specFullName") final String specFullName,
        @PathParam("propertyName") final String propertyName);

    @GET
    @Path("/{specFullName}/collection/{collectionName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public abstract String specCollection(@PathParam("specFullName") final String specFullName,
        @PathParam("collectionName") final String collectionName);

    @GET
    @Path("/{specFullName}/action/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public abstract String specAction(@PathParam("specFullName") final String specFullName,
        @PathParam("actionId") final String actionId);

    @GET
    @Path("/{specFullName}/property/{propertyName}/facet/{facetType}")
    @Produces({ MediaType.APPLICATION_JSON })
    public abstract String specPropertyFacet(@PathParam("specFullName") final String specFullName,
        @PathParam("propertyName") final String propertyName, @PathParam("facetType") final String facetTypeName);

    @GET
    @Path("/{specFullName}/collection/{collectionName}/facet/{facetType}")
    @Produces({ MediaType.APPLICATION_JSON })
    public abstract String specCollectionFacet(@PathParam("specFullName") final String specFullName,
        @PathParam("collectionName") final String collectionName, @PathParam("facetType") final String facetTypeName);

    @GET
    @Path("/{specFullName}/action/{actionId}/facet/{facetType}")
    @Produces({ MediaType.APPLICATION_JSON })
    public abstract String specActionFacet(@PathParam("specFullName") final String specFullName,
        @PathParam("actionId") final String actionId, @PathParam("facetType") final String facetTypeName);

}