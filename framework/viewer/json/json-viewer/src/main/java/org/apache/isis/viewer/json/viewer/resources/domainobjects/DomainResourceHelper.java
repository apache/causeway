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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.RendererFactory;
import org.apache.isis.viewer.json.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract.Caching;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.JsonValueEncoder.ExpectedStringRepresentingValueException;
import org.apache.isis.viewer.json.viewer.util.OidUtils;
import org.apache.isis.viewer.json.viewer.util.UrlDecoderUtils;
import org.apache.isis.viewer.json.viewer.util.UrlParserUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

public class DomainResourceHelper {
    
    private final ResourceContext resourceContext;

    // TODO: inject somehow instead
    private final RendererFactoryRegistry rendererFactoryRegistry = RendererFactoryRegistry.instance;

    public DomainResourceHelper(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

    // //////////////////////////////////////////////////////////////
    // propertyDetails
    // //////////////////////////////////////////////////////////////

    Response propertyDetails(
            final ObjectAdapter objectAdapter,
            final String propertyId, 
            final Caching caching) {

        final OneToOneAssociation property = getPropertyThatIsVisibleAndUsable(
                objectAdapter, propertyId, Intent.ACCESS);

        RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.OBJECT_PROPERTY);
        final ObjectPropertyReprRenderer renderer = 
                (ObjectPropertyReprRenderer) factory.newRenderer(resourceContext, null, JsonRepresentation.newMap());
        
        renderer.with(new ObjectAndProperty(objectAdapter, property));
        
        return ResourceAbstract.responseOfOk(caching, renderer).build();
    }

    // //////////////////////////////////////////////////////////////
    // action Prompt
    // //////////////////////////////////////////////////////////////

    Response actionPrompt(final String actionId, final ObjectAdapter serviceAdapter) {
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                serviceAdapter, actionId, Intent.ACCESS);

        RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.OBJECT_ACTION);
        final ObjectActionReprRenderer renderer = 
                (ObjectActionReprRenderer) factory.newRenderer(resourceContext, null, JsonRepresentation.newMap());
        
        renderer.with(new ObjectAndAction(serviceAdapter, action))
                .withSelf()
                .withMutatorsIfEnabled();

        return ResourceAbstract.responseOfOk(Caching.NONE, renderer).build();
    }

    
    // //////////////////////////////////////////////////////////////
    // invoke action
    // //////////////////////////////////////////////////////////////

    enum Intent {
        ACCESS, MUTATE;

        public boolean isMutate() {
            return this == MUTATE;
        }
    }

    Response invokeActionQueryOnly(
            final ObjectAdapter objectAdapter, 
            final String actionId, 
            final String arguments) {
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                objectAdapter, actionId, Intent.ACCESS);

        final ActionSemantics actionSemantics = ActionSemantics.determine(resourceContext, action);
        if(!actionSemantics.isQueryOnly()) {
            throw JsonApplicationException.create(HttpStatusCode.METHOD_NOT_ALLOWED,
                    "Method not allowed; action '%s' is not query only", action.getId());
        }

        List<ObjectAdapter> argumentAdapters;
        try {
            argumentAdapters = argumentAdaptersFor(action, arguments);
        } catch (IOException e) {
            throw JsonApplicationException.create(HttpStatusCode.BAD_REQUEST,
                    "Action '%s' has query arguments that cannot be parsed as JSON", e, action.getId());
        }

        int numParameters = action.getParameterCount();
        int argSize = argumentAdapters.size();
        if (argSize != numParameters) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST,
                    "Action '%s' has %d parameters but received %d arguments",
                    numParameters, argSize, action.getId());
        }
        
        JsonRepresentation representationWithSelf = representationWithSelfFor(objectAdapter, action, arguments);
        return invokeActionUsingAdapters(objectAdapter, action, argumentAdapters, representationWithSelf);
    }


    Response invokeActionIdempotent(
            final ObjectAdapter objectAdapter, 
            final String actionId, 
            final InputStream body) {
        
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                objectAdapter, actionId, Intent.MUTATE);

        final ActionSemantics actionSemantics = ActionSemantics.determine(resourceContext, action);
        if(!actionSemantics.isIdempotent()) {
            throw JsonApplicationException.create(
                    HttpStatusCode.METHOD_NOT_ALLOWED,
                    "Method not allowed; action '%s' is not idempotent", action.getId());
        }
        String bodyAsString = asStringUtf8(body);
        final JsonRepresentation arguments = readBodyAsMap(bodyAsString);

        List<ObjectAdapter> argumentAdapters = parseArguments(action, arguments);
        JsonRepresentation representationWithSelf = representationWithSelfFor(objectAdapter, action, arguments);

        return invokeActionUsingAdapters(objectAdapter, action,
                argumentAdapters, representationWithSelf);
    }

    Response invokeAction(final ObjectAdapter objectAdapter, final String actionId, final InputStream body) {
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                objectAdapter, actionId, Intent.MUTATE);
        
        String bodyAsString = asStringUtf8(body);
        final JsonRepresentation arguments = readBodyAsMap(bodyAsString);

        JsonRepresentation representationWithSelf = representationWithSelfFor(objectAdapter, action, arguments);

        List<ObjectAdapter> argumentAdapters = parseArguments(action, arguments);
        return invokeActionUsingAdapters(objectAdapter, action,
                argumentAdapters, representationWithSelf);
    }

    Response invokeActionUsingAdapters(
        final ObjectAdapter objectAdapter,
        final ObjectAction action,
        final List<ObjectAdapter> argAdapters, 
        final JsonRepresentation representation) {
        
        // validate
        List<ObjectActionParameter> parameters = action.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            ObjectActionParameter parameter = parameters.get(i);
            ObjectAdapter paramAdapter = argAdapters.get(i);
            if (paramAdapter.getSpecification().containsFacet(ValueFacet.class)) {
                Object arg = paramAdapter.getObject();
                String reasonNotValid = parameter.isValid(objectAdapter, arg);
                if (reasonNotValid != null) {
                    throw JsonApplicationException.create(HttpStatusCode.NOT_ACCEPTABLE, reasonNotValid);
                }
            }
        }
        ObjectAdapter[] argArray = argAdapters.toArray(new ObjectAdapter[0]);
        Consent consent = action.isProposedArgumentSetValid(objectAdapter,
                argArray);
        if (consent.isVetoed()) {
            throw JsonApplicationException.create(HttpStatusCode.NOT_ACCEPTABLE, consent.getReason());
        }

        // invoke
        final ObjectAdapter returnedAdapter = action.execute(objectAdapter, argArray);

        // response
        if (returnedAdapter == null) {
            return ResourceAbstract.responseOfNoContent(objectAdapter.getVersion()).build();
        }

        final CollectionFacet collectionFacet = returnedAdapter.getSpecification().getFacet(CollectionFacet.class);
        if (collectionFacet != null) {
            final Collection<ObjectAdapter> collectionAdapters = collectionFacet
                    .collection(returnedAdapter);

            final RendererFactory factory = rendererFactoryRegistry.find(RepresentationType.LIST);
            final ListReprRenderer renderer = (ListReprRenderer) factory.newRenderer(resourceContext, null, representation);
            renderer.with(collectionAdapters);
            
            return ResourceAbstract.responseOfOk(Caching.NONE, renderer).build();
        }

        
        final EncodableFacet encodableFacet = returnedAdapter.getSpecification().getFacet(EncodableFacet.class);
        if(encodableFacet != null) {
            
            final RendererFactory factory = rendererFactoryRegistry.find(RepresentationType.SCALAR_VALUE);

            ScalarValueReprRenderer renderer = (ScalarValueReprRenderer) factory.newRenderer(resourceContext, null, representation);
            renderer.with(objectAdapter);
            return ResourceAbstract.responseOfOk(Caching.NONE, renderer).build();
        }

        final RendererFactory factory = rendererFactoryRegistry.find(RepresentationType.DOMAIN_OBJECT);
        final DomainObjectReprRenderer renderer = (DomainObjectReprRenderer) factory.newRenderer(resourceContext, null, representation);
        renderer.with(returnedAdapter);
        
        ResponseBuilder respBuilder = ResourceAbstract.responseOfOk(Caching.NONE, renderer);
        
        Version version = returnedAdapter.getVersion();
        if (version != null && version.getTime() != null) {
            respBuilder.tag(""+version.getTime());
        }
        return respBuilder.build();
    }



    private JsonRepresentation representationWithSelfFor(final ObjectAdapter objectAdapter, final ObjectAction action, final String queryArgs) {
        JsonRepresentation representationWithSelf = representationWithSelfFor(objectAdapter, action);
        final String href = representationWithSelf.getString("self.href");
        representationWithSelf.mapPut("self.href", href + "?" + queryArgs);
        return representationWithSelf;
    }

    private JsonRepresentation representationWithSelfFor(final ObjectAdapter objectAdapter, final ObjectAction action, final JsonRepresentation bodyArgs) {
        JsonRepresentation representationWithSelf = representationWithSelfFor(objectAdapter, action);
        final Link selfLink = representationWithSelf.getLink("self");
        selfLink.mapPut("args", bodyArgs);
        return representationWithSelf;
    }
    
    private JsonRepresentation representationWithSelfFor(final ObjectAdapter objectAdapter, final ObjectAction action) {
        JsonRepresentation representation = JsonRepresentation.newMap();
        String oid = OidUtils.getOidStr(resourceContext, objectAdapter);
        // TODO: review; can't be more specific with the media type because we don't have a media type for action/invoke
        final JsonRepresentation repBuilder = 
                LinkBuilder.newBuilder(resourceContext, "self", MediaType.APPLICATION_JSON_TYPE, "objects/%s/actions/%s/invoke", oid, action.getId()).build();
        representation.mapPut("self", repBuilder);
        return representation;
    }


    private List<ObjectAdapter> argumentAdaptersFor(ObjectAction action,
        String arguments) throws JsonParseException, JsonMappingException, IOException {

        List<ObjectAdapter> argumentAdapters = Lists.newArrayList();
        // List<ObjectActionParameter> parameters = action.getParameters();
        // for (int i = 0; i < parameters.size(); i++) {
        // ObjectActionParameter parameter = parameters.get(i);
        // ObjectSpecification paramSpc = parameter.getSpecification();
        // String argument = arguments.get(i);
        // argumentAdapters.add(objectAdapterFor(paramSpc, argument));
        // }
        //
        return argumentAdapters;
    }

    
    /**
     * 
     * @param resourceContext
     * @param objectSpec
     *            - the {@link ObjectSpecification} to interpret the object as.
     * @param node
     *            - expected to be either a String or a Map (ie from within a
     *            List, built by parsing a JSON structure).
     */
    private static ObjectAdapter objectAdapterFor(
            final ResourceContext resourceContext, 
            final ObjectSpecification objectSpec, 
            final JsonRepresentation representation) {
        
        // value (encodable)
        if (objectSpec.isEncodeable()) {
            return new JsonValueEncoder().asAdapter(objectSpec, representation);
        }

        // reference
        if(!representation.isLink()) {
            throw new ExpectedMapRepresentingLinkException();
        }
        JsonRepresentation argLink = representation.asLink();
        String oidFromHref = UrlParserUtils.oidFromLink(argLink);

        final ObjectAdapter objectAdapter = OidUtils.getObjectAdapter(
                resourceContext, oidFromHref);

        if (objectAdapter == null) {
            throw new UnknownOidException(oidFromHref);
        }
        return objectAdapter;
    }

    /**
     * Similar to {@link #objectAdapterFor(ResourceContext, ObjectSpecification, Object)},
     * however the object being interpreted is a String holding URL encoded JSON
     * (rather than having already been parsed into a Map representation).
     * 
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    private ObjectAdapter objectAdapterFor(
            final ObjectSpecification spec,
            final String urlEncodedJson) throws JsonParseException, JsonMappingException, IOException {

        final String json = UrlDecoderUtils.urlDecode(urlEncodedJson);
        JsonRepresentation representation = JsonMapper.instance().read(json);
        return objectAdapterFor(resourceContext, spec, representation);
    }

    private static class ExpectedMapRepresentingLinkException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
    }

    private static class UnknownOidException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        public UnknownOidException(String oid) {
            super(UrlDecoderUtils.urlDecode(oid));
        }
    }

    // ///////////////////////////////////////////////////////////////////
    // get{MemberType}ThatIsVisibleAndUsable
    // ///////////////////////////////////////////////////////////////////

    protected OneToOneAssociation getPropertyThatIsVisibleAndUsable(
        final ObjectAdapter objectAdapter,
        final String propertyId, final Intent intent) {
        ObjectAssociation association = objectAdapter.getSpecification()
                .getAssociation(propertyId);
        if (association == null || !association.isOneToOneAssociation()) {
            throwNotFoundException(propertyId, MemberType.OBJECT_PROPERTY);
        }
        OneToOneAssociation property = (OneToOneAssociation) association;
        return memberThatIsVisibleAndUsable(objectAdapter, property, MemberType.OBJECT_PROPERTY,
                intent);
    }

    protected OneToManyAssociation getCollectionThatIsVisibleAndUsable(
        final ObjectAdapter objectAdapter,
        final String collectionId,
        final Intent intent) {

        ObjectAssociation association = objectAdapter.getSpecification()
                .getAssociation(collectionId);
        if (association == null || !association.isOneToManyAssociation()) {
            throwNotFoundException(collectionId, MemberType.OBJECT_COLLECTION);
        }
        OneToManyAssociation collection = (OneToManyAssociation) association;
        return memberThatIsVisibleAndUsable(objectAdapter, collection, MemberType.OBJECT_COLLECTION,
                intent);
    }

    protected ObjectAction getObjectActionThatIsVisibleAndUsable(
        final ObjectAdapter objectAdapter,
        final String actionId,
        Intent intent) {

        ObjectAction action = objectAdapter.getSpecification().getObjectAction(actionId);
        if (action == null) {
            throwNotFoundException(actionId, MemberType.OBJECT_ACTION);
        }
        
        return memberThatIsVisibleAndUsable(objectAdapter, action, MemberType.OBJECT_ACTION, intent);
    }

    protected <T extends ObjectMember> T memberThatIsVisibleAndUsable(
        final ObjectAdapter objectAdapter,
        T objectMember, final MemberType memberType,
        final Intent intent) {
        String memberId = objectMember.getId();
        AuthenticationSession authenticationSession = resourceContext.getAuthenticationSession();
        if (objectMember.isVisible(authenticationSession, objectAdapter).isVetoed()) {
            throwNotFoundException(memberId, memberType);
        }
        if (intent.isMutate()) {
            Consent usable = objectMember.isUsable(authenticationSession, objectAdapter);
            if (usable.isVetoed()) {
                String memberTypeStr = memberType.name().toLowerCase();
                throw JsonApplicationException.create(
                        HttpStatusCode.NOT_ACCEPTABLE,
                        "%s is not usable: '%s' (%s)",
                        memberTypeStr, memberId, usable.getReason());
            }
        }
        return objectMember;
    }

    protected static void throwNotFoundException(
        final String memberId, MemberType memberType) {
        String memberTypeStr = memberType.name().toLowerCase();
        throw JsonApplicationException.create(
                HttpStatusCode.NOT_FOUND,
                "%s '%s' either does not exist or is not visible",
                memberTypeStr, memberId);
    }


    
    
    // ///////////////////////////////////////////////////////////////////
    // parseBody
    // ///////////////////////////////////////////////////////////////////

    /**
     * 
     * @param objectSpec
     * @param bodyAsString - as per {@link #asStringUtf8(InputStream)}
     * @return
     */
    ObjectAdapter parseBodyAsMapWithSingleValue(
            final ObjectSpecification objectSpec, 
            final String bodyAsString) {
        JsonRepresentation arguments = readBodyAsMap(bodyAsString);
        
        JsonRepresentation representation = arguments.getRepresentation("value");
        if (arguments.size() != 1 || representation == null) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST,
                    "Body should be a map with a single key 'value' whose value represents an instance of type '%s'",
                    resourceFor(objectSpec));
        }

        ObjectAdapter proposedValueAdapter = objectAdapterFor(resourceContext, objectSpec, arguments);
        return proposedValueAdapter;
    }

    
    /**
     * @param action
     * @param bodyAsString - as per {@link #asStringUtf8(InputStream)}
     */
    private List<ObjectAdapter> parseBody(
            final ObjectAction action, 
            final String bodyAsString) {
        
        final JsonRepresentation arguments = readBodyAsMap(bodyAsString);
        return parseArguments(action, arguments);
    }



    private List<ObjectAdapter> parseArguments(
            final ObjectAction action, 
            final JsonRepresentation arguments) {
        final List<JsonRepresentation> argList = argListFor(action, arguments);
        
        final List<ObjectAdapter> argAdapters = Lists.newArrayList();
        final List<ObjectActionParameter> parameters = action.getParameters();
        for (int i = 0; i < argList.size(); i++) {
            final String paramName = parameters.get(i).getName();
            final JsonRepresentation arg = argList.get(i);
            final ObjectSpecification paramSpec = parameters.get(i).getSpecification();
            try {
                final ObjectAdapter objectAdapter = objectAdapterFor(resourceContext, paramSpec, arg);
                argAdapters.add(objectAdapter);
            } catch (ExpectedStringRepresentingValueException e) {
                throw JsonApplicationException.create(
                        HttpStatusCode.BAD_REQUEST,
                        "Action '%s', argument %s should be a URL encoded string representing a value of type %s",
                        action.getId(), paramName, resourceFor(paramSpec));
            } catch (ExpectedMapRepresentingLinkException e) {
                throw JsonApplicationException.create(
                        HttpStatusCode.BAD_REQUEST,
                        "Action '%s', argument %s should be a map representing a link to reference of type %s",
                        action.getId(), paramName, resourceFor(paramSpec));
            }
        }
        return argAdapters;
    }


    private List<JsonRepresentation> argListFor(final ObjectAction action, JsonRepresentation arguments) {
        List<JsonRepresentation> argList = Lists.newArrayList();
        
        int numParameters = action.getParameterCount();
        int numArguments = arguments.size();
        if (numArguments != numParameters) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST,
                    "Action '%s' has %d parameters but received %d arguments in body",
                    action.getId(), numParameters, numArguments);
        }
        final List<ObjectActionParameter> parameters = action.getParameters();
        for (ObjectActionParameter param : parameters) {
            final String paramName = param.getName();
            final JsonRepresentation argRepr = arguments.getRepresentation(paramName);
            if(argRepr == null) {
                throw JsonApplicationException.create(
                        HttpStatusCode.BAD_REQUEST,
                        "Action '%s', no argument found for parameter '%s'",
                        action.getId(), paramName);
            }
            argList.add(argRepr);
        }
        return argList;
    }

    private JsonRepresentation readBodyAsMap(String bodyAsString) {
        try {
            final JsonRepresentation jsonRepr = JsonMapper.instance().read(bodyAsString);
            if(!jsonRepr.isMap()) {
                throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST,
                    "could not read body as a JSON map");
            }
            return jsonRepr;
        } catch (JsonParseException e) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST, e,
                    "could not parse body");
        } catch (JsonMappingException e) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST, e,
                    "could not read body as JSON");
        } catch (IOException e) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST, e,
                    "could not parse body");
        }
    }

    static String asStringUtf8(final InputStream body) {
        try {
            byte[] byteArray = ByteStreams.toByteArray(body);
            return new String(byteArray, Charsets.UTF_8);
        } catch (IOException e) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST, e,
                    "could not read body");
        }
    }


    // //////////////////////////////////////////////////////////////
    // misc
    // //////////////////////////////////////////////////////////////

    private static String resourceFor(ObjectSpecification objectSpec) {
        // TODO: should return a string in the form
        // http://localhost:8080/types/xxx
        return objectSpec.getFullIdentifier();
    }


}
