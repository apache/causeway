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
package org.apache.isis.viewer.restfulobjects.applib.domainobjects;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;

/**
 * @since 1.x {@index}
 */
@Path("/services")
public interface DomainServiceResource {

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_LIST, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response services();

    @DELETE
    @Path("/")
    public Response deleteServicesNotAllowed();

    @PUT
    @Path("/")
    public Response putServicesNotAllowed();

    @POST
    @Path("/")
    public Response postServicesNotAllowed();

    // //////////////////////////////////////////////////////////
    // domain service
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{serviceId}")
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response service(@PathParam("serviceId") final String serviceId);

    @DELETE
    @Path("/{serviceId}")
    public Response deleteServiceNotAllowed(@PathParam("serviceId") final String serviceId);

    @PUT
    @Path("/{serviceId}")
    public Response putServiceNotAllowed(@PathParam("serviceId") final String serviceId);

    @POST
    @Path("/{serviceId}")
    public Response postServiceNotAllowed(@PathParam("serviceId") final String serviceId);


    // //////////////////////////////////////////////////////////
    // domain service action
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{serviceId}/actions/{actionId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_ACTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response actionPrompt(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId);

    @DELETE
    @Path("/{serviceId}/actions/{actionId}")
    public Response deleteActionPromptNotAllowed(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId);

    @PUT
    @Path("/{serviceId}/actions/{actionId}")
    public Response putActionPromptNotAllowed(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId);

    @POST
    @Path("/{serviceId}/actions/{actionId}")
    public Response postActionPromptNotAllowed(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId);

    // //////////////////////////////////////////////////////////
    // domain service action invoke
    // //////////////////////////////////////////////////////////

    /**
     * Because it isn't possible with the RestEasy client-side framework to specify a query string nor to pass arbitrary query params; instead
     * we provide an additional syntax of passing an Isis-defined query param <tt>x-isis-querystring</tt>.
     *
     * <p>
     * The content of this is taken to be the URL encoded map of arguments.
     */
    @GET
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeActionQueryOnly(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId, @QueryParam("x-isis-querystring") final String xIsisQueryString);

    @PUT
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeActionIdempotent(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId, final InputStream arguments);

    @POST
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeAction(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId, final InputStream arguments);

    @DELETE
    @Path("/{serviceId}/actions/{actionId}/invoke")
    public Response deleteInvokeActionNotAllowed(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId);
}
