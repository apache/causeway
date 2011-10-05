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
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulMediaType;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.viewer.representations.RendererFactory;
import org.apache.isis.viewer.json.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;

@Path("/services")
public class DomainServiceResourceServerside extends ResourceAbstract implements
        DomainServiceResource {

    @Override
    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_LIST, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response services() {
        RepresentationType representationType = RepresentationType.LIST;
        init(representationType);

        final List<ObjectAdapter> serviceAdapters = getResourceContext().getPersistenceSession().getServices();

        final RendererFactory factory = RendererFactoryRegistry.instance.find(representationType);
        
        final ListReprRenderer renderer = (ListReprRenderer) factory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.usingLinkToBuilder(new DomainServiceLinkToBuilder())
                .withSelf("services")
                .with(serviceAdapters);
        
        return responseOfOk(Caching.ONE_DAY, renderer).build();
    }

    ////////////////////////////////////////////////////////////
    // domain service
    ////////////////////////////////////////////////////////////

    @GET
    @Path("/{serviceId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR })
    @Override
    public Response service(
            @PathParam("serviceId") String serviceId) {
        RepresentationType representationType = RepresentationType.DOMAIN_OBJECT;
        init(representationType);
        
        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);
        
        final RendererFactory factory = rendererFactoryRegistry.find(representationType);
        final DomainObjectReprRenderer renderer = 
                (DomainObjectReprRenderer) factory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.usingLinkToBuilder(new DomainServiceLinkToBuilder())
                    .includesSelf()
                    .with(serviceAdapter);
        
        return responseOfOk(Caching.ONE_DAY, renderer).build();
    }


    ////////////////////////////////////////////////////////////
    // domain service property
    ////////////////////////////////////////////////////////////

    @GET
    @Path("/{serviceId}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response propertyDetails(
            @PathParam("serviceId") final String serviceId,
            @PathParam("propertyId") final String propertyId) {
        init(RepresentationType.OBJECT_PROPERTY);

        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext());

        return helper.propertyDetails(serviceAdapter, propertyId, Caching.ONE_DAY);
    }



    ////////////////////////////////////////////////////////////
    // domain service action
    ////////////////////////////////////////////////////////////
    
    @GET
    @Path("/{serviceId}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response actionPrompt(
            @PathParam("serviceId") final String serviceId, 
            @PathParam("actionId") final String actionId) {
        init(RepresentationType.OBJECT_ACTION);

        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext());

        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);

        return helper.actionPrompt(actionId, serviceAdapter);
    }

    
    ////////////////////////////////////////////////////////////
    // domain service action invoke
    ////////////////////////////////////////////////////////////

    @GET
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, RestfulMediaType.APPLICATION_JSON_LIST, RestfulMediaType.APPLICATION_JSON_SCALAR_VALUE, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response serviceInvokeActionQueryOnly(
            @PathParam("oid") final String oidStr, 
            @PathParam("actionId") final String actionId,
            @QueryParam("args") final String arguments) {
        init(RepresentationType.GENERIC);

        // TODO
        throw new UnsupportedOperationException();
    }

    @PUT
    @Path("/{oid}/actions/{actionId}/invoke")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, RestfulMediaType.APPLICATION_JSON_LIST, RestfulMediaType.APPLICATION_JSON_SCALAR_VALUE, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response serviceInvokeActionIdempotent(
            @PathParam("oid") final String oidStr, 
            @PathParam("actionId") final String actionId,
            final InputStream arguments) {
        init(RepresentationType.GENERIC);

        // TODO
        throw new UnsupportedOperationException();
    }

    @POST
    @Path("/{oid}/actions/{actionId}/invoke")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, RestfulMediaType.APPLICATION_JSON_LIST, RestfulMediaType.APPLICATION_JSON_SCALAR_VALUE, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response serviceInvokeAction(
            @PathParam("oid") final String oidStr, 
            @PathParam("actionId") final String actionId,
            final InputStream arguments) {
        init(RepresentationType.GENERIC);

        // TODO
        throw new UnsupportedOperationException();
    }

}
