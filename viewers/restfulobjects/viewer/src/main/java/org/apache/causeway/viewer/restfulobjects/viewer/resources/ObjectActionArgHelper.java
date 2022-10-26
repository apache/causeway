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

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Railway;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Utility class that encapsulates the logic for parsing arguments to be invoked by an
 * {@link ObjectAction}.
 */
@RequiredArgsConstructor(staticName = "of")
public class ObjectActionArgHelper {

    public static Can<Railway<InteractionVeto, ManagedObject>> parseArguments(
            final IResourceContext resourceContext,
            final ObjectAction action,
            final JsonRepresentation arguments) {

        val jsonArgList = argListFor(action, arguments);
        val parameters = action.getParameters();

        return IntStream.range(0, jsonArgList.size())
        .mapToObj(argIndex->{
            final JsonRepresentation argRepr = jsonArgList.get(argIndex);
            val paramMeta = parameters.getElseFail(argIndex);
            val paramSpec = paramMeta.getElementType();

            val tryArgument = (paramMeta.isOptional()
                    && argRepr == null)
                    ? Try.success(ManagedObject.empty(paramSpec))
                    : Try.call(()->
                        new JsonParserHelper(resourceContext, paramSpec)
                            .objectAdapterFor(argRepr))
                        .mapSuccess(success->success!=null
                                ? success
                                : ManagedObject.empty(paramSpec));

            val objectOrVeto = tryArgument.<Railway<InteractionVeto, ManagedObject>>fold(
                    exception->Railway.failure(
                            InteractionVeto.actionParamInvalid(
                                    String.format("exception when parsing paramNr %d [%s]: %s",
                                            argIndex, argRepr, exception))),
                    success->Railway.success(success.orElseThrow()));

            return objectOrVeto;
        })
        .collect(Can.toCan());
    }

    private static List<JsonRepresentation> argListFor(
            final ObjectAction action,
            final JsonRepresentation arguments) {

        // ensure that we have no arguments that are not parameters
        arguments.streamMapEntries()
        .map(Map.Entry::getKey)
        .filter(argName->!argName.startsWith("x-ro"))
        .forEach(argName->{
            if (action.getParameterById(argName) == null) {
                String reason = String.format("Argument '%s' found but no such parameter", argName);
                arguments.mapPutString("x-ro-invalidReason", reason);
                throw RestfulObjectsApplicationException
                    .createWithBody(RestfulResponse.HttpStatusCode.BAD_REQUEST, arguments, reason);
            }
        });

        // ensure that an argument value has been provided for all non-optional
        // parameters
        val argList = _Lists.<JsonRepresentation>newArrayList();
        val parameters = action.getParameters();
        for (final ObjectActionParameter param : parameters) {
            final String paramId = param.getId();
            final JsonRepresentation argRepr = arguments.getRepresentation(paramId);
            if (argRepr == null
                    && !param.isOptional()) {
                val reason = String.format("No argument found for (mandatory) parameter '%s'", paramId);
                arguments.mapPutString("x-ro-invalidReason", reason);
                throw RestfulObjectsApplicationException
                    .createWithBody(RestfulResponse.HttpStatusCode.BAD_REQUEST, arguments, reason);
            }
            argList.add(argRepr);
        }
        return argList;
    }



}
