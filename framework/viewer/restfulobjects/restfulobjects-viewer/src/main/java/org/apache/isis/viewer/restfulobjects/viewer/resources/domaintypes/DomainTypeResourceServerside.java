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
package org.apache.isis.viewer.restfulobjects.viewer.resources.domaintypes;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;

import org.jboss.resteasy.annotations.ClientResponseType;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.DomainTypeResource;
import org.apache.isis.viewer.restfulobjects.applib.links.Rel;
import org.apache.isis.viewer.restfulobjects.viewer.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.viewer.representations.RendererFactory;
import org.apache.isis.viewer.restfulobjects.viewer.resources.ResourceAbstract;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects.DomainObjectReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects.DomainResourceHelper;
import org.apache.isis.viewer.restfulobjects.viewer.util.UrlParserUtils;

/**
 * Implementation note: it seems to be necessary to annotate the implementation
 * with {@link Path} rather than the interface (at least under RestEasy 1.0.2
 * and 1.1-RC2).
 */
@Path("/domainTypes")
public class DomainTypeResourceServerside extends ResourceAbstract implements DomainTypeResource {

    @Override
    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_TYPE_LIST })
    public Response domainTypes() {
        final RepresentationType representationType = RepresentationType.TYPE_LIST;
        init(representationType, Where.ANYWHERE);

        final Collection<ObjectSpecification> allSpecifications = getSpecificationLoader().allSpecifications();

        final RendererFactory rendererFactory = rendererFactoryRegistry.find(representationType);

        final TypeListReprRenderer renderer = (TypeListReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(allSpecifications).includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    @GET
    @Path("/{domainType}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPE })
    public Response domainType(@PathParam("domainType") final String domainType) {

        final RepresentationType representationType = RepresentationType.DOMAIN_TYPE;
        init(representationType, Where.ANYWHERE);

        final ObjectSpecification objectSpec = getSpecificationLoader().loadSpecification(domainType);

        final RendererFactory rendererFactory = rendererFactoryRegistry.find(representationType);

        final DomainTypeReprRenderer renderer = (DomainTypeReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(objectSpec).includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    @GET
    @Path("/{domainType}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_PROPERTY_DESCRIPTION })
    public Response typeProperty(@PathParam("domainType") final String domainType, @PathParam("propertyId") final String propertyId) {
        final RepresentationType representationType = RepresentationType.PROPERTY_DESCRIPTION;
        init(representationType, Where.ANYWHERE);

        final ObjectSpecification parentSpec = getSpecificationLoader().loadSpecification(domainType);
        if (parentSpec == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }

        final ObjectMember objectMember = parentSpec.getAssociation(propertyId);
        if (objectMember == null || objectMember.isOneToManyAssociation()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        final OneToOneAssociation property = (OneToOneAssociation) objectMember;

        final RendererFactory rendererFactory = rendererFactoryRegistry.find(representationType);

        final PropertyDescriptionReprRenderer renderer = (PropertyDescriptionReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndProperty(parentSpec, property)).includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    @GET
    @Path("/{domainType}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_COLLECTION_DESCRIPTION })
    public Response typeCollection(@PathParam("domainType") final String domainType, @PathParam("collectionId") final String collectionId) {
        final RepresentationType representationType = RepresentationType.COLLECTION_DESCRIPTION;
        init(representationType, Where.ANYWHERE);

        final ObjectSpecification parentSpec = getSpecificationLoader().loadSpecification(domainType);
        if (parentSpec == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }

        final ObjectMember objectMember = parentSpec.getAssociation(collectionId);
        if (objectMember == null || objectMember.isOneToOneAssociation()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        final OneToManyAssociation collection = (OneToManyAssociation) objectMember;

        final RendererFactory rendererFactory = rendererFactoryRegistry.find(representationType);

        final CollectionDescriptionReprRenderer renderer = (CollectionDescriptionReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndCollection(parentSpec, collection)).includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    @GET
    @Path("/{domainType}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_DESCRIPTION })
    public Response typeAction(@PathParam("domainType") final String domainType, @PathParam("actionId") final String actionId) {
        final RepresentationType representationType = RepresentationType.ACTION_DESCRIPTION;
        init(representationType, Where.ANYWHERE);

        final ObjectSpecification parentSpec = getSpecificationLoader().loadSpecification(domainType);
        if (parentSpec == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }

        final ObjectMember objectMember = parentSpec.getObjectAction(actionId);
        if (objectMember == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        final ObjectAction action = (ObjectAction) objectMember;

        final RendererFactory rendererFactory = rendererFactoryRegistry.find(representationType);

        final ActionDescriptionReprRenderer renderer = (ActionDescriptionReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndAction(parentSpec, action)).includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    @GET
    @Path("/{domainType}/actions/{actionId}/params/{paramName}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_PARAMETER_DESCRIPTION })
    public Response typeActionParam(@PathParam("domainType") final String domainType, @PathParam("actionId") final String actionId, @PathParam("paramName") final String paramName) {
        final RepresentationType representationType = RepresentationType.ACTION_PARAMETER_DESCRIPTION;
        init(representationType, Where.ANYWHERE);

        final ObjectSpecification parentSpec = getSpecificationLoader().loadSpecification(domainType);
        if (parentSpec == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }

        final ObjectMember objectMember = parentSpec.getObjectAction(actionId);
        if (objectMember == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        final ObjectAction parentAction = (ObjectAction) objectMember;

        final ObjectActionParameter actionParam = parentAction.getParameterByName(paramName);

        final RendererFactory rendererFactory = rendererFactoryRegistry.find(representationType);

        final ActionParameterDescriptionReprRenderer renderer = (ActionParameterDescriptionReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndActionParam(parentSpec, actionParam)).includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    // //////////////////////////////////////////////////////////
    // domain type actions
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{domainType}/typeactions/isSubtypeOf/invoke")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response domainTypeIsSubtypeOf(@PathParam("domainType") final String domainType, @QueryParam("supertype") final String superTypeStr, // simple
                                                                                                                                                // style
            @QueryParam("args") final String args // formal style
    ) {

        final RepresentationType representationType = RepresentationType.TYPE_ACTION_RESULT;
        init(Where.ANYWHERE);

        final String supertype = domainTypeFor(superTypeStr, args, "supertype");

        final ObjectSpecification domainTypeSpec = getSpecificationLoader().loadSpecification(domainType);
        final ObjectSpecification supertypeSpec = getSpecificationLoader().loadSpecification(supertype);

        final RendererFactory rendererFactory = rendererFactoryRegistry.find(representationType);
        final TypeActionResultReprRenderer renderer = (TypeActionResultReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());

        final String url = "domainTypes/" + domainTypeSpec.getFullIdentifier() + "/typeactions/isSubtypeOf/invoke";
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.SELF, RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = DomainTypeReprRenderer.argumentsTo(getResourceContext(), "supertype", supertypeSpec);
        final JsonRepresentation selfLink = linkBuilder.withArguments(arguments).build();

        final boolean value = domainTypeSpec.isOfType(supertypeSpec);
        renderer.with(domainTypeSpec).withSelf(selfLink).withValue(value);

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    @GET
    @Path("/{domainType}/typeactions/isSupertypeOf/invoke")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response domainTypeIsSupertypeOf(
            @PathParam("domainType") final String domainType, 
            @QueryParam("subtype") final String subTypeStr, // simple style
            @QueryParam("args") final String args // formal style
            ) {

        final RepresentationType representationType = RepresentationType.TYPE_ACTION_RESULT;
        init(Where.ANYWHERE);

        final String subtype = domainTypeFor(subTypeStr, args, "subtype");

        final ObjectSpecification domainTypeSpec = getSpecificationLoader().loadSpecification(domainType);
        final ObjectSpecification subtypeSpec = getSpecificationLoader().loadSpecification(subtype);

        final RendererFactory rendererFactory = rendererFactoryRegistry.find(representationType);
        final TypeActionResultReprRenderer renderer = (TypeActionResultReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());

        final String url = "domainTypes/" + domainTypeSpec.getFullIdentifier() + "/typeactions/isSupertypeOf/invoke";
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.SELF, RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = DomainTypeReprRenderer.argumentsTo(getResourceContext(), "subtype", subtypeSpec);
        final JsonRepresentation selfLink = linkBuilder.withArguments(arguments).build();

        final boolean value = subtypeSpec.isOfType(domainTypeSpec);
        renderer.with(domainTypeSpec).withSelf(selfLink).withValue(value);

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    @GET
    @Path("/{domainType}/typeactions/newTransientInstance/invoke")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response newTransientInstance(@PathParam("domainType") final String domainTypeStr, @QueryParam("args") final String args) {

        final RepresentationType representationType = RepresentationType.TYPE_ACTION_RESULT;
        init(representationType, Where.ANYWHERE);

        final String domainType = domainTypeFor(domainTypeStr, args, "domainType");

        final ObjectSpecification domainTypeSpec = getSpecificationLoader().loadSpecification(domainType);

        final RendererFactory rendererFactory = rendererFactoryRegistry.find(representationType);
        final TypeActionResultReprRenderer renderer = (TypeActionResultReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());

        final String url = "domainTypes/" + domainTypeSpec.getFullIdentifier() + "/typeactions/newTransientInstance/invoke";
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.SELF, RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation selfLink = linkBuilder.build();

        final RendererFactory domainObjectRendererFactory = rendererFactoryRegistry.find(RepresentationType.DOMAIN_OBJECT);
        final DomainObjectReprRenderer domainObjectRenderer = (DomainObjectReprRenderer) domainObjectRendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        final ObjectAdapter transientInstance = getResourceContext().getPersistenceSession().createTransientInstance(domainTypeSpec);
        domainObjectRenderer.with(transientInstance).includesSelf();

        renderer.with(domainTypeSpec).withSelf(selfLink).withValue(domainObjectRenderer.render());

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    private static String domainTypeFor(final String domainTypeStr, final String argumentsQueryString, final String argsParamName) {
        // simple style; simple return
        if (!Strings.isNullOrEmpty(domainTypeStr)) {
            return domainTypeStr;
        }

        // formal style; must parse from args that has a link with an href to
        // the domain type
        final String href = linkFromFormalArgs(argumentsQueryString, argsParamName);
        return UrlParserUtils.domainTypeFrom(href);
    }

    private static String linkFromFormalArgs(final String argumentsQueryString, final String paramName) {
        final JsonRepresentation arguments = DomainResourceHelper.readQueryStringAsMap(argumentsQueryString);
        if (!arguments.isLink(paramName)) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, "Args should contain a link '%s'", paramName);
        }

        return arguments.getLink(paramName).getHref();
    }

}