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
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.json.applib.HttpStatusCode;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.RepresentationBuilder;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.DomainObjectRepBuilder;
import org.apache.isis.viewer.json.viewer.util.OidUtils;
import org.apache.isis.viewer.json.viewer.util.UrlDecoderUtils;
import org.apache.isis.viewer.json.viewer.util.UrlParserUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jboss.resteasy.util.HttpHeaderNames;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public abstract class ResourceAbstract {

    protected final static JsonMapper jsonMapper = JsonMapper.instance();

    protected static final CacheControl CACHE_ONE_DAY = new CacheControl();
    protected static final CacheControl CACHE_ONE_HOUR = new CacheControl();
    protected static final CacheControl CACHE_NONE = new CacheControl();
    static {
        CACHE_ONE_DAY.setMaxAge(24*60*60);
        CACHE_ONE_HOUR.setMaxAge(60*60);
        CACHE_NONE.setNoCache(true);
    }


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
        
        List<MediaType> clientMediaTypes = getResourceContext().getHttpHeaders().getAcceptableMediaTypes();
        if(!clientMediaTypes.contains(MediaType.APPLICATION_JSON_TYPE)) {
            throw JsonApplicationException.create(
                    HttpStatusCode.NOT_ACCEPTABLE, 
                    "Client %s header %s does not include %s", 
                    HttpHeaderNames.ACCEPT, clientMediaTypes, MediaType.APPLICATION_JSON_TYPE); 
        }

    }

    protected ResourceContext getResourceContext() {
        return resourceContext;
    }

    
    // //////////////////////////////////////////////////////////////
    // Rendering
    // //////////////////////////////////////////////////////////////

    protected String jsonFrom(RepresentationBuilder<?> builder) {
        JsonRepresentation representation = builder.build();
        return jsonFor(representation);
    }

	protected String jsonFor(
			final Collection<ObjectAdapter> collectionAdapters) {
		return jsonFor(Lists.newArrayList(
            Collections2.transform(collectionAdapters, toObjectSelfRepresentation())));
	}

	protected String jsonFor(
			final ObjectAdapter objectAdapter) {
		return jsonFor(toObjectSelfRepresentation().apply(objectAdapter));
	}


    protected String jsonFor(final Object object) {
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

	private Function<ObjectAdapter, JsonRepresentation> toObjectSelfRepresentation() {
		final ResourceContext representationContext = getResourceContext();
        
        Function<ObjectAdapter, JsonRepresentation> objectSelfRepresentation = 
            Functions.compose(
                DomainObjectRepBuilder.selfOf(), 
                DomainObjectRepBuilder.fromAdapter(representationContext));
		return objectSelfRepresentation;
	}


    // //////////////////////////////////////////////////////////////
    // Isis integration
    // //////////////////////////////////////////////////////////////

    protected ObjectSpecification getSpecification(final String specFullName) {
        return getSpecificationLoader().loadSpecification(specFullName);
    }

    protected ObjectAdapter getObjectAdapter(final String oidEncodedStr) {
        init();

        final ObjectAdapter objectAdapter = OidUtils.getObjectAdapter(oidEncodedStr, getOidStringifier());
        
        if (objectAdapter == null) {
            final String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);
            throw new WebApplicationException(responseOf(HttpStatusCode.NOT_FOUND, "could not determine adapter for OID: '%s'", oidStr));
        }
        return objectAdapter;
    }


    protected String getOidStr(final ObjectAdapter objectAdapter) {
        return OidUtils.getOidStr(objectAdapter, getOidStringifier());
    }



	protected static class ExpectedStringRepresentingValueException extends IllegalArgumentException {
		private static final long serialVersionUID = 1L;
	}
	protected static class ExpectedMapRepresentingReferenceException extends IllegalArgumentException {
		private static final long serialVersionUID = 1L;
	}
	protected static class UnknownOidException extends IllegalArgumentException {
		private static final long serialVersionUID = 1L;
		public UnknownOidException(String oid) {
		    super(UrlDecoderUtils.urlDecode(oid));
		}
	}
	
	/**
	 * 
	 * @param objectSpec - the {@link ObjectSpecification} to interpret the object as.
	 * @param node - expected to be either a String or a Map (ie from within a List, built by parsing a JSON structure). 
	 */
	protected ObjectAdapter objectAdapterFor(ObjectSpecification objectSpec, Object node) {
		// null
		if(node == null) {
			return null;
		}
		
		// value (encodable)
		if(objectSpec.isEncodeable()) {
			EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
			if(!(node instanceof String)) {
				throw new ExpectedStringRepresentingValueException();
			} 
			String argStr = (String) node;
			return encodableFacet.fromEncodedString(argStr);
		}

		// reference
		try {
			JsonRepresentation argLink = (JsonRepresentation) node;
			String oidFromHref = UrlParserUtils.oidFromHref(argLink);
			
			final ObjectAdapter objectAdapter = OidUtils.getObjectAdapter(oidFromHref, getOidStringifier());
			
			if (objectAdapter == null) {
			    throw new UnknownOidException(oidFromHref);
			}
			return objectAdapter;
		} catch (Exception e) {
			throw new ExpectedMapRepresentingReferenceException();
		}
	}

    // //////////////////////////////////////////////////////////////
    // Responses
    // //////////////////////////////////////////////////////////////

    /**
     * Common case.
     */
    public static Response responseOfOk(String jsonEntity) {
        return Response.ok().entity(jsonEntity).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Common case.
     */
    protected static Response responseOfNoContent() {
        return Response.status(HttpStatusCode.NO_CONTENT.getJaxrsStatusType()).build();
    }

    /**
     * Common case.
     */
    protected static Response responseOfUnauthorized(final Consent consent) {
        return responseOf(HttpStatusCode.UNAUTHORIZED, consent.getReason());
    }

    
    protected static Response responseOf(HttpStatusCode httpStatusCode, final String reason, final Object... args) {
        return Response.status(httpStatusCode.getJaxrsStatusType()).header(RestfulResponse.Header.WARNING.getName(), String.format(reason, args)).build();
    }

    


    // //////////////////////////////////////////////////////////////
    // Dependencies (from singletons)
    // //////////////////////////////////////////////////////////////

    protected AuthenticationSession getAuthenticationSession() {
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
