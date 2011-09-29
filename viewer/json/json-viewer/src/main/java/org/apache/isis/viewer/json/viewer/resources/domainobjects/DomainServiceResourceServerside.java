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
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulMediaType;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.AbstractReprBuilder;
import org.apache.isis.viewer.json.viewer.representations.ReprBuilder;

@Path("/services")
public class DomainServiceResourceServerside extends DomainResourceAbstract implements
        DomainServiceResource {

    @Override
    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_LIST, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response services() {
        init();

        final ReprBuilder builder = 
                new DomainServiceResourceHelper(getResourceContext(), "services").services();
        
        return responseOfOk(RepresentationType.LIST, Caching.ONE_DAY, builder).build();
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
        init();
        
        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);
        
        ResourceContext resourceContext = getResourceContext();
        AbstractReprBuilder<?> builder = 
                DomainObjectReprBuilder.newBuilder(resourceContext)
                    .usingLinkToBuilder(new DomainServiceLinkToBuilder())
                    .with(serviceAdapter);
        
        return responseOfOk(RepresentationType.DOMAIN_OBJECT, Caching.ONE_DAY, builder).build();
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
        init();

        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);
        final OneToOneAssociation property = getPropertyThatIsVisibleAndUsable(
                serviceAdapter, propertyId, Intent.ACCESS);

        ResourceContext resourceContext = getResourceContext();
        final ObjectPropertyReprBuilder builder = ObjectPropertyReprBuilder.newBuilder(
                resourceContext, serviceAdapter, property);

        return responseOfOk(RepresentationType.OBJECT_PROPERTY, Caching.ONE_DAY, builder).build();
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
        init();

        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);

        return actionPrompt(actionId, serviceAdapter);
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
        init();

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
        init();

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
        init();

        // TODO
        throw new UnsupportedOperationException();
    }

}
