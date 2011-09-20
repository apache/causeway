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
import java.util.Map;

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
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.AbstractRepresentationBuilder;
import org.apache.isis.viewer.json.viewer.representations.LinkToBuilder;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract.Caching;
import org.apache.isis.viewer.json.viewer.util.OidUtils;
import org.apache.isis.viewer.json.viewer.util.UrlDecoderUtils;
import org.apache.isis.viewer.json.viewer.util.UrlParserUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

public abstract class DomainResourceAbstract extends ResourceAbstract {

    // //////////////////////////////////////////////////////////////
    // object
    // //////////////////////////////////////////////////////////////

    protected Response object(final ObjectAdapter objectAdapter) {
        ResourceContext resourceContext = getResourceContext();
        final AbstractRepresentationBuilder<?> repBuilder =
                DomainObjectRepBuilder.newBuilder(resourceContext)
                        .withAdapter(objectAdapter);

        ResponseBuilder respBuilder = 
                responseOfOk(RepresentationType.DOMAIN_OBJECT, repBuilder, Caching.NONE);

        Version version = objectAdapter.getVersion();
        if (version != null && version.getTime() != null) {
            respBuilder.lastModified(version.getTime());
        }
        return respBuilder.build();
    }


    
    // //////////////////////////////////////////////////////////////
    // action Prompt
    // //////////////////////////////////////////////////////////////

    protected Response actionPrompt(final String actionId, final ObjectAdapter serviceAdapter) {
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                serviceAdapter, actionId, Intent.ACCESS);

        ObjectActionRepBuilder repBuilder = 
                ObjectActionRepBuilder.newBuilder(getResourceContext(), serviceAdapter, action)
                .withDetailsLink()
                .withMutatorsIfEnabled();
        
        return responseOfOk(RepresentationType.OBJECT_ACTION, repBuilder, Caching.NONE)
                .build();
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

    protected Response invokeActionQueryOnly(final ObjectAdapter objectAdapter, final String actionId, final String arguments) {
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                objectAdapter, actionId, Intent.ACCESS);

        final ActionSemantics actionSemantics = ActionSemantics.determine(getResourceContext(), action);
        if(!actionSemantics.isQueryOnly()) {
            // TODO: reinstate
//            throw JsonApplicationException.create(HttpStatusCode.METHOD_NOT_ALLOWED,
//                    "Method not allowed; action '%s' is not query only", action.getId());
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
        
        JsonRepresentation representationWithSelf = representationWithSelfFor(objectAdapter, action);
        // TODO: append action args to 'self'

        return invokeActionUsingAdapters(objectAdapter, action, argumentAdapters, representationWithSelf);
    }

    protected Response invokeActionIdempotent(final ObjectAdapter objectAdapter, final String actionId, final InputStream arguments) {
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                objectAdapter, actionId, Intent.MUTATE);

        final ActionSemantics actionSemantics = ActionSemantics.determine(getResourceContext(), action);
        if(actionSemantics.isQueryOnlyOrIdempotent()) {
            // TODO: reinstate
//            throw JsonApplicationException.create(
//                    HttpStatusCode.METHOD_NOT_ALLOWED,
//                    "Method not allowed; action '%s' is not idempotent", action.getId());
        }

        List<ObjectAdapter> argumentAdapters = parseBody(action, arguments);
        JsonRepresentation representationWithSelf = representationWithSelfFor(objectAdapter, action);
        // TODO: append action args to 'self'

        return invokeActionUsingAdapters(objectAdapter, action,
                argumentAdapters, representationWithSelf);
    }

    protected Response invokeAction(final ObjectAdapter objectAdapter, final String actionId, final InputStream body) {
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                objectAdapter, actionId, Intent.MUTATE);

        List<ObjectAdapter> argumentAdapters = parseBody(action, body);
        JsonRepresentation representationWithSelf = representationWithSelfFor(objectAdapter, action);
        // TODO: append action args to 'self'

        return invokeActionUsingAdapters(objectAdapter, action,
                argumentAdapters, representationWithSelf);
    }

    protected Response invokeActionUsingAdapters(
        final ObjectAdapter objectAdapter,
        final ObjectAction action,
        final List<ObjectAdapter> argAdapters, JsonRepresentation representationWithSelf) {
        
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
            return responseOfNoContent(objectAdapter.getVersion()).build();
        }

        final CollectionFacet collectionFacet = returnedAdapter.getSpecification().getFacet(CollectionFacet.class);
        if (collectionFacet != null) {
            final Collection<ObjectAdapter> collectionAdapters = collectionFacet
                    .collection(returnedAdapter);
            DomainObjectListRepBuilder repBuilder = DomainObjectListRepBuilder.newBuilder(getResourceContext(), representationWithSelf).withAdapters(collectionAdapters);
            return responseOfOk(RepresentationType.LIST, repBuilder, Caching.NONE).build();
        }

        final EncodableFacet encodableFacet = returnedAdapter.getSpecification().getFacet(EncodableFacet.class);
        if(encodableFacet != null) {
            ScalarRepBuilder repBuilder = ScalarRepBuilder.newBuilder(getResourceContext(), representationWithSelf).withAdapter(objectAdapter);
            return responseOfOk(RepresentationType.SCALAR_VALUE, repBuilder, Caching.NONE).build();
        }

        return object(returnedAdapter);
    }


    private JsonRepresentation representationWithSelfFor(final ObjectAdapter objectAdapter, final ObjectAction action) {
        JsonRepresentation representation = JsonRepresentation.newMap();
        String oid = getOidStr(objectAdapter);
        representation.mapPut("self", LinkToBuilder.newBuilder(getResourceContext(), "self", "objects/%s/actions/%s/invoke", oid, action.getId()).build());
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

    
    // //////////////////////////////////////////////////////////////
    // objectAdapterFor
    // //////////////////////////////////////////////////////////////

    protected ObjectAdapter objectAdapterFor(
        final ObjectAction action, int i,
        ObjectSpecification paramSpec, Object arg) {
        try {
            return objectAdapterFor(paramSpec, arg);
        } catch (ExpectedStringRepresentingValueException e) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST,
                    "Action '%s', argument %d should be a URL encoded string representing a value of type %s",
                    action.getId(), i, resourceFor(paramSpec));
        } catch (ExpectedMapRepresentingReferenceException e) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST,
                    "Action '%s', argument %d should be a map representing a link to reference of type %s",
                    action.getId(), i, resourceFor(paramSpec));
        }
    }

    protected ObjectAdapter objectAdapterFor(
        final ObjectAction action, List<?> arguments, int i) {
        List<ObjectActionParameter> parameters = action.getParameters();

        ObjectSpecification paramSpec = parameters.get(i).getSpecification();
        Object arg = arguments.get(i);

        ObjectAdapter objectAdapter = objectAdapterFor(action, i, paramSpec, arg);
        return objectAdapter;
    }

    /**
     * 
     * @param objectSpec
     *            - the {@link ObjectSpecification} to interpret the object as.
     * @param node
     *            - expected to be either a String or a Map (ie from within a
     *            List, built by parsing a JSON structure).
     */
    protected ObjectAdapter objectAdapterFor(ObjectSpecification objectSpec, Object node) {
        // null
        if (node == null) {
            return null;
        }

        // value (encodable)
        if (objectSpec.isEncodeable()) {
            EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
            if (!(node instanceof String)) {
                throw new ExpectedStringRepresentingValueException();
            }
            String argStr = (String) node;
            return encodableFacet.fromEncodedString(argStr);
        }

        // reference
        try {
            JsonRepresentation argLink = (JsonRepresentation) node;
            String oidFromHref = UrlParserUtils.oidFromHref(argLink);

            final ObjectAdapter objectAdapter = OidUtils.getObjectAdapter(oidFromHref, getResourceContext().getOidStringifier());

            if (objectAdapter == null) {
                throw new UnknownOidException(oidFromHref);
            }
            return objectAdapter;
        } catch (Exception e) {
            throw new ExpectedMapRepresentingReferenceException();
        }
    }

    /**
     * Similar to {@link #objectAdapterFor(ObjectSpecification, Object)},
     * however the object being interpreted is a String holding URL encoded JSON
     * (rather than having already been parsed into a List/Map representation).
     */
    protected ObjectAdapter objectAdapterFor(
        final ObjectSpecification spec,
        final String urlEncodedJson) throws JsonParseException, JsonMappingException, IOException {

        final String json = UrlDecoderUtils.urlDecode(urlEncodedJson);
        if (spec.containsFacet(EncodableFacet.class)) {
            EncodableFacet encodableFacet = spec.getFacet(EncodableFacet.class);
            return encodableFacet.fromEncodedString(json);
        } else {
            Map<String, Object> representation = JsonMapper.instance().readAsMap(json);
            return objectAdapterFor(spec, representation);
        }
    }


    private static class ExpectedStringRepresentingValueException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
    }

    private static class ExpectedMapRepresentingReferenceException extends IllegalArgumentException {
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
        AuthenticationSession authenticationSession = getResourceContext().getAuthenticationSession();
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

    protected ObjectAdapter parseBody(ObjectSpecification objectSpec,
        final InputStream body) {
        List<?> arguments = parseBody(body);
        if (arguments.size() != 1) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST,
                    "Body should contain 1 argument representing a value of type '%s'",
                    resourceFor(objectSpec));
        }

        ObjectAdapter proposedValueAdapter = objectAdapterFor(objectSpec, arguments.get(0));
        return proposedValueAdapter;
    }

    
    protected List<?> parseBody(final InputStream body) {
        try {
            byte[] byteArray = ByteStreams.toByteArray(body);
            String bodyAsString = new String(byteArray, Charsets.UTF_8);

            List<?> arguments = jsonMapper.readAsList(bodyAsString);
            return arguments;
        } catch (JsonParseException e) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST, e,
                    "could not parse body");
        } catch (JsonMappingException e) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST, e,
                    "could not map body to a Map structure");
        } catch (IOException e) {
            throw JsonApplicationException.create(
                    HttpStatusCode.BAD_REQUEST, e,
                    "could not read body");
        }
    }

    private List<ObjectAdapter> parseBody(final ObjectAction action,
            final InputStream body) {
            List<ObjectAdapter> argAdapters = Lists.newArrayList();
            List<?> arguments = parseBody(body);

            int numParameters = action.getParameterCount();
            int numArguments = arguments.size();
            if (numArguments != numParameters) {
                throw JsonApplicationException.create(
                        HttpStatusCode.BAD_REQUEST,
                        "Action '%s' has %d parameters but received %d arguments in body",
                        action.getId(), numParameters, numArguments);
            }

            for (int i = 0; i < numParameters; i++) {
                ObjectAdapter argAdapter = objectAdapterFor(action, arguments, i);
                argAdapters.add(argAdapter);
            }
            return argAdapters;

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
