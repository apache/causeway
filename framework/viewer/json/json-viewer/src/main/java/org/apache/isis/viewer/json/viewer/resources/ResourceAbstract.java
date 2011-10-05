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
package org.apache.isis.viewer.json.viewer.resources;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.util.OidUtils;
import org.apache.isis.viewer.json.viewer.util.UrlDecoderUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

public abstract class ResourceAbstract {

    protected final static JsonMapper jsonMapper = JsonMapper.instance();

    public enum Caching {
        ONE_DAY(24*60*60),
        ONE_HOUR(60*60),
        NONE(0);

        private final CacheControl cacheControl;

        private Caching(int maxAge) {
            this.cacheControl = new CacheControl();
            if(maxAge > 0) {
                cacheControl.setMaxAge(maxAge);
            } else {
                cacheControl.setNoCache(true);
            }
        }
        
        public CacheControl getCacheControl() {
            return cacheControl;
        }
    }

    // nb: SET is excluded; we simply flatten contributed actions.
	public final static ActionType[] ACTION_TYPES = { ActionType.USER, ActionType.DEBUG, ActionType.EXPLORATION };

	// TODO: should inject this instead...
	protected final static RendererFactoryRegistry rendererFactoryRegistry = RendererFactoryRegistry.instance;

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

    

    protected void init(RepresentationType representationType) {
        this.resourceContext =
            new ResourceContext(representationType, httpHeaders, uriInfo, request, httpServletRequest, httpServletResponse, 
                    securityContext, getOidStringifier(), getLocalization(), getAuthenticationSession(), getPersistenceSession(), getAdapterManager());
    }
    
    protected ResourceContext getResourceContext() {
        return resourceContext;
    }

    
    // //////////////////////////////////////////////////////////////
    // Rendering
    // //////////////////////////////////////////////////////////////

    protected static String jsonFor(final Object object) {
        try {
            return jsonMapper.write(object);
        } catch (JsonGenerationException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // //////////////////////////////////////////////////////////////
    // Isis integration
    // //////////////////////////////////////////////////////////////

    protected ObjectSpecification getSpecification(final String specFullName) {
        return getSpecificationLoader().loadSpecification(specFullName);
    }

    protected ObjectAdapter getObjectAdapter(final String oidEncodedStr) {

        final ObjectAdapter objectAdapter = OidUtils.getObjectAdapter(resourceContext, oidEncodedStr);
        
        if (objectAdapter == null) {
            final String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);
            throw JsonApplicationException.create(
                    HttpStatusCode.NOT_FOUND, "could not determine adapter for OID: '%s'", oidStr);
        }
        return objectAdapter;
    }

    protected ObjectAdapter getServiceAdapter(String serviceId) {
        final List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
        for (ObjectAdapter serviceAdapter : serviceAdapters) {
            Object servicePojo = serviceAdapter.getObject();
            String id = ServiceUtil.id(servicePojo);
            if(serviceId.equals(id)) {
                return serviceAdapter;
            }
        }
        throw JsonApplicationException.create(HttpStatusCode.NOT_FOUND, "Could not locate service '%s'", serviceId);
    }


    protected String getOidStr(final ObjectAdapter objectAdapter) {
        return OidUtils.getOidStr(resourceContext, objectAdapter);
    }



    // //////////////////////////////////////////////////////////////
    // Responses
    // //////////////////////////////////////////////////////////////

    public static ResponseBuilder responseOf(HttpStatusCode httpStatusCode) {
        return Response.status(httpStatusCode.getJaxrsStatusType()).type(MediaType.APPLICATION_JSON_TYPE);
    }

    public static ResponseBuilder responseOfNoContent(Version version) {
        return responseOf(HttpStatusCode.NO_CONTENT).lastModified(version.getTime());
    }

    public static ResponseBuilder responseOfOk(RepresentationType representationType, Caching caching, String representation) {
        return responseOf(HttpStatusCode.OK)
                .type(representationType.getMediaType())
                .cacheControl(caching.getCacheControl())
                .entity(representation);
    }

    public static ResponseBuilder responseOfOk(RepresentationType representationType, Caching caching, JsonRepresentation representation) {
        return responseOfOk(representationType, caching, jsonFor(representation));
    }

    public static ResponseBuilder responseOfOk(Caching caching, ReprRenderer<?,?> renderer) {
        RepresentationType representationType = renderer.getRepresentationType();
        return responseOfOk(representationType, caching, renderer.render());
    }


    // //////////////////////////////////////////////////////////////
    // Dependencies (from singletons)
    // //////////////////////////////////////////////////////////////

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }
    
    protected SpecificationLoader getSpecificationLoader() {
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

    protected Localization getLocalization() {
        return IsisContext.getLocalization();
    }

    // //////////////////////////////////////////////////////////////
    // Dependencies (injected via @Context)
    // //////////////////////////////////////////////////////////////

    protected HttpServletRequest getServletRequest() {
        return getResourceContext().getHttpServletRequest();
    }

}
