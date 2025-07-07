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
package org.apache.causeway.viewer.restfulobjects.viewer.resources;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender.JsonValueEncoderService;
import org.apache.causeway.viewer.restfulobjects.rendering.util.RequestParams;

/**
 * Utility class that encapsulates the logic for parsing some content (JSON, or a simple string that is JSON)
 * into an{@link ManagedObject} of a specified
 * {@link ObjectSpecification type}.
 */
public class JsonParserHelper {

    private static final Pattern OBJECT_OID = Pattern.compile(".*objects\\/([^/]+)\\/(.+)");

    private final IResourceContext resourceContext;
    private final ObjectSpecification objectSpec;
    private final JsonValueEncoderService jsonValueEncoder;

    public JsonParserHelper(final IResourceContext resourceContext, final ObjectSpecification objectSpecification) {
        this.objectSpec = objectSpecification;
        this.resourceContext = resourceContext;
        this.jsonValueEncoder = resourceContext.getMetaModelContext().getServiceRegistry()
                .lookupServiceElseFail(JsonValueEncoderService.class);
    }

    /**
     * @param bodyAsString
     *            - as per {@link org.apache.causeway.viewer.restfulobjects.rendering.util.Util#asStringUtf8(java.io.InputStream)}
     * @return
     */
    ManagedObject parseAsMapWithSingleValue(final RequestParams body) {
        final JsonRepresentation arguments = body.asMap();
        return parseAsMapWithSingleValue(arguments);
    }

    ManagedObject parseAsMapWithSingleValue(final JsonRepresentation arguments) {
        final JsonRepresentation representation = arguments.getRepresentation("value");
        if (arguments.size() != 1 || representation == null) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatus.BAD_REQUEST,
                "Body should be a map with a single key 'value' whose value represents an instance of type '%s'".formatted(resourceFor(objectSpec)));
        }

        return objectAdapterFor(arguments);
    }

    /**
     *
     * @param argRepr
     *            - expected to be either a String or a Map (ie from within a
     *            List, built by parsing a JSON structure).
     */
    ManagedObject objectAdapterFor(final JsonRepresentation argRepr) {

        if (argRepr == null) {
            return null;
        }

        if(!argRepr.mapHas("value")) {
            String reason = "No 'value' key";
            argRepr.mapPutString("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }

        if (objectSpec == null) {
            String reason = "ObjectSpec is null, cannot validate";
            argRepr.mapPutString("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }

        final JsonRepresentation argValueRepr = argRepr.getRepresentation("value");

        // value (encodable)
        if (objectSpec.isValue()) {
            try {
                return jsonValueEncoder.asAdapter(objectSpec, argValueRepr, null);
            } catch(IllegalArgumentException ex) {
                argRepr.mapPutString("invalidReason", ex.getMessage());
                throw ex;
            } catch(Exception ex) {
                StringBuilder buf = new StringBuilder("Failed to parse representation ");
                try {
                    final String reprStr = argRepr.getString("value");
                    buf.append("'").append(reprStr).append("' ");
                } catch(Exception ex2) {
                }
                buf.append("as value of type '").append(objectSpec.getShortIdentifier()).append("'");
                String reason = buf.toString();
                argRepr.mapPutString("invalidReason", reason);
                throw new IllegalArgumentException(reason);
            }
        }

        // reference
        if (!argValueRepr.isLink()) {
            final String reason = "Expected a link (because this object's type is not a value) but found no 'href'";
            argRepr.mapPutString("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }
        final String oidFromHref = encodedOidFromLink(argValueRepr);
        if (oidFromHref == null) {
            final String reason = "Could not parse 'href' to identify the object's OID";
            argRepr.mapPutString("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }

        var objectAdapter = resourceContext.objectAdapterForOidFromHref(oidFromHref)
                .orElseThrow(()->{
                    var reason = "'href' does not reference a known entity";
                    argRepr.mapPutString("invalidReason", reason);
                    return new IllegalArgumentException(reason);
                });
        return objectAdapter;
    }

    static String encodedOidFromLink(final JsonRepresentation link) {
        final String href = link.getString("href");

        final Matcher matcher = OBJECT_OID.matcher(href);
        if (!matcher.matches()) {
            return null;
        }
        String domainType = matcher.group(1);
        String instanceId = matcher.group(2);

        return Bookmark.forLogicalTypeNameAndIdentifier(domainType, instanceId).stringify();
    }

    private static String resourceFor(final ObjectSpecification objectSpec) {
        // TODO: should return a string in the form
        // http://localhost:8080/types/xxx
        return objectSpec.getFullIdentifier();
    }

}
