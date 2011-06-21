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
package org.apache.isis.viewer.restful.viewer2.resources;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.restful.viewer2.ResourceContext;
import org.apache.isis.viewer.restful.viewer2.resources.objects.DomainObjectRepresentation;
import org.apache.isis.viewer.restful.viewer2.util.OidUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

public abstract class ResourceAbstract {

    public final static ActionType[] ACTION_TYPES = { ActionType.USER, ActionType.DEBUG, ActionType.EXPLORATION,
    // SET is excluded; we simply flatten contributed actions.
        };


    @Context
    HttpHeaders httpHeaders;

    @Context
    UriInfo uriInfo;

    @Context
    Request request;

    @Context
    HttpServletRequest httpServletRequest;

    @Context
    HttpServletResponse httpServletResponse;

    @Context
    SecurityContext securityContext;

    private ResourceContext resourceContext;


    protected void init() {
        this.resourceContext =
            new ResourceContext(httpHeaders, uriInfo, request, httpServletRequest, httpServletResponse, securityContext);
    }

    protected ResourceContext getResourceContext() {
        return resourceContext;
    }

    
    // //////////////////////////////////////////////////////////////
    // Rendering
    // //////////////////////////////////////////////////////////////

    protected String asJson(final Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonGenerationException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String asJsonList(List<DomainObjectRepresentation> services) {
        return asJson(services);
    }

    // //////////////////////////////////////////////////////////////
    // Isis integration
    // //////////////////////////////////////////////////////////////

    protected ObjectSpecification getSpecification(final String specFullName) {
        return getSpecificationLoader().loadSpecification(specFullName);
    }

    protected ObjectAdapter getNakedObject(final String oidEncodedStr) {
        return OidUtils.getNakedObject(oidEncodedStr, getOidStringifier());
    }

    protected String getOidStr(final ObjectAdapter nakedObject) {
        return OidUtils.getOidStr(nakedObject, getOidStringifier());
    }



    // //////////////////////////////////////////////////////////////
    // Responses
    // //////////////////////////////////////////////////////////////

    protected Response responseOfOk() {
        return Response.ok().build();
    }

    protected Response responseOfGone(final String reason) {
        return Response.status(Status.GONE).header("isis-reason", reason).build();
    }

    protected Response responseOfBadRequest(final Consent consent) {
        return responseOfBadRequest(consent.getReason());
    }

    protected Response responseOfNoContent(final String reason) {
        return Response.status(Status.NO_CONTENT).header("isis-reason", reason).build();
    }

    protected Response responseOfBadRequest(final String reason) {
        return Response.status(Status.BAD_REQUEST).header("isis-reason", reason).build();
    }

    protected Response responseOfNotFound(final IllegalArgumentException e) {
        return responseOfNotFound(e.getMessage());
    }

    protected Response responseOfNotFound(final String reason) {
        return Response.status(Status.NOT_FOUND).header("isis-reason", reason).build();
    }

    protected Response responseOfInternalServerError(final Exception ex) {
        return responseOfInternalServerError(ex.getMessage());
    }

    protected Response responseOfInternalServerError(final String reason) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).header("isis-reason", reason).build();
    }

    // //////////////////////////////////////////////////////////////
    // Dependencies (from singletons)
    // //////////////////////////////////////////////////////////////

    protected AuthenticationSession getSession() {
        return IsisContext.getAuthenticationSession();
    }

    private SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private OidGenerator getOidGenerator() {
        return getPersistenceSession().getOidGenerator();
    }

    private OidStringifier getOidStringifier() {
        return getOidGenerator().getOidStringifier();
    }

    // //////////////////////////////////////////////////////////////
    // Dependencies (injected via @Context)
    // //////////////////////////////////////////////////////////////

    protected HttpServletRequest getServletRequest() {
        return getResourceContext().getHttpServletRequest();
    }

}
