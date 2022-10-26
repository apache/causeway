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
package org.apache.causeway.applib.services.acceptheader;

import java.util.List;

import javax.ws.rs.core.MediaType;

/**
 * This service simply exposes the HTTP `Accept` header to the domain.
 *
 * <p>
 * Its intended use is to support multiple versions of a REST API, where the
 * responsibility for content negotiation (determining which version of the
 * REST API is to be used) is managed by logic in the domain objects themselves.
 *
 * </p>
 *
 * @since 1.x {@index}
 */
public interface AcceptHeaderService {
    /**
     * The intention is that this service only returns a list when the request
     * is initiated through the _Restful Objects viewer_.
     *
     * <p>
     * Otherwise the service will likely return `null`.
     * </p>
     */
    List<MediaType> getAcceptableMediaTypes();
}
