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
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.consent.Consent;
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
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.RendererFactory;
import org.apache.isis.viewer.json.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract.Caching;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.DomainResourceHelper.MemberMode;
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
    private ObjectAdapterLinkTo adapterLinkTo;

    private final ObjectAdapter objectAdapter;


    public DomainResourceHelper(ResourceContext resourceContext, ObjectAdapter objectAdapter) {
        this.resourceContext = resourceContext;
        this.objectAdapter = objectAdapter;
        using(new DomainObjectLinkTo());
    }

    public DomainResourceHelper using(ObjectAdapterLinkTo linkTo) {
        adapterLinkTo = linkTo;
        adapterLinkTo.usingResourceContext(resourceContext).with(objectAdapter);
        return this;
    }

    // //////////////////////////////////////////////////////////////
    // propertyDetails
    // //////////////////////////////////////////////////////////////

    public enum MemberMode {
        NOT_MUTATING {
            @Override
            public void apply(AbstractObjectMemberReprRenderer<?,?> renderer) {
                renderer.asStandalone();
            }
        },
        MUTATING {
            @Override
            public void apply(AbstractObjectMemberReprRenderer<?,?> renderer) {
                renderer.asMutated();
            }
        };

        public abstract void apply(AbstractObjectMemberReprRenderer<?,?> renderer);
    }
    
    Response propertyDetails(
            final ObjectAdapter objectAdapter,
            final String propertyId, 
            final MemberMode memberMode, 
            final Caching caching) {

        final OneToOneAssociation property = getPropertyThatIsVisibleAndUsable(
                propertyId, Intent.ACCESS);

        RendererFactory factory = getRendererFactoryRegistry().find(RepresentationType.OBJECT_PROPERTY);
        final ObjectPropertyReprRenderer renderer = 
                (ObjectPropertyReprRenderer) factory.newRenderer(resourceContext, null, JsonRepresentation.newMap());
        
        renderer.with(new ObjectAndProperty(objectAdapter, property))
            .usingLinkTo(adapterLinkTo);
        
        memberMode.apply(renderer);
        
        return ResourceAbstract.responseOfOk(renderer, caching).build();
    }

    // //////////////////////////////////////////////////////////////
    // collectionDetails
    // //////////////////////////////////////////////////////////////

    Response collectionDetails(
            final ObjectAdapter objectAdapter, 
            final String collectionId, 
            final MemberMode memberMode, 
            final Caching caching) {
        
        final OneToManyAssociation collection = getCollectionThatIsVisibleAndUsable(
                collectionId, Intent.ACCESS);
        
        RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.OBJECT_COLLECTION);
        final ObjectCollectionReprRenderer renderer = 
                (ObjectCollectionReprRenderer) factory.newRenderer(resourceContext, null, JsonRepresentation.newMap());

        renderer.with(new ObjectAndCollection(objectAdapter, collection))
            .usingLinkTo(adapterLinkTo);
        
        memberMode.apply(renderer);
        
        return ResourceAbstract.responseOfOk(renderer, caching).build();
    }


    // //////////////////////////////////////////////////////////////
    // action Prompt
    // //////////////////////////////////////////////////////////////

    Response actionPrompt(final String actionId) {
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                actionId, Intent.ACCESS);

        RendererFactory factory = getRendererFactoryRegistry().find(RepresentationType.OBJECT_ACTION);
        final ObjectActionReprRenderer renderer = 
                (ObjectActionReprRenderer) factory.newRenderer(resourceContext, null, JsonRepresentation.newMap());
        
        renderer.with(new ObjectAndAction(objectAdapter, action))
                .usingLinkTo(adapterLinkTo)
                .asStandalone();

        return ResourceAbstract.responseOfOk(renderer, Caching.NONE).build();
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
            final String actionId, 
            final String argumentsQueryString) {
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                actionId, Intent.ACCESS);

        final ActionSemantics actionSemantics = ActionSemantics.determine(resourceContext, action);
        if(!actionSemantics.isQueryOnly()) {
            throw JsonApplicationException.create(HttpStatusCode.METHOD_NOT_ALLOWED,
                    "Method not allowed; action '%s' is not query only", action.getId());
        }

        JsonRepresentation arguments = parseQueryString(action, argumentsQueryString);
        
        return invokeActionUsingAdapters(action, arguments);
    }


    static JsonRepresentation parseQueryString(ObjectAction action, String argumentsQueryString) {
        return QueryStringUtil.parseQueryString(argumentsQueryString, "Action", action.getId());
    }

    Response invokeActionIdempotent(
            final String actionId, 
            final InputStream body) {
        
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                actionId, Intent.MUTATE);

        final ActionSemantics actionSemantics = ActionSemantics.determine(resourceContext, action);
        if(!actionSemantics.isIdempotent()) {
            throw JsonApplicationException.create(
                    HttpStatusCode.METHOD_NOT_ALLOWED,
                    "Method not allowed; action '%s' is not idempotent", action.getId());
        }
        String bodyAsString = asStringUtf8(body);
        final JsonRepresentation arguments = readBodyAsMap(bodyAsString);

        return invokeActionUsingAdapters(action, arguments);
    }

    Response invokeAction(final String actionId, final InputStream body) {
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                actionId, Intent.MUTATE);
        
        String bodyAsString = asStringUtf8(body);
        final JsonRepresentation arguments = readBodyAsMap(bodyAsString);

        return invokeActionUsingAdapters(action, arguments);
    }

    Response invokeActionUsingAdapters(
        final ObjectAction action,
        final JsonRepresentation arguments) {
        
        List<ObjectAdapter> argAdapters = parseArguments(action, arguments);
        
        // validate individual args
        List<ObjectActionParameter> parameters = action.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            ObjectActionParameter parameter = parameters.get(i);
            ObjectAdapter argAdapter = argAdapters.get(i);
            if(argAdapter == null) {
                // can only happen if this is an optional parameter; nothing to do
                continue;
            } 
            if (argAdapter.getSpecification().containsFacet(ValueFacet.class)) {
                Object arg = argAdapter.getObject();
                String reasonNotValid = parameter.isValid(objectAdapter, arg);
                if (reasonNotValid != null) {
                    throw JsonApplicationException.create(HttpStatusCode.NOT_ACCEPTABLE, reasonNotValid);
                }
            }
        }
        
        // validate all args
        ObjectAdapter[] argArray = argAdapters.toArray(new ObjectAdapter[0]);
        Consent consent = action.isProposedArgumentSetValid(objectAdapter,
                argArray);
        if (consent.isVetoed()) {
            throw JsonApplicationException.create(HttpStatusCode.NOT_ACCEPTABLE, consent.getReason());
        }

        // invoke
        final ObjectAdapter returnedAdapter = action.execute(objectAdapter, argArray);

        // response (void)
        RendererFactory factory = getRendererFactoryRegistry().find(RepresentationType.ACTION_RESULT);
        final ActionResultReprRenderer renderer = 
                (ActionResultReprRenderer) factory.newRenderer(resourceContext, null, JsonRepresentation.newMap());
        
        renderer.with(new ObjectAndActionInvocation(objectAdapter, action, arguments, returnedAdapter)).using(adapterLinkTo);
        
        final ResponseBuilder respBuilder = ResourceAbstract.responseOfOk(renderer, Caching.NONE);
        
        Version version = objectAdapter.getVersion();
        ResourceAbstract.addLastModifiedAndETagIfAvailable(respBuilder, version);

        return respBuilder.build();
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

        if(representation == null) {
            return null;
        }
        
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
        if(oidFromHref == null) {
            throw new ExpectedMapRepresentingLinkException();
        }

        ObjectAdapter objectAdapter = OidUtils.getObjectAdapter(
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
    ObjectAdapter objectAdapterFor(
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
        final String propertyId,
        final Intent intent) {
        
        ObjectAssociation association = objectAdapter.getSpecification()
                .getAssociation(propertyId);
        if (association == null || !association.isOneToOneAssociation()) {
            throwNotFoundException(propertyId, MemberType.PROPERTY);
        }
        OneToOneAssociation property = (OneToOneAssociation) association;
        return memberThatIsVisibleAndUsable(property, MemberType.PROPERTY, intent);
    }

    protected OneToManyAssociation getCollectionThatIsVisibleAndUsable(
        final String collectionId,
        final Intent intent) {

        ObjectAssociation association = objectAdapter.getSpecification()
                .getAssociation(collectionId);
        if (association == null || !association.isOneToManyAssociation()) {
            throwNotFoundException(collectionId, MemberType.COLLECTION);
        }
        OneToManyAssociation collection = (OneToManyAssociation) association;
        return memberThatIsVisibleAndUsable(collection, MemberType.COLLECTION, intent);
    }

    protected ObjectAction getObjectActionThatIsVisibleAndUsable(
            final String actionId,
            Intent intent) {

        ObjectAction action = objectAdapter.getSpecification().getObjectAction(actionId);
        if (action == null) {
            throwNotFoundException(actionId, MemberType.ACTION);
        }
        
        return memberThatIsVisibleAndUsable(action, MemberType.ACTION, intent);
    }

    protected <T extends ObjectMember> T memberThatIsVisibleAndUsable(
            T objectMember,
            final MemberType memberType, final Intent intent) {
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

        ObjectAdapter proposedValueAdapter = objectAdapterFor(resourceContext, objectSpec, representation);
        return proposedValueAdapter;
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

        
        // ensure that we have no arguments that are not parameters
        for(Entry<String, JsonRepresentation> arg: arguments.mapIterable()) {
            final String argName = arg.getKey();
            if(action.getParameter(argName) == null) {
                throw JsonApplicationException.create(
                        HttpStatusCode.BAD_REQUEST,
                        "Action '%s' does not have a parameter %s but an argument of that name was provided",
                        action.getId(), argName);
            }
        }

        // ensure that an argument value has been provided for all non-optional parameters 
        final List<ObjectActionParameter> parameters = action.getParameters();
        for (ObjectActionParameter param : parameters) {
            final String paramName = param.getName();
            final JsonRepresentation argRepr = arguments.getRepresentation(paramName);
            if(argRepr == null && !param.isOptional()) {
                throw JsonApplicationException.create(
                        HttpStatusCode.BAD_REQUEST,
                        "Action '%s', no argument found for (mandatory) parameter '%s'",
                        action.getId(), paramName);
            }
            argList.add(argRepr);
        }
        return argList;
    }

    static JsonRepresentation readBodyAsMap(String body) {
        if(body == null) {
            return JsonRepresentation.newMap();
        }
        final String bodyTrimmed = body.trim();
        if(bodyTrimmed.isEmpty()) {
            return JsonRepresentation.newMap();
        }
        try {
            final JsonRepresentation jsonRepr = JsonMapper.instance().read(bodyTrimmed);
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

    
    // //////////////////////////////////////////////////////////////
    // dependencies
    // //////////////////////////////////////////////////////////////


    protected RendererFactoryRegistry getRendererFactoryRegistry() {
        // TODO: yuck
        return RendererFactoryRegistry.instance;
    }


}
