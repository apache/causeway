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

import org.springframework.lang.Nullable;

import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

import org.jspecify.annotations.NonNull;

public class InteractionFailureHandler {

    public static RestfulObjectsApplicationException onFailure(
            final @Nullable InteractionVeto veto) {

        if(veto==null) {
            return RestfulObjectsApplicationException
                    .createWithMessage(RestfulResponse.HttpStatusCode.INTERNAL_SERVER_ERROR,
                            "unexpected empty failure holder");
        }

        switch(veto.getVetoType()) {
        case NOT_FOUND:
        case HIDDEN:
            return RestfulObjectsApplicationException
                    .createWithMessage(RestfulResponse.HttpStatusCode.NOT_FOUND,
                            veto.getReasonAsString().orElse(null));

        case READONLY:
        case INVALID:
            return RestfulObjectsApplicationException
                    .createWithMessage(RestfulResponse.HttpStatusCode.FORBIDDEN,
                            veto.getReasonAsString().orElse(null));

        case ACTION_NOT_SAFE:
        case ACTION_NOT_IDEMPOTENT:
            return RestfulObjectsApplicationException
                    .createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                            veto.getReasonAsString().orElse(null));

        case ACTION_PARAM_INVALID:
            return RestfulObjectsApplicationException
                    .createWithMessage(RestfulResponse.HttpStatusCode.VALIDATION_FAILED,
                            veto.getReasonAsString().orElse(null));
        }

        return RestfulObjectsApplicationException
                .createWithMessage(RestfulResponse.HttpStatusCode.INTERNAL_SERVER_ERROR,
                        "unmatched veto type " + veto.getVetoType());

    }

    public static RestfulObjectsApplicationException onParameterListInvalid(
            final @NonNull InteractionVeto veto,
            final @NonNull JsonRepresentation arguments) {

        if(veto!=null) {
            arguments.mapPutString("x-ro-invalidReason", veto.getReasonAsString().orElse(null));
        }
        return RestfulObjectsApplicationException
                .createWithBody(RestfulResponse.HttpStatusCode.VALIDATION_FAILED,
                        arguments,
                        "Validation failed, see body for details");
    }

    // collect info for each individual param that is not valid
    public static void collectParameterInvalid(
            final @NonNull ObjectActionParameter paramMeta,
            final @NonNull InteractionVeto veto,
            final @NonNull JsonRepresentation arguments) {

        var paramId = paramMeta.getId();
        var argRepr = arguments.getRepresentation(paramId);
        argRepr.mapPutString("invalidReason", veto.getReasonAsString().orElse(null));
    }

}
