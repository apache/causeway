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
package org.apache.causeway.applib.services.swagger;

import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.Value;

/**
 * Specifies which elements of the metamodel are included within the generated
 * swagger spec.
 *
 * @since 1.x {@index}
 */
@Named(CausewayModuleApplib.NAMESPACE + ".services.swagger.Visibility")
@Value
public enum Visibility {

    /**
     * Specification for use by third-party clients, ie public use and not
     * under the control of the authors of the backend Apache Causeway application.
     *
     * <p>
     * The generated swagger spec is therefore restricted only to include only
     * view models ({@link DomainObject#nature()} of
     * {@link org.apache.causeway.applib.annotation.Nature#VIEW_MODEL})
     * and to REST domain services ({@link DomainService#nature()} of
     * {@link NatureOfService#REST}). Exposing entities also would couple the
     * REST client too deeply to the backend implementation.
     * </p>
     */
    PUBLIC,
    /**
     * Specification for use only by internally-managed clients, ie private
     * internal use.
     *
     * <p>
     * This visibility level removes all constraints and so includes the
     * specifications of domain entities as well as view models. This is
     * perfectly acceptable where the team developing the REST client is the
     * same as the team developing the backend service ... the use of the REST
     * API between the client and server is a private implementation detail of
     * the application.
     * </p>
     */
    PRIVATE,
    /**
     * As {@link #PRIVATE}, also including any prototype actions (where
     * {@link Action#restrictTo()} set to {@link RestrictTo#PROTOTYPING}).
     */
    PRIVATE_WITH_PROTOTYPING;

    public boolean isPublic() {
        return this == PUBLIC;
    }

    public boolean isPrivate() {
        return this == PRIVATE;
    }

    public boolean isPrivateWithPrototyping() {
        return this == PRIVATE_WITH_PROTOTYPING;
    }
}
