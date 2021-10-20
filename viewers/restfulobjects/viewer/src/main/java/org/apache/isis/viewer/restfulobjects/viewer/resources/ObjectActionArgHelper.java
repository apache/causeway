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
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Utility class that encapsulates the logic for parsing arguments to be invoked by an
 * {@link ObjectAction}.
 */
@RequiredArgsConstructor(staticName = "of")
public class ObjectActionArgHelper {

    public static Can<_Either<ManagedObject, InteractionVeto>> parseArguments(
            final IResourceContext resourceContext,
            final ObjectAction action,
            final JsonRepresentation arguments) {

        val jsonArgList = argListFor(action, arguments);

        final List<_Either<ManagedObject, InteractionVeto>> argAdapters = _Lists.newArrayList();
        val parameters = action.getParameters();
        for (int i = 0; i < jsonArgList.size(); i++) {
            final JsonRepresentation argRepr = jsonArgList.get(i);
            final int argIndex = i;
            val paramMeta = parameters.getElseFail(argIndex);
            val paramSpec = paramMeta.getElementType();

            val objectOrVeto = Result.of(()->
                    (paramMeta.isOptional() && argRepr == null)
                    ? ManagedObject.empty(paramSpec)
                    : new JsonParserHelper(resourceContext, paramSpec)
                            .objectAdapterFor(argRepr))
            .<_Either<ManagedObject, InteractionVeto>>fold(
                    _Either::left,
                    exception->_Either.right(
                            InteractionVeto.actionParamInvalid(
                                    String.format("exception when parsing paramNr %d [%s]: %s",
                                            argIndex, argRepr, exception))));

            argAdapters.add(objectOrVeto);
        }
        return Can.ofCollection(argAdapters);
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
