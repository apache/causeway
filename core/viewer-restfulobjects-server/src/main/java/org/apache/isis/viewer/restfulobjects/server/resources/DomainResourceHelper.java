
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
package org.apache.isis.viewer.restfulobjects.server.resources;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonMapper;
import org.apache.isis.viewer.restfulobjects.applib.util.UrlEncodingUtils;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.AbstractObjectMemberReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ActionResultReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ActionResultReprRenderer.SelfLink;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.JsonValueEncoder;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.MemberType;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectActionReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAdapterLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndAction;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndCollection;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndProperty;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectCollectionReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectPropertyReprRenderer;
import org.apache.isis.viewer.restfulobjects.server.ResourceContext;
import org.apache.isis.viewer.restfulobjects.server.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.server.resources.ResourceAbstract.Caching;
import org.apache.isis.viewer.restfulobjects.server.util.OidUtils;
import org.apache.isis.viewer.restfulobjects.server.util.UrlDecoderUtils;
import org.apache.isis.viewer.restfulobjects.server.util.UrlParserUtils;

public final class DomainResourceHelper {

    private static final DateFormat ETAG_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final RendererContext resourceContext;
    private ObjectAdapterLinkTo adapterLinkTo;

    private final ObjectAdapter objectAdapter;

    public DomainResourceHelper(final RendererContext resourceContext, final ObjectAdapter objectAdapter) {
        this.resourceContext = resourceContext;
        this.objectAdapter = objectAdapter;
        using(new DomainObjectLinkTo());
    }

    public DomainResourceHelper using(final ObjectAdapterLinkTo linkTo) {
        adapterLinkTo = linkTo;
        adapterLinkTo.usingUrlBase(resourceContext).with(objectAdapter);
        return this;
    }

    // //////////////////////////////////////////////////////////////
    // multiple properties (persist or multi-property update)
    // //////////////////////////////////////////////////////////////

    static boolean copyOverProperties(final RendererContext resourceContext, final ObjectAdapter objectAdapter, final JsonRepresentation propertiesList) {
        final ObjectSpecification objectSpec = objectAdapter.getSpecification();
        final List<ObjectAssociation> properties = objectSpec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.PROPERTIES);
        boolean allOk = true;

        for (final ObjectAssociation association : properties) {
            final OneToOneAssociation property = (OneToOneAssociation) association;
            final ObjectSpecification propertySpec = property.getSpecification();
            final String id = property.getId();
            final JsonRepresentation propertyRepr = propertiesList.getRepresentation(id);
            final Consent visibility = property.isVisible(resourceContext.getAuthenticationSession() , objectAdapter, resourceContext.getWhere());
            final Consent usability = property.isUsable(resourceContext.getAuthenticationSession() , objectAdapter, resourceContext.getWhere());

            final boolean invisible = visibility.isVetoed();
            final boolean disabled = usability.isVetoed();
            final boolean valueProvided = propertyRepr != null;

            if(!valueProvided) {
                
                // no value provided
                
                if(invisible || disabled) {
                    // that's ok, indeed expected
                    continue; 
                }
                if (!property.isMandatory()) {
                    // optional, so also not a problem
                    continue;
                }

                // otherwise, is an error.
                final String invalidReason = propertiesList.getString("x-ro-invalidReason");
                if(invalidReason != null) {
                    propertiesList.mapPut("x-ro-invalidReason", invalidReason + "; " + property.getName());
                } else {
                    propertiesList.mapPut("x-ro-invalidReason", "Mandatory field(s) missing: " + property.getName());
                }
                allOk = false;
                continue;
                
            } else {
                
                // value has been provided
                if (invisible) {
                    // silently ignore; don't want to acknowledge the existence of this property to the caller
                    continue;
                }
                if (disabled) {
                    // not allowed to update
                    propertyRepr.mapPut("invalidReason", usability.getReason());
                    allOk = false;
                    continue;
                }
                
                // ok, we have a value, and the property is not invisible, and is not disabled
                final ObjectAdapter valueAdapter;
                try {
                    valueAdapter = objectAdapterFor(resourceContext, propertySpec, propertyRepr);
                } catch(IllegalArgumentException ex) {
                    propertyRepr.mapPut("invalidReason", ex.getMessage());
                    allOk = false;
                    continue;
                }
                // check if the proposed value is valid 
                final Consent validity = property.isAssociationValid(objectAdapter, valueAdapter);
                if (validity.isAllowed()) {
                    try {
                        property.set(objectAdapter, valueAdapter);
                    } catch (final IllegalArgumentException ex) {
                        propertyRepr.mapPut("invalidReason", ex.getMessage());
                        allOk = false;
                    }
                } else {
                    propertyRepr.mapPut("invalidReason", validity.getReason());
                    allOk = false;
                }
            }
            
        }

        return allOk;
    }

    // //////////////////////////////////////////////////////////////
    // propertyDetails
    // //////////////////////////////////////////////////////////////

    public Response objectRepresentation() {
        final DomainObjectReprRenderer renderer = new DomainObjectReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.with(objectAdapter).includesSelf();

        final ResponseBuilder respBuilder = ResourceAbstract.responseOfOk(renderer, Caching.NONE);

        final Version version = objectAdapter.getVersion();
        if (version != null && version.getTime() != null) {
            respBuilder.tag(ETAG_FORMAT.format(version.getTime()));
        }
        return respBuilder.build();
    }

    // //////////////////////////////////////////////////////////////
    // propertyDetails
    // //////////////////////////////////////////////////////////////

    public enum MemberMode {
        NOT_MUTATING {
            @Override
            public void apply(final AbstractObjectMemberReprRenderer<?, ?> renderer) {
                renderer.asStandalone();
            }
        },
        MUTATING {
            @Override
            public void apply(final AbstractObjectMemberReprRenderer<?, ?> renderer) {
                renderer.asMutated();
            }
        };

        public abstract void apply(AbstractObjectMemberReprRenderer<?, ?> renderer);
    }

    Response propertyDetails(final String propertyId, final MemberMode memberMode, final Caching caching, Where where) {

        final OneToOneAssociation property = getPropertyThatIsVisibleForIntent(propertyId, Intent.ACCESS, where);

        final ObjectPropertyReprRenderer renderer = new ObjectPropertyReprRenderer(resourceContext, null, null, JsonRepresentation.newMap());

        renderer.with(new ObjectAndProperty(objectAdapter, property)).usingLinkTo(adapterLinkTo);

        memberMode.apply(renderer);

        return ResourceAbstract.responseOfOk(renderer, caching).build();
    }

    // //////////////////////////////////////////////////////////////
    // collectionDetails
    // //////////////////////////////////////////////////////////////

    Response collectionDetails(final String collectionId, final MemberMode memberMode, final Caching caching, Where where) {

        final OneToManyAssociation collection = getCollectionThatIsVisibleForIntent(collectionId, Intent.ACCESS, where);

        final ObjectCollectionReprRenderer renderer = new ObjectCollectionReprRenderer(resourceContext, null, null, JsonRepresentation.newMap());

        renderer.with(new ObjectAndCollection(objectAdapter, collection)).usingLinkTo(adapterLinkTo);

        memberMode.apply(renderer);

        return ResourceAbstract.responseOfOk(renderer, caching).build();
    }

    // //////////////////////////////////////////////////////////////
    // action Prompt
    // //////////////////////////////////////////////////////////////

    Response actionPrompt(final String actionId, Where where) {
        final ObjectAction action = getObjectActionThatIsVisibleForIntent(actionId, Intent.ACCESS, where);

        final ObjectActionReprRenderer renderer = new ObjectActionReprRenderer(resourceContext, null, null, JsonRepresentation.newMap());

        renderer.with(new ObjectAndAction(objectAdapter, action)).usingLinkTo(adapterLinkTo).asStandalone();

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

    Response invokeActionQueryOnly(final String actionId, final JsonRepresentation arguments, Where where) {
        final ObjectAction action = getObjectActionThatIsVisibleForIntent(actionId, Intent.MUTATE, where);

        final ActionSemantics.Of actionSemantics = action.getSemantics();
        if (actionSemantics != ActionSemantics.Of.SAFE) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Method not allowed; action '%s' is not query only", action.getId());
        }

        return invokeActionUsingAdapters(action, arguments, SelfLink.INCLUDED);
    }

    Response invokeActionIdempotent(final String actionId, final JsonRepresentation arguments, Where where) {

        final ObjectAction action = getObjectActionThatIsVisibleForIntent(actionId, Intent.MUTATE, where);

        final ActionSemantics.Of actionSemantics = action.getSemantics();
        if (!actionSemantics.isIdempotentInNature()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Method not allowed; action '%s' is not idempotent", action.getId());
        }
        return invokeActionUsingAdapters(action, arguments, SelfLink.EXCLUDED);
    }

    Response invokeAction(final String actionId, final JsonRepresentation arguments, Where where) {
        final ObjectAction action = getObjectActionThatIsVisibleForIntent(actionId, Intent.MUTATE, where);

        return invokeActionUsingAdapters(action, arguments, SelfLink.EXCLUDED);
    }

    private Response invokeActionUsingAdapters(final ObjectAction action, final JsonRepresentation arguments, SelfLink selfLink) {

        final List<ObjectAdapter> argAdapters = parseAndValidateArguments(action, arguments);

        // invoke
        final ObjectAdapter[] argArray2 = argAdapters.toArray(new ObjectAdapter[0]);
        final ObjectAdapter returnedAdapter = action.execute(objectAdapter, argArray2);

        // response (void)
        final ActionResultReprRenderer renderer = new ActionResultReprRenderer(resourceContext, null, selfLink, JsonRepresentation.newMap());

        renderer.with(new ObjectAndActionInvocation(objectAdapter, action, arguments, returnedAdapter)).using(adapterLinkTo);

        final ResponseBuilder respBuilder = ResourceAbstract.responseOfOk(renderer, Caching.NONE);

        final Version version = objectAdapter.getVersion();
        ResourceAbstract.addLastModifiedAndETagIfAvailable(respBuilder, version);

        return respBuilder.build();
    }

    /**
     *
     * @param resourceContext
     * @param objectSpec
     *            - the {@link ObjectSpecification} to interpret the object as.
     * @param argRepr
     *            - expected to be either a String or a Map (ie from within a
     *            List, built by parsing a JSON structure).
     */
    private static ObjectAdapter objectAdapterFor(final RendererContext resourceContext, final ObjectSpecification objectSpec, final JsonRepresentation argRepr) {

        if (argRepr == null) {
            return null;
        }

        if(!argRepr.mapHas("value")) {
            String reason = "No 'value' key";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }

        if (objectSpec == null) {
            String reason = "ObjectSpec is null, cannot validate";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }
        
        final JsonRepresentation argValueRepr = argRepr.getRepresentation("value");

        // value (encodable)
        if (objectSpec.isEncodeable()) {
            try {
                return JsonValueEncoder.asAdapter(objectSpec, argValueRepr, null);
            }catch(IllegalArgumentException ex) {
                argRepr.mapPut("invalidReason", ex.getMessage());
                throw ex;
            }catch(Exception ex) {
                StringBuilder buf = new StringBuilder("Failed to parse representation ");
                try {
                    final String reprStr = argRepr.getString("value");
                    buf.append("'").append(reprStr).append("' ");
                } catch(Exception ex2) {
                }
                buf.append("as value of type '").append(objectSpec.getShortIdentifier()).append("'");
                String reason = buf.toString();
                argRepr.mapPut("invalidReason", reason);
                throw new IllegalArgumentException(reason);
            }
        }

        // reference
        if (!argValueRepr.isLink()) {
            final String reason = "Expected a link (because this object's type is not a value) but found no 'href'";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }
        final String oidFromHref = UrlParserUtils.encodedOidFromLink(argValueRepr);
        if (oidFromHref == null) {
            final String reason = "Could not parse 'href' to identify the object's OID";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }

        final ObjectAdapter objectAdapter = OidUtils.getObjectAdapterElseNull(resourceContext, oidFromHref);
        if (objectAdapter == null) {
            final String reason = "'href' does not reference a known entity";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }
        return objectAdapter;
    }

    /**
     * Similar to
     * {@link #objectAdapterFor(ResourceContext, ObjectSpecification, Object)},
     * however the object being interpreted is a String holding URL encoded JSON
     * (rather than having already been parsed into a Map representation).
     *
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    ObjectAdapter objectAdapterFor(final ObjectSpecification spec, final String urlEncodedJson) throws JsonParseException, JsonMappingException, IOException {

        final String json = UrlDecoderUtils.urlDecode(urlEncodedJson);
        final JsonRepresentation representation = JsonMapper.instance().read(json);
        return objectAdapterFor(resourceContext, spec, representation);
    }


    // ///////////////////////////////////////////////////////////////////
    // get{MemberType}ThatIsVisibleAndUsable
    // ///////////////////////////////////////////////////////////////////

    protected OneToOneAssociation getPropertyThatIsVisibleForIntent(final String propertyId, final Intent intent, Where where) {

        final ObjectAssociation association;
        try {
            final ObjectSpecification specification = objectAdapter.getSpecification();
            association = specification.getAssociation(propertyId);
        } catch(Exception ex) {
            // fall through
            throwNotFoundException(propertyId, MemberType.PROPERTY);
            return null; // to keep compiler happy.
        }

        if (association == null || !association.isOneToOneAssociation()) {
            throwNotFoundException(propertyId, MemberType.PROPERTY);
        }
        
        final OneToOneAssociation property = (OneToOneAssociation) association;
        return memberThatIsVisibleForIntent(property, MemberType.PROPERTY, intent, where);
    }

    protected OneToManyAssociation getCollectionThatIsVisibleForIntent(final String collectionId, final Intent intent, Where where) {

        final ObjectAssociation association;
        try {
            final ObjectSpecification specification = objectAdapter.getSpecification();
            association = specification.getAssociation(collectionId);
        } catch(Exception ex) {
            // fall through
            throwNotFoundException(collectionId, MemberType.COLLECTION);
            return null; // to keep compiler happy.
        }
        if (association == null || !association.isOneToManyAssociation()) {
            throwNotFoundException(collectionId, MemberType.COLLECTION);
        } 
        final OneToManyAssociation collection = (OneToManyAssociation) association;
        return memberThatIsVisibleForIntent(collection, MemberType.COLLECTION, intent, where);
    }

    protected ObjectAction getObjectActionThatIsVisibleForIntent(final String actionId, final Intent intent, Where where) {

        final ObjectAction action;
        try {
            final ObjectSpecification specification = objectAdapter.getSpecification();
            action = specification.getObjectAction(actionId);
        } catch(Exception ex) {
            throwNotFoundException(actionId, MemberType.ACTION);
            return null; // to keep compiler happy.
        }
        if (action == null) {
            throwNotFoundException(actionId, MemberType.ACTION);
        } 
        return memberThatIsVisibleForIntent(action, MemberType.ACTION, intent, where);
    }

    protected <T extends ObjectMember> T memberThatIsVisibleForIntent(final T objectMember, final MemberType memberType, final Intent intent, Where where) {
        final String memberId = objectMember.getId();
        final AuthenticationSession authenticationSession = resourceContext.getAuthenticationSession();
        if (objectMember.isVisible(authenticationSession, objectAdapter, where).isVetoed()) {
            throwNotFoundException(memberId, memberType);
        }
        if (intent.isMutate()) {
            final Consent usable = objectMember.isUsable(authenticationSession, objectAdapter, where);
            if (usable.isVetoed()) {
                throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.FORBIDDEN, usable.getReason());
            }
        }
        return objectMember;
    }

    protected static void throwNotFoundException(final String memberId, final MemberType memberType) {
        final String memberTypeStr = memberType.name().toLowerCase();
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.NOT_FOUND, "%s '%s' either does not exist or is not visible", memberTypeStr, memberId);
    }

    // ///////////////////////////////////////////////////////////////////
    // parseBody
    // ///////////////////////////////////////////////////////////////////

    /**
     *
     * @param objectSpec
     * @param bodyAsString
     *            - as per {@link #asStringUtf8(InputStream)}
     * @return
     */
    ObjectAdapter parseAsMapWithSingleValue(final ObjectSpecification objectSpec, final String bodyAsString) {
        final JsonRepresentation arguments = readAsMap(bodyAsString);
        return parseAsMapWithSingleValue(objectSpec, arguments);
    }

    ObjectAdapter parseAsMapWithSingleValue(final ObjectSpecification objectSpec, final JsonRepresentation arguments) {
        final JsonRepresentation representation = arguments.getRepresentation("value");
        if (arguments.size() != 1 || representation == null) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "Body should be a map with a single key 'value' whose value represents an instance of type '%s'", resourceFor(objectSpec));
        }

        return objectAdapterFor(resourceContext, objectSpec, arguments);
    }

    private List<ObjectAdapter> parseAndValidateArguments(final ObjectAction action, final JsonRepresentation arguments) {
        final List<JsonRepresentation> argList = argListFor(action, arguments);

        final List<ObjectAdapter> argAdapters = Lists.newArrayList();
        final List<ObjectActionParameter> parameters = action.getParameters();
        boolean valid = true;
        for (int i = 0; i < argList.size(); i++) {
            final JsonRepresentation argRepr = argList.get(i);
            final ObjectSpecification paramSpec = parameters.get(i).getSpecification();
            try {
                final ObjectAdapter argAdapter = objectAdapterFor(resourceContext, paramSpec, argRepr);
                argAdapters.add(argAdapter);

                // validate individual arg
                final ObjectActionParameter parameter = parameters.get(i);
                final Object argPojo = argAdapter!=null?argAdapter.getObject():null;
                final String reasonNotValid = parameter.isValid(objectAdapter, argPojo, null);
                if (reasonNotValid != null) {
                    argRepr.mapPut("invalidReason", reasonNotValid);
                    valid = false;
                }
            } catch (final IllegalArgumentException e) {
                argAdapters.add(null);
                valid = false;
            }
        }
        
        // validate all args
        final ObjectAdapter[] argArray = argAdapters.toArray(new ObjectAdapter[0]);
        final Consent consent = action.isProposedArgumentSetValid(objectAdapter, argArray);
        if (consent.isVetoed()) {
            arguments.mapPut("x-ro-invalidReason", consent.getReason());
            valid = false;
        }

        if(!valid) {
            throw RestfulObjectsApplicationException.createWithBody(HttpStatusCode.VALIDATION_FAILED, arguments, "Validation failed, see body for details");
        }
        
        return argAdapters;
    }

    private static List<JsonRepresentation> argListFor(final ObjectAction action, final JsonRepresentation arguments) {
        final List<JsonRepresentation> argList = Lists.newArrayList();

        // ensure that we have no arguments that are not parameters
        for (final Entry<String, JsonRepresentation> arg : arguments.mapIterable()) {
            final String argName = arg.getKey();
            if (action.getParameterById(argName) == null) {
                String reason = String.format("Argument '%s' found but no such parameter", argName);
                arguments.mapPut("x-ro-invalidReason", reason);
                throw RestfulObjectsApplicationException.createWithBody(HttpStatusCode.BAD_REQUEST, arguments, reason);
            }
        }

        // ensure that an argument value has been provided for all non-optional
        // parameters
        final List<ObjectActionParameter> parameters = action.getParameters();
        for (final ObjectActionParameter param : parameters) {
            final String paramId = param.getId();
            final JsonRepresentation argRepr = arguments.getRepresentation(paramId);
            if (argRepr == null && !param.isOptional()) {
                String reason = String.format("No argument found for (mandatory) parameter '%s'", paramId);
                arguments.mapPut("x-ro-invalidReason", reason);
                throw RestfulObjectsApplicationException.createWithBody(HttpStatusCode.BAD_REQUEST, arguments, reason);
            }
            argList.add(argRepr);
        }
        return argList;
    }

    public static JsonRepresentation readParameterMapAsMap(final Map<String, String[]> parameterMap) {
        final JsonRepresentation map = JsonRepresentation.newMap();
        for (final Map.Entry<String, String[]> parameter : parameterMap.entrySet()) {
            map.mapPut(parameter.getKey(), parameter.getValue()[0]);
        }
        return map;
    }

    public static JsonRepresentation readQueryStringAsMap(final String queryString) {
        if (queryString == null) {
            return JsonRepresentation.newMap();
        }
        final String queryStringTrimmed = queryString.trim();
        if (queryStringTrimmed.isEmpty()) {
            return JsonRepresentation.newMap();
        }
        return read(queryStringTrimmed, "query string");
    }

    public static JsonRepresentation readAsMap(final String body) {
        if (body == null) {
            return JsonRepresentation.newMap();
        }
        final String bodyTrimmed = body.trim();
        if (bodyTrimmed.isEmpty()) {
            return JsonRepresentation.newMap();
        }
        return read(bodyTrimmed, "body");
    }

    private static JsonRepresentation read(final String args, final String argsNature) {
        try {
            final JsonRepresentation jsonRepr = JsonMapper.instance().read(args);
            if (!jsonRepr.isMap()) {
                throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "could not read %s as a JSON map", argsNature);
            }
            return jsonRepr;
        } catch (final JsonParseException e) {
            throw RestfulObjectsApplicationException.createWithCauseAndMessage(HttpStatusCode.BAD_REQUEST, e, "could not parse %s", argsNature);
        } catch (final JsonMappingException e) {
            throw RestfulObjectsApplicationException.createWithCauseAndMessage(HttpStatusCode.BAD_REQUEST, e, "could not read %s as JSON", argsNature);
        } catch (final IOException e) {
            throw RestfulObjectsApplicationException.createWithCauseAndMessage(HttpStatusCode.BAD_REQUEST, e, "could not parse %s", argsNature);
        }
    }

    public static String asStringUtf8(final InputStream body) {
        try {
            final byte[] byteArray = ByteStreams.toByteArray(body);
            return new String(byteArray, Charsets.UTF_8);
        } catch (final IOException e) {
            throw RestfulObjectsApplicationException.createWithCauseAndMessage(HttpStatusCode.BAD_REQUEST, e, "could not read body");
        }
    }

    // //////////////////////////////////////////////////////////////
    // misc
    // //////////////////////////////////////////////////////////////

    private static String resourceFor(final ObjectSpecification objectSpec) {
        // TODO: should return a string in the form
        // http://localhost:8080/types/xxx
        return objectSpec.getFullIdentifier();
    }

}
