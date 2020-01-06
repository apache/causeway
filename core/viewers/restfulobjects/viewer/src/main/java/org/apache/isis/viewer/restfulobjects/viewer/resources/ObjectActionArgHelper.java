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
package org.apache.isis.viewer.restfulobjects.viewer.resources;

import java.util.List;
import java.util.Map;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

import lombok.val;

/**
 * Utility class that encapsulates the logic for parsing arguments to be invoked by an
 * {@link org.apache.isis.metamodel.spec.feature.ObjectAction}.
 */
public class ObjectActionArgHelper {

    private final IResourceContext resourceContext;
    private final ManagedObject objectAdapter;
    private final ObjectAction action;

    public ObjectActionArgHelper(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter,
            final ObjectAction action) {
        this.resourceContext = resourceContext;
        this.objectAdapter = objectAdapter;
        this.action = action;
    }

    public Can<ManagedObject> parseAndValidateArguments(final JsonRepresentation arguments) {
        final List<JsonRepresentation> argList = argListFor(action, arguments);

        final List<ManagedObject> argAdapters = _Lists.newArrayList();
        val parameters = action.getParameters();
        boolean valid = true;
        for (int i = 0; i < argList.size(); i++) {
            final JsonRepresentation argRepr = argList.get(i);
            final ObjectSpecification paramSpec = parameters.getOrThrow(i).getSpecification();
            try {
                final ManagedObject argAdapter = new JsonParserHelper(resourceContext, paramSpec).objectAdapterFor(argRepr);
                argAdapters.add(argAdapter);

                // validate individual arg
                final ObjectActionParameter parameter = parameters.getOrThrow(i);
                final Object argPojo = argAdapter!=null?argAdapter.getPojo():null;
                final String reasonNotValid = parameter.isValid(objectAdapter, argPojo, InteractionInitiatedBy.USER);
                if (reasonNotValid != null) {
                    argRepr.mapPut("invalidReason", reasonNotValid);
                    valid = false;
                }
            } catch (final IllegalArgumentException e) {
                argAdapters.add(null);
                valid = false;
            }
        }

        // validate entire argument set
        val args = Can.ofCollection(argAdapters);
        final Consent consent = action.isArgumentSetValid(
                objectAdapter, args, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            arguments.mapPut("x-ro-invalidReason", consent.getReason());
            valid = false;
        }

        if(!valid) {
            throw RestfulObjectsApplicationException.createWithBody(
                    RestfulResponse.HttpStatusCode.VALIDATION_FAILED,
                    arguments,
                    "Validation failed, see body for details");
        }

        return args;
    }

    private static List<JsonRepresentation> argListFor(final ObjectAction action, final JsonRepresentation arguments) {
        final List<JsonRepresentation> argList = _Lists.newArrayList();

        // ensure that we have no arguments that are not parameters
        arguments.streamMapEntries()
        .map(Map.Entry::getKey)
        .filter(argName->!argName.startsWith("x-ro"))
        .forEach(argName->{
            if (action.getParameterById(argName) == null) {
                String reason = String.format("Argument '%s' found but no such parameter", argName);
                arguments.mapPut("x-ro-invalidReason", reason);
                throw RestfulObjectsApplicationException.createWithBody(RestfulResponse.HttpStatusCode.BAD_REQUEST, arguments, reason);
            }
        });

        // legacy of ...
        //        for (final Map.Entry<String, JsonRepresentation> arg : arguments.streamMap()) {
        //            final String argName = arg.getKey();
        //            if(argName.startsWith("x-ro")) {
        //                continue;
        //            }
        //            if (action.getParameterById(argName) == null) {
        //                String reason = String.format("Argument '%s' found but no such parameter", argName);
        //                arguments.mapPut("x-ro-invalidReason", reason);
        //                throw RestfulObjectsApplicationException.createWithBody(RestfulResponse.HttpStatusCode.BAD_REQUEST, arguments, reason);
        //            }
        //        }

        // ensure that an argument value has been provided for all non-optional
        // parameters
        val parameters = action.getParameters();
        for (final ObjectActionParameter param : parameters) {
            final String paramId = param.getId();
            final JsonRepresentation argRepr = arguments.getRepresentation(paramId);
            if (argRepr == null && !param.isOptional()) {
                String reason = String.format("No argument found for (mandatory) parameter '%s'", paramId);
                arguments.mapPut("x-ro-invalidReason", reason);
                throw RestfulObjectsApplicationException.createWithBody(RestfulResponse.HttpStatusCode.BAD_REQUEST, arguments, reason);
            }
            argList.add(argRepr);
        }
        return argList;
    }

}
