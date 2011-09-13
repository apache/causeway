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
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.AbstractRepresentationBuilder;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;

@Path("/services")
public class DomainServiceResourceServerside extends ResourceAbstract implements
        DomainServiceResource {

    @Override
    @Produces({ MediaType.APPLICATION_JSON })
    public Response services() {
        init();

        ResourceContext resourceContext = getResourceContext();
        final List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();

        DomainObjectListRepBuilder builder = 
                DomainObjectListRepBuilder.newBuilder(resourceContext)
                    .usingLinkToBuilder(new DomainServiceLinkToBuilder())
                    .withSelf("services")
                    .withAdapters(serviceAdapters);
        
        return Response.ok()
                .entity(jsonFrom(builder))
                .cacheControl(CACHE_NONE)
                .header(RestfulResponse.Header.X_REPRESENTATION_TYPE.getName(), RepresentationType.LIST.getName())
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    ////////////////////////////////////////////////////////////
    // domain service
    ////////////////////////////////////////////////////////////

    @GET
    @Path("/{serviceId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Override
    public Response service(@PathParam("serviceId") String serviceId) {
        init();
        
        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);
        if(serviceAdapter == null) {
            Object[] args = { serviceId };
            throw JsonApplicationException.create(HttpStatusCode.NOT_FOUND, "Could not locate service '%s'", args);
        }
        ResourceContext resourceContext = getResourceContext();

        AbstractRepresentationBuilder<?> builder = 
                DomainObjectRepBuilder.newBuilder(resourceContext)
                    .usingLinkToBuilder(new DomainServiceLinkToBuilder())
                    .withAdapter(serviceAdapter);
        
        return Response.ok()
                .entity(jsonFrom(builder))
                .cacheControl(CACHE_NONE)
                .header(RestfulResponse.Header.X_REPRESENTATION_TYPE.getName(), RepresentationType.DOMAIN_OBJECT.getName())
                .type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
    }


    private ObjectAdapter getServiceAdapter(String serviceId) {
        final List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
        for (ObjectAdapter serviceAdapter : serviceAdapters) {
            Object servicePojo = serviceAdapter.getObject();
            String id = ServiceUtil.id(servicePojo);
            if(serviceId.equals(id)) {
                return serviceAdapter;
            }
        }
        return null;
    }

    
    ////////////////////////////////////////////////////////////
    // domain service action
    ////////////////////////////////////////////////////////////
    
    @GET
    @Path("/{serviceId}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response serviceActionPrompt(
        @PathParam("serviceId") final String serviceId, 
        @PathParam("actionId") final String actionId) {
        return null;
    }

    
    ////////////////////////////////////////////////////////////
    // domain service action invoke
    ////////////////////////////////////////////////////////////

    @GET
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response serviceInvokeActionQueryOnly(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId,
        @QueryParam("args") final String arguments) {
        return null;
    }

    @PUT
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response serviceInvokeActionIdempotent(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId,
        final InputStream arguments) {
        return null;
    }

    @POST
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response serviceInvokeAction(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId,
        final InputStream arguments) {
        return null;
    }

}
